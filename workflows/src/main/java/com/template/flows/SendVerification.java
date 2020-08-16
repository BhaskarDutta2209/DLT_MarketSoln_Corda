package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.VerificationContract;
import com.template.states.VerificationState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SendVerification extends FlowLogic<Void> {

    private final UUID uuid;
    private final String receiver;

    public SendVerification(UUID uuid, String receiver) {
        this.uuid = uuid;
        this.receiver = receiver;
    }


    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier linearId = new UniqueIdentifier(null,uuid);

        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        VerificationState outputState = new VerificationState(linearId,receiverParty);
        Command command = new Command(new VerificationContract.Issue(), Arrays.asList(getOurIdentity().getOwningKey(),receiverParty.getOwningKey()));
        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(command);

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,getOurIdentity().getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession), Collections.singleton(getOurIdentity().getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        return null;
    }
}


@InitiatedBy(SendVerification.class)
class SendVerificationResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public SendVerificationResponder(FlowSession counterpartySession) {
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

