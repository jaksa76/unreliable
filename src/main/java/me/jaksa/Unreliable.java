package me.jaksa;

import java.util.function.Supplier;

/**
 * Utility methods to make it easier to work with unreliable components
 */
public class Unreliable {

    /**
     * Performs an action until no exception is thrown.
     *
     * @param r the action to perform
     */
    public static void keepTrying(RunnableWithException r) {
        keepTrying(() -> {
            r.run();
            return true; // return anything
        });
    }


    /**
     * Tries to retrieve a value until no exception is thrown.
     *
     * @param <T> the type of the return value
     *
     * @param s the function to evaluate
     *
     * @return the value retunred by the function
     */
    public static <T> T keepTrying(SupplierWithException<T> s) {
        boolean success = true;
        do {
            try {
                return s.get();
            } catch (Exception e) {
                success = false;
            }
        } while (!success);
        throw new IllegalStateException();
    }

    /**
     * Performs an action up to three times until no exception is thrown.
     *
     * @param r the action to perform
     *
     * @throws RuntimeException if the action fails three times.
     */
    public static void tenaciusly(RunnableWithException r) {
        tenaciusly(r, 3);
    }


    /**
     * Performs an action up to the specified number of times until no exception is thrown.
     *
     * @param r the action to perform
     *
     * @param times the number of times to retry
     *
     * @throws RuntimeException if the action fails the specified number of times.
     */
    public static void tenaciusly(RunnableWithException r, int times) {
        tenaciusly(() -> {
            r.run();
            return true; // return anything
        }, times);
    }


    /**
     * Tries to retrieve a value up to three times until no exception is thrown.
     *
     * @param <T> the type of the return value
     *
     * @param s the function to evaluate
     *
     * @return the value retunred by the function
     *
     * @throws RuntimeException if the function fails three times.
     */
    public static <T> T tenaciusly(SupplierWithException<T> s) {
        return tenaciusly(s, 3);
    }


    /**
     * Tries to retrieve a value up to the specified number of times until no exception is thrown.
     *
     * @param <T> the type of the return value
     *
     * @param s the function to evaluate
     *
     * @param times the number of times to retry
     *
     * @return the value retunred by the function
     *
     * @throws RuntimeException if the function fails the specified number of times.
     */
    public static <T> T tenaciusly(SupplierWithException<T> s, int times) {
        boolean success = true;
        int tries = 0;
        Exception lastException = null;
        do {
            try {
                tries++;
                return s.get();
            } catch (Exception e) {
                success = false;
                lastException = e;
            }
        } while (!success && tries < times);
        throw new RuntimeException("Tried " + tries + ", but failed: " + lastException.getMessage(), lastException);
    }
}
