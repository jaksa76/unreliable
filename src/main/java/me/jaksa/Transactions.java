package me.jaksa;

/**
 * Provides utility functions for lightweight transaction management. Full ACID transaction semantics are <b>NOT</b> provided.
 * The methods in this class allow weak atomicity: either all or no operations will be performed
 * as long as the process doesn't crash. The user must provide rollback and/or commit mechanisms.
 * For critical applications where preserving atomicity even
 * in crash scenarios is more important than performance, you should use a full transaction manager.
 * Consistency, Isolation and Durability must be implemented by the user.
 */
public class Transactions {
    /**
     * Builds an {@link VoidTransaction} that can have a rollback and/or a commit.
     *
     * @param r the operation to perform
     * @return the {@link VoidTransaction} object for chaining modifiers
     */
    public static VoidTransaction perform(RunnableWithException r) {
        return new VoidTransaction(r);
    }


    /**
     * Tries to perform one or more {@link VoidTransaction}s, retrying each one several times in case of failures.
     * {@link VoidTransaction}s can have defined rollbacks and commits, number of retries etc.
     *
     * @param voidTransactions the transactions to perform
     */
    public static void atomically(VoidTransaction... voidTransactions) {
        int operationsToRollback = 0;
        try {
            for (VoidTransaction voidTransaction : voidTransactions) {
                Unreliable.tenaciously(() -> {
                    try {
                        voidTransaction.run();
                    } catch (Exception e) {
                        voidTransaction.performRollback();
                        throw e;
                    }
                }, voidTransaction.getRetries());
                operationsToRollback++;
            }
        } catch (Exception e) {
            for (int i = 0; i < operationsToRollback; i++) {
                voidTransactions[i].performRollback();
            }
            throw new RuntimeException(e);
        }
        for (VoidTransaction voidTransaction : voidTransactions) {
            voidTransaction.performCommit();
        }
    }


    /**
     * Builds a {@link FunctionalTransaction} that can have a rollback and/or a commit.
     *
     * @param function the function to evaluate
     * @param <T> the type of the value to return
     * @return the value returned by a successful invocation of the function
     */
    public static <T> FunctionalTransaction<T> evaluate(SupplierWithException<T> function) {
        return new FunctionalTransaction<T>(function);
    }


    /**
     * Tries to evaluate a {@link FunctionalTransaction}, retrying each one several times in case of failures.
     * {@link FunctionalTransaction}s can have defined rollbacks and commits, number of retries etc.
     *
     * @param transaction the transactiontransaction to evaluate
     * @param <T> the type of the value to return
     * @return the value returned by a successful invocation of the function
     */
    public static <T> T atomically(FunctionalTransaction<T> transaction) {
        T result = prepareAll(transaction);
        commitAll(transaction);
        return result;
    }


    /**
     * Either prepares the transaction and all its predecessors or throws an exception rolling back
     * the function and all it's predecessors.
     */
    static <R, T> T prepareAll(FunctionalTransaction<T> f) {
        if (f.previous == null) return prepare(f);

        f.resultOfPrevious = prepareAll(f.previous);

        try {
            return prepare(f);
        } catch (Exception e) {
            rollbackAll(f.previous);
            throw e;
        }
    }


    /**
     * Either prepares this transaction of rolls back throwing an exception
     */
    private static <T> T prepare(FunctionalTransaction<T> transaction) {
        return Unreliable.tenaciously(() -> {
            try {
                return transaction.evaluate();
            } catch (Exception e) {
                transaction.performRollback();
                throw e;
            }
        }, transaction.getRetries());
    }


    static <R> void rollbackAll(FunctionalTransaction<R> f) {
        if (f.previous != null) rollbackAll(f.previous);
        f.performRollback();
    }


    static <T> void commitAll(FunctionalTransaction<T> f) {
        if (f.previous != null) commitAll(f.previous);
        f.performCommit();
    }
}
