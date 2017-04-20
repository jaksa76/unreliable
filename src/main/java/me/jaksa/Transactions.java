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
     * Builds an {@link Operation} that can have a rollback and/or a commit.
     *
     * @param r the operation to perform
     * @return the {@link Operation} object for chaining modifiers
     */
    public static Operation perform(RunnableWithException r) {
        return new Operation(r);
    }

    /**
     * Tries to perform one or more {@link Operation}s, retrying each one several times in case of failures.
     * {@link Operation}s can have defined rollbacks and commits, number of retries etc.
     *
     * @param operations the operations to perform
     */
    public static void atomically(Operation... operations) {
        int operationsToRollback = 0;
        try {
            for (Operation operation : operations) {
                Unreliable.tenaciusly(() -> {
                    try {
                        operation.run();
                    } catch (Exception e) {
                        operation.performRollback();
                        throw e;
                    }
                }, operation.getRetries());
                operationsToRollback++;
            }
        } catch (Exception e) {
            for (int i = 0; i < operationsToRollback; i++) {
                operations[i].performRollback();
            }
            throw new RuntimeException(e);
        }
        for (Operation operation : operations) {
            operation.performCommit();
        }
    }

    /**
     * Builds a {@link Function} that can have a rollback and/or a commit.
     *
     * @param function
     * @param <T>
     * @return
     */
    public static <T> Function<T> evaluate(SupplierWithException<T> function) {
        return new Function<T>(function);
    }

    /**
     * Tries to evaluate a {@link Function}, retrying each one several times in case of failures.
     * {@link Function}s can have defined rollbacks and commits, number of retries etc.
     *
     * @param function the function to evaluate
     */
    public static <T> T atomically(Function<T> function) {
        T result = prepareAll(function);
        commitAll(function);
        return result;
    }


    /**
     * Either prepares the function and all its predecessors or throws an exception rolling back
     * the function and all it's predecessors.
     */
    private static <R, T> T prepareAll(Function<T> f) {
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
     * Either prepares this function of rolls back throwing an exception
     */
    private static <T> T prepare(Function<T> function) {
        return Unreliable.tenaciusly(() -> {
            try {
                return function.evaluate();
            } catch (Exception e) {
                function.performRollback();
                throw e;
            }
        }, function.getRetries());
    }


    private static <R> void rollbackAll(Function<R> f) {
        if (f.previous != null) rollbackAll(f.previous);
        f.performRollback();
    }


    private static <T> void commitAll(Function<T> f) {
        if (f.previous != null) commitAll(f.previous);
        f.performCommit();
    }
}
