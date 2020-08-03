package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.utilities.ProgressTracker;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class HandoverItem extends FlowLogic<Void> {



    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        return null;
    }
}
