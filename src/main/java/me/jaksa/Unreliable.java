package me.jaksa;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility methods to make it easier to work with unreliable components
 */
public class Unreliable {

    public static final int DEFAILT_RETRIES = 3;

    /**
     * Performs an action until no exception is thrown.
     *
     * @param r the action to perform
     */
    public static void keepTrying(RunnableWithException r) {
        keepTrying(toSupplier(r));
    }


    /**
     * Tries to retrieve a value until no exception is thrown.
     *
     * @param <T> the type of the return value
     * @param s   the function to evaluate
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
     * @throws RuntimeException if the action fails three times.
     */
    public static void tenaciously(RunnableWithException r) {
        tenaciously(r, DEFAILT_RETRIES);
    }


    /**
     * Performs an action up to the specified number of times until no exception is thrown.
     *
     * @param r     the action to perform
     * @param times the number of times to retry
     * @throws RuntimeException if the action fails the specified number of times.
     */
    public static void tenaciously(RunnableWithException r, int times) {
        tenaciously(toSupplier(r), times);
    }


    /**
     * Tries to retrieve a value up to three times until no exception is thrown.
     *
     * @param <T> the type of the return value
     * @param s   the function to evaluate
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails three times.
     */
    public static <T> T tenaciously(SupplierWithException<T> s) {
        return tenaciously(s, DEFAILT_RETRIES);
    }


    /**
     * Tries to retrieve a value up to the specified number of times until no exception is thrown.
     *
     * @param <T>   the type of the return value
     * @param s     the function to evaluate
     * @param times the number of times to retry
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails the specified number of times.
     */
    public static <T> T tenaciously(SupplierWithException<T> s, int times) {
        return retryOn(Exception.class, s, times);
    }

    /**
     * Tries to execute the supplied runnable up to three times as long as the specified exception is being thrown.
     *
     * @param exceptionClass retry only on this class of exceptions
     * @param r              the function to evaluate
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static void retryOn(Class exceptionClass, RunnableWithException r) {
        retryOn(exceptionClass, toSupplier(r));
    }

    /**
     * Tries to retrieve a value up to three times as long as the specified exception is being thrown.
     *
     * @param <T>            the type of the return value
     * @param exceptionClass retry only on this class of exceptions
     * @param s              the function to evaluate
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static <T> T retryOn(Class exceptionClass, SupplierWithException<T> s) {
        return retryOn(exceptionClass, s, DEFAILT_RETRIES);
    }


    /**
     * Tries to execute the supplied runnable up to the specified number of times as long as the specified exception is being thrown.
     *
     * @param exceptionClass retry only on this class of exceptions
     * @param r              the function to evaluate
     * @param times          the number of times to retry
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static void retryOn(Class exceptionClass, RunnableWithException r, int times) {
        retryOn(exceptionClass, toSupplier(r), times);
    }


    /**
     * Tries to retrieve a value up to the specified number of times as long as the specified exception is being thrown.
     *
     * @param <T>            the type of the return value
     * @param exceptionClass retry only on this class of exceptions
     * @param s              the function to evaluate
     * @param times          the maximum number of times to retry
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static <T> T retryOn(Class exceptionClass, SupplierWithException<T> s, int times) {
        return retryOn(Collections.singletonList(exceptionClass), s, times);
    }


    /**
     * Tries to execute the runnable up to three times as long as one of the specified exceptions is being thrown.
     *
     * @param exceptionClasses retry on any of these classes of exceptions
     * @param r                the function to evaluate
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static void retryOn(Collection<Class> exceptionClasses, RunnableWithException r) {
        retryOn(exceptionClasses, toSupplier(r), DEFAILT_RETRIES);
    }


    /**
     * Tries to execute the runnable up to the specified number of times as long as one of the specified exceptions is being thrown.
     *
     * @param exceptionClasses retry on any of these classes of exceptions
     * @param r                the function to evaluate
     * @param times            the maximum number of times to retry
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static void retryOn(Collection<Class> exceptionClasses, RunnableWithException r, int times) {
        retryOn(exceptionClasses, toSupplier(r), times);
    }


    /**
     * Tries to retrieve a value up to three times as long as one of the specified exceptions is being thrown.
     *
     * @param <T>              the type of the return value
     * @param exceptionClasses retry on any of these classes of exceptions
     * @param s                the function to evaluate
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static <T> T retryOn(Collection<Class> exceptionClasses, SupplierWithException<T> s) {
        return retryOn(exceptionClasses, s, DEFAILT_RETRIES);
    }


    /**
     * Tries to retrieve a value up to the specified number of times as long as one of the specified exceptions is being thrown.
     *
     * @param <T>              the type of the return value
     * @param exceptionClasses retry on any of these classes of exceptions
     * @param s                the function to evaluate
     * @param times            the maximum number of times to retry
     * @return the value retunred by the function
     * @throws RuntimeException if the function fails the specified number of times or a different exception is thrown.
     */
    public static <T> T retryOn(Collection<Class> exceptionClasses, SupplierWithException<T> s, int times) {
        boolean success = true;
        int tries = 0;
        Exception lastException = null;
        do {
            try {
                tries++;
                return s.get();
            } catch (Exception e) {
                if (exceptionClasses.stream().noneMatch(ex -> ex.isAssignableFrom(e.getClass()))) throwOrWrap(e);
                success = false;
                lastException = e;
            }
        } while (!success && tries < times);
        throw new RuntimeException("Tried " + tries + " times, but failed: " + lastException.getMessage(), lastException);
    }

    private static void throwOrWrap(Exception e) {
        if (e instanceof RuntimeException) throw (RuntimeException) e;
        else throw new RuntimeException(e.getMessage(), e);
    }

    private static SupplierWithException<Boolean> toSupplier(RunnableWithException s) {
        return () -> {
            s.run();
            return true; // return anything
        };
    }
}
