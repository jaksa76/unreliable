package me.jaksa;

import java.util.function.Supplier;

/**
 * Represents operations that can have an associated rollback, commit or custom verification.
 */
public class Operation {
    private RunnableWithException operation;
    private Runnable rollback;
    private Runnable commit;
    private Supplier<Boolean> verification;
    private int retries = 3;

    /**
     * Constructs a potentially unreliable operation that can have a rollback and a commit.
     *
     * @param operation the operation to perform.
     */
    public Operation(RunnableWithException operation) {
        this.operation = operation;
        this.rollback = () -> {};
        this.commit = () -> {};
        this.verification = () -> true;
    }

    /**
     * Alias for {@link #withRollback(Runnable)}
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any changes from the operation
     * @return the operation for chaining other invocations
     */
    public Operation withReset(Runnable rollback) {
        return withRollback(rollback);
    }


    /**
     * Specify the rollback for this operation. Rollback will be performed after every unsuccessful attempt.
     * The rollback should never throw an exception.
     *
     * @param rollback the {@link Runnable} to perform in order to rollback any changes from the operation
     * @return the operation for chaining other invocations
     */
    public Operation withRollback(Runnable rollback) {
        this.rollback = rollback;
        return this;
    }


    /**
     * Specify the commit for this operation. Commit will be performed only after a successful attempt.
     * The commit should never throw an exception.
     *
     * @param commit the {@link Runnable} to perform in order to commit any changes from the operation
     * @return the operation for chaining other invocations
     */
    public Operation withCommit(Runnable commit) {
        this.commit = commit;
        return this;
    }


    /**
     * Specify the verification for this operation. Verification will be run to assess whether an attempt
     * has been successful. If the operation threw an Exception, verification is not necessary.
     *
     * @param verification - a {@link Supplier} that returns false if there has been an error
     * @return the operation for chaining other invocations
     */
    public Operation withVerification(Supplier<Boolean> verification) {
        this.verification = verification;
        return this;
    }


    /**
     * Specify the number of retires for this operation.
     *
     * @param times the number of times this operation should be retried before giving up
     * @return the operation for chaining other invocations
     */
    public Operation retry(int times) {
        this.retries = times;
        return this;
    }


    int getRetries() {
        return retries;
    }


    void performRollback() {
        rollback.run();
    }


    void performCommit() {
        commit.run();
    }


    void run() throws Exception {
        operation.run();
        if (!verification.get()) throw new RuntimeException("Verification failed.");
    }
}
