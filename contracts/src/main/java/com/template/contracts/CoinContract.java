package com.template.contracts;

import com.sun.istack.NotNull;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * Contract *
// ************
public class CoinContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.CoinContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx)throws IllegalArgumentException {}

    // Used to indicate the transaction's intent.
    public static class Issue implements CommandData {}
    public static class Consume implements CommandData {}
    public static class Redeem implements CommandData {}
}