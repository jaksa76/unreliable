package me.jaksa;

/**
 * Provides utility functions for lightweight transaction management. Full ACID transaction semantics are <b>NOT</b> provided.
 * The methods of this class allow multiple transactional operations to be performed atomically. Unlike the methods \
 * from {@link Transactions}, the methods here don't require the operations to be chained together but can be
 * interleaved with non-transactional operations.
 * As with the methods from {@link Transactions} the atomicity provided here is of the weak type.
 *
 * The methods in this class may be used in three different ways.
 *
 * The simplest form is using the
 * {@link #longTransaction(RunnableWithException)} to wrap individual transactional operations in a transaction.
 * The operations themselves must be defined using the {@link #transactionally(FunctionalTransaction)} method. The main
 * body of each operation will be performed immediately, but the commit and rollback parts will be performed only when
 * the long transaction is comitted/rolled back.
 *
 * Another possibility is to manually control the transaction using {@link #beginTx()}, {@link #commitTx()} and {@link #rollbackTx()}.
 * These methods make it easier to access variables defined during the transaction after the transaction finishes.
 * Notice that these methods are bound to the current thread, so if you use multithreading (e.g. parallel streams)
 * within the transaction, it will not work. Transactional operations are again defined using the
 * {@link #transactionally(FunctionalTransaction)} method.
 *
 * The third way to control a transaction is to use the {@link LongTransaction} object returned by {@link #beginTx()}.
 * This object has the {@link LongTransaction#commit()} and {@link LongTransaction#rollback()} methods. The transactional
 * operations are added using the {@link LongTransaction#transactionally(FunctionalTransaction)} method. This way of explicitly
 * referencing the transaction allows multithreaded code to be run within a transaction. Notice that {@link LongTransaction}
 * implements {@link AutoCloseable} which means that the transaction will be committed unless a rollback has been called
 * before.
 *
 * @see Transactions
 */
public class LongTransactions {
    private static ThreadLocal<LongTransaction> longTransactions = new ThreadLocal<>();

    /**
     * Defines the transaction boundaries.
     *
     * Before executing the supplied runnable, a transaction will be started and will be comitted in the end, unless an exception is thrown.
     * If an exception is thrown by the runnable, the transaction will be rolled back.
     *
     * Individual operations are defined using the method {@link #transactionally(FunctionalTransaction)}.
     *
     * @param r the runnable to execute in a transaction
     */
    public static void longTransaction(RunnableWithException r) {
        try {
            LongTransaction tx = beginTx();
            r.run();
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
    }

    /**
     * Starts a {@link LongTransaction} and binds it to the current thread.
     *
     * @return a new {@link LongTransaction}
     */
    public static LongTransaction beginTx() {
//        if (longTransactions.get() != null) throw new IllegalStateException("Cannot begin a new transaction: a transaction is already active.");
        LongTransaction tx = new LongTransaction();
        longTransactions.set(tx);
        return tx;
    }

    /**
     * Commits the {@link LongTransaction} bound to the current thread. All transactional operations defined with
     * {@link #transactionally(FunctionalTransaction)} will be committed.
     */
    public static void commitTx() {
        getLongTransaction().commit();
        longTransactions.set(null);
    }

    /**
     * Rolls back the {@link LongTransaction} bound to the current thread. All transactional operations defined with
     * {@link #transactionally(FunctionalTransaction)} will be rolled back.
     */
    public static void rollbackTx() {
        getLongTransaction().rollback();
        longTransactions.set(null);
    }

    /**
     * Adds a transactional operation the the transaction bound to the current thread.
     * The body of the operation and the verification will be performed immediately, but the commit or rollback parts will
     * be performed only when the {@link LongTransaction} is committed/rolled back.
     *
     * @param operation
     * @param <T>
     * @return
     */
    public static <T> T transactionally(FunctionalTransaction<T> operation) {
        LongTransaction tx = getLongTransaction();
        return tx.transactionally(operation);
    }

    private static LongTransaction getLongTransaction() {
        LongTransaction tx = longTransactions.get();
        if (tx == null) throw new IllegalStateException("You need to perform this within a transaction. Use longTransaction() or beginTx()");
        return tx;
    }
}
