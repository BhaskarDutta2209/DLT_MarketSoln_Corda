package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import com.template.contracts.DeliveryRespondeContract;
import com.template.contracts.ItemContract;
import com.template.states.DeliveryRespondState;
import com.template.states.ItemState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
@StartableByService
public class TransferItem extends FlowLogic<String> {
    private final UUID trackingId;
    private final UUID productKey;
    private final String sender;
    private final String receiver;

    public TransferItem(UUID trackingId, UUID productKey, String sender, String receiver) {
        this.trackingId = trackingId;
        this.productKey = productKey;
        this.sender = sender;
        this.receiver = receiver;
    }

    private StateAndRef<ItemState> findItemState(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<ItemState> itemStateStateAndRef = getServiceHub().getVaultService().queryBy(ItemState.class,criteria).getStates().get(0);
        return itemStateStateAndRef;
    }

    private StateAndRef<DeliveryRespondState> findDeliveryRespondState(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<DeliveryRespondState> deliveryRespondStateStateAndRef = getServiceHub().getVaultService().queryBy(DeliveryRespondState.class,criteria).getStates().get(0);
        return deliveryRespondStateStateAndRef;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();

        AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        UniqueIdentifier linearId = new UniqueIdentifier(null,productKey);
        UniqueIdentifier trackingLinearId = new UniqueIdentifier(null, trackingId);

        StateAndRef<ItemState> itemStateStateAndRef = findItemState(linearId);
        StateAndRef<DeliveryRespondState> deliveryRespondStateStateAndRef = findDeliveryRespondState(trackingLinearId);

        ItemState outputState = new ItemState(linearId,
                itemStateStateAndRef.getState().getData().getProductId(),
                itemStateStateAndRef.getState().getData().getProductName(),
                itemStateStateAndRef.getState().getData().getProductDetails(),
                itemStateStateAndRef.getState().getData().getShopAccountName(),
                receiverParty);

        Command itemCommand = new Command(new ItemContract.Transfer(), Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));
        Command deliveryRespondCommand = new Command(new DeliveryRespondeContract.Consume(),Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addInputState(itemStateStateAndRef)
                .addInputState(deliveryRespondStateStateAndRef)
                .addCommand(itemCommand)
                .addCommand(deliveryRespondCommand);

        txB.verify(getServiceHub());

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession),Collections.singletonList(senderParty.getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        //The below share is not used because there is only one owner

//        //Searching the created output state and calling ShareStateAndSyncAccounts flow
//
//        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
//                null,
//                Collections.singletonList(linearId),
//                Vault.StateStatus.UNCONSUMED,
//                null
//        );
//        StateAndRef<ItemState> itemState = getServiceHub().getVaultService().queryBy(ItemState.class,criteria).getStates().get(0);
//        subFlow(new ShareStateAndSyncAccounts(itemState,receiverAccountInfo.getHost()));

        return "Success with id: " + stx.toString();
    }
}

@InitiatedBy(TransferItem.class)
class TransferItemResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public TransferItemResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

            }
        });
        return subFlow(new ReceiveFinalityFlow(counterpartySession)).toString();
    }
}