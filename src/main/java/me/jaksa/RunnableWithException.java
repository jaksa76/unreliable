package me.jaksa;

/**
 * A functional interface that acts as a variant of {@link Runnable} that throws an exception.
 */
@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
