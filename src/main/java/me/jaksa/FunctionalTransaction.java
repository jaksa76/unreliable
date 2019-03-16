package me.jaksa;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A lightweight transaction that returns a value. The {@link FunctionalTransaction} can have possible
 * side effects and thus can have rollback and commit operations.
 *
 * @see VoidTransaction
 */
public class FunctionalTransaction<T> {
    private SupplierWithException<T> function; // used only if there is no chaining
    private Consumer<T> rollback;
    private Runnable rollbackRunnable; // an alternative to rollback
    private Consumer<T> commit;
    private java.util.function.Function<T, Boolean> verification;
    private int retries = 3;
    private T lastResult;

    // chaining support
    private FunctionWithException<?, T> functionWithArgument; // used instead of function
    FunctionalTransaction<?> previous;
    Object resultOfPrevious;

    /**
     * Constructs a lightweight transaction that can have a rollback and a commit.
     *
     * @param function the function to evaluate.
     */
    public FunctionalTransaction(SupplierWithException<T> function) {
        this.function = function;
        this.rollback = result -> {};
        rollbackRunnable = () -> {};
        this.commit = result -> {};
        this.verification = result -> true;
    }


    /**
     * Constructs a lightweight transaction chained to a previous one.
     *
     * @param <I> the input type of the function
     * @param function the function to evaluate.
     * @param previous the {@link FunctionalTransaction} to attach to
     */
    public <I> FunctionalTransaction(FunctionWithException<I, T> function, FunctionalTransaction<I> previous) {
        this.functionWithArgument = function;
        this.previous = previous;
        this.rollback = result -> {};
        rollbackRunnable = () -> {};
        this.commit = result -> {};
        this.verification = result -> true;
    }

    /**
     * Alias for {@link #withRollback(Consumer)}
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any side effects of the transaction
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> withReset(Consumer<T> rollback) {
        return withRollback(rollback);
    }


    /**
     * Specify the rollback for this function. Rollback will be performed after every unsuccessful attempt.
     * The rollback should never throw an exception. The rollback will receive null if an exception has been
     * thrown or the last value if the function failed during verification.
     *
     * @param rollback the {@link Consumer} to perform in order to rollback any side effects of the transaction
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> withRollback(Consumer<T> rollback) {
        this.rollback = rollback;
        return this;
    }


    /**
     * Specify the rollback for this function. Rollback will be performed after every unsuccessful attempt.
     * The rollback should never throw an exception.
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any side effects of the transaction
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> withRollback(Runnable rollback) {
        this.rollbackRunnable = rollback;
        return this;
    }


    /**
     * Specify the commit for this function. Commit will be performed only after a successful attempt.
     * The commit should never throw an exception.
     *
     * @param commit the {@link Consumer} to perform in order to commit any side effects of this transaction
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> withCommit(Consumer<T> commit) {
        this.commit = commit;
        return this;
    }

    /**
     * Specify the verification for this function. Verification will be run to assess whether an attempt
     * has been successful. If the function threw an Exception, verification is not necessary.
     *
     * @param verification - a {@link Supplier} that returns false if there has been an error
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> withVerification(java.util.function.Function<T, Boolean> verification) {
        this.verification = verification;
        return this;
    }


    /**
     * Specify the number of retires for the evaluation of this function.
     *
     * @param times the number of times this transaction should be retried before giving up
     * @return the transaction for chaining other invocations
     */
    public FunctionalTransaction<T> retry(int times) {
        this.retries = times;
        return this;
    }


    /**
     * Creates another transaction and chains it to this one.
     *
     * @param function a function that takes the output of this transaction and returns a new value
     * @param <R> the return type of the new transaction
     * @return a new lightweight transaction chained to this one
     */
    public <R> FunctionalTransaction<R> then(FunctionWithException<T, R> function) {
        return (FunctionalTransaction<R>) new FunctionalTransaction(function, this);
    }


    /**
     * Creates another transaction and chains it to this one preserving the intermediate result.
     * The newly created transaction returns a {@link Pair} conatining the result of this transaction
     * and the result of the function passed.
     *
     * @param f a function that takes the output of this transaction and returns a new value
     * @param <R> the return type of the new transaction
     * @return a new lightweight transaction chained to this one
     */
    public <R> FunctionalTransaction<Pair<T, R>> and(FunctionWithException<T, R> f) {
        return then(t -> new Pair(t, f.apply(t)));
    }


    int getRetries() {
        return retries;
    }


    void performRollback() {
        rollback.accept(lastResult);
        rollbackRunnable.run();
    }


    void performCommit() {
        commit.accept(lastResult);
    }


    T evaluate() throws Exception {
        lastResult = null;
        lastResult = previous == null ? function.get() : functionWithArgument.apply(getResultOfPrevious());
        if (!verification.apply(lastResult)) throw new RuntimeException("Verification failed.");
        return lastResult;
    }


    <T> T getResultOfPrevious() {
        return (T) resultOfPrevious;
    }
}
