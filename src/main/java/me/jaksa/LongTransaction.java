package me.jaksa;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transaction that is composed out of several transactional operations. Transactional operations are added
 * using the {@link #transactionally(FunctionalTransaction)}. Each transactional operation can have it's verification,
 * commit or rollback function. The body of each operation and the verification are performed immediately while the commit
 * and rollback are performed only when the {@link LongTransaction} is committed or rolled back.
 *
 * Notice that this class implements the {@link AutoCloseable} interface, which means it can be used as a resource in a
 * try-catch block. In other words the transaction will be committed at the end of the try block unless it has been
 * already rolled back. It will not be automatically rolled back in case of an exception.
 */
class LongTransaction implements AutoCloseable {
    private final List<FunctionalTransaction> steps = new ArrayList<>();

    // TODO add state management and checks for correct usage

    /**
     * Performs a transactional operation and adds it to the {@link LongTransaction}.
     * The verification of the operation is also performed immediately. The commit and rollback of the operation will
     * be performed onlt when the whole {@link LongTransaction} is committed/rolled back.
     *
     * @param operation the transactional operation that should be part of this transaction
     * @param <T> the type of value returned by the transactional operation
     * @return the value returned by the transactional operation
     */
    public <T> T transactionally(FunctionalTransaction<T> operation) {
        steps.add(operation);
        return Transactions.prepareAll(operation);
    }

    /**
     * Commits all the transactional operations that have been added by {@link #transactionally(FunctionalTransaction)}.
     */
    public void commit() {
        for (FunctionalTransaction step : steps) {
            Transactions.commitAll(step);
        }
        steps.clear();
    }

    /**
     * Rolls back all the transactional operations that have been added by {@link #transactionally(FunctionalTransaction)}.
     */
    public void rollback() {
        for (FunctionalTransaction step : steps) {
            Transactions.rollbackAll(step);
        }
        steps.clear();
    }

    /**
     * Will all the transactional operations that have been added by {@link #transactionally(FunctionalTransaction)},
     * unless the transaction has already been committed or rolled back.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        commit();
    }
}
