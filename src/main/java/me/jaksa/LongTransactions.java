package me.jaksa;

public class LongTransactions {
    private static ThreadLocal<LongTransaction> longTransactions = new ThreadLocal<>();

    public static void longTransaction(RunnableWithException r) {
        try {
            LongTransaction tx = beginTx();
            r.run();
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
    }

    public static LongTransaction beginTx() {
//        if (longTransactions.get() != null) throw new IllegalStateException("Cannot begin a new transaction: a transaction is already active.");
        LongTransaction tx = new LongTransaction();
        longTransactions.set(tx);
        return tx;
    }

    public static void commitTx() {
        getLongTransaction().commit();
        longTransactions.set(null);
    }

    public static void rollbackTx() {
        getLongTransaction().rollback();
        longTransactions.set(null);
    }

    public static <T> T transactionally(FunctionalTransaction<T> transaction) {
        T result = Transactions.prepareAll(transaction);
        getLongTransaction().addStep(transaction);
        return result;
    }

    private static LongTransaction getLongTransaction() {
        LongTransaction tx = longTransactions.get();
        if (tx == null) throw new IllegalStateException("You need to perform this within a transaction. Use longTransaction() or beginTx()");
        return tx;
    }
}
