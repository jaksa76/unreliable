package me.jaksa;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A function that returns a value. The function can have possible side effects and thus can have
 * rollbacks and commits.
 */
public class Function<T> {
    private SupplierWithException<T> function;
    private Consumer<T> rollback;
    private Consumer<T> commit;
    private java.util.function.Function<T, Boolean> verification;
    private int retries = 3;
    private T lastResult;
    private Runnable rollbackRunnable;

    /**
     * Constructs a potentially unreliable function that can have a rollback and a commit.
     *
     * @param function the function to evaluate.
     */
    public Function(SupplierWithException<T> function) {
        this.function = function;
        this.rollback = result -> {};
        rollbackRunnable = () -> {};
        this.commit = result -> {};
        this.verification = result -> true;
    }

    /**
     * Alias for {@link #withRollback(Consumer)}
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any side effects of the function
     * @return the function for chaining other invocations
     */
    public Function<T> withReset(Consumer<T> rollback) {
        return withRollback(rollback);
    }


    /**
     * Specify the rollback for this function. Rollback will be performed after every unsuccessful attempt.
     * The rollback should never throw an exception. The rollback will receive null if an exception has been
     * thrown or the last value if the function failed during verification.
     *
     * @param rollback the {@link Consumer} to perform in order to rollback any side effects of the function
     * @return the function for chaining other invocations
     */
    public Function<T> withRollback(Consumer<T> rollback) {
        this.rollback = rollback;
        return this;
    }


    /**
     * Specify the rollback for this function. Rollback will be performed after every unsuccessful attempt.
     * The rollback should never throw an exception.
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any side effects of the function
     * @return the function for chaining other invocations
     */
    public Function<T> withRollback(Runnable rollback) {
        this.rollbackRunnable = rollback;
        return this;
    }


    /**
     * Specify the commit for this function. Commit will be performed only after a successful attempt.
     * The commit should never throw an exception.
     *
     * @param commit the {@link Consumer} to perform in order to commit any side effects of this function
     * @return the function for chaining other invocations
     */
    public Function<T> withCommit(Consumer<T> commit) {
        this.commit = commit;
        return this;
    }

    /**
     * Specify the verification for this function. Verification will be run to assess whether an attempt
     * has been successful. If the function threw an Exception, verification is not necessary.
     *
     * @param verification - a {@link Supplier} that returns false if there has been an error
     * @return the function for chaining other invocations
     */
    public Function<T> withVerification(java.util.function.Function<T, Boolean> verification) {
        this.verification = verification;
        return this;
    }


    /**
     * Specify the number of retires for the evaluation of this function.
     *
     * @param times the number of times this function should be retried before giving up
     * @return the function for chaining other invocations
     */
    public Function<T> retry(int times) {
        this.retries = times;
        return this;
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
        lastResult = function.get();
        if (!verification.apply(lastResult)) throw new RuntimeException("Verification failed.");
        return lastResult;
    }

}