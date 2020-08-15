package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.CoinContract;
import com.template.contracts.PaymentContract;
import com.template.states.CoinState;
import com.template.states.PaymentState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
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
public class MakePayment extends FlowLogic<String> {

    private final UUID linearId;
    private final String accountName;
    private final String bankAccountName;
    private final double amtToShop;
    private final double amtToDelivery;

    public MakePayment(UUID linearId, String accountName, String bankAccountName, double amtToShop, double amtToDelivery) {
        this.linearId = linearId;
        this.accountName = accountName;
        this.bankAccountName = bankAccountName;
        this.amtToShop = amtToShop;
        this.amtToDelivery = amtToDelivery;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(accountName).get(0).getState().getData();
        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(bankAccountName).get(0).getState().getData();

        AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        double totalCost = amtToDelivery + amtToShop;
        double availableBalance = 0;

        UUID id = senderAccountInfo.getIdentifier().getId();
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria().withExternalIds(Arrays.asList(id));
        List<StateAndRef<CoinState>> coinStates = getServiceHub().getVaultService().queryBy(CoinState.class,criteria).getStates();
        StateAndRef<CoinState> inputState = coinStates.get(0);

        if(coinStates.size() > 0)
            availableBalance = coinStates.get(0).getState().getData().getValue();
        else
            availableBalance = 0;


        if(totalCost > availableBalance) {
            throw new FlowException("Insufficient Balance");
        }
        else {
            FlowSession receiverSession = initiateFlow(receiverParty);
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            Command paymentCommand = new Command(new PaymentContract.Issue(), Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));
            Command coinCommand = new Command(new CoinContract.Consume(),Arrays.asList(receiverParty.getOwningKey(),senderParty.getOwningKey()));

            PaymentState outputPaymentState = new PaymentState(new UniqueIdentifier(null,linearId),amtToShop,amtToDelivery,senderParty,receiverParty);
            CoinState outputCoinState = new CoinState(availableBalance - totalCost,bankAccountName,receiverParty,accountName,senderParty);

            TransactionBuilder txB = new TransactionBuilder(notary)
                    .addInputState(inputState)
                    .addOutputState(outputPaymentState)
                    .addOutputState(outputCoinState)
                    .addCommand(paymentCommand)
                    .addCommand(coinCommand);

            SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
            final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                    Arrays.asList(receiverSession), Collections.singleton(senderParty.getOwningKey())));

            SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));
            return stx.getId().toString();
        }
    }
}

@InitiatedBy(MakePayment.class)
class MakePaymentResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public MakePaymentResponder(FlowSession counterpartySession) {
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

