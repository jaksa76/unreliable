package me.jaksa;

import me.jaksa.namedparameters.Param;

import java.util.Arrays;
import java.util.stream.Stream;

import static me.jaksa.Unreliable.RetryParam.Names.*;
import static me.jaksa.namedparameters.Params.getParam;
import static me.jaksa.namedparameters.Params.param;

/**
 * Utility methods to make it easier to work with unreliable components
 */
public class Unreliable {

    public static final int DEFAULT_RETRIES = 3;

    public static class RetryParam<T> extends Param<T> {
        private RetryParam(Names k, T v) { super(k, v); }
        enum Names {ON_EXCEPTION, TIMES, INTERVAL, INFINITELY}
        public static RetryParam infinitely() { return new RetryParam(INFINITELY, true); }
        public static RetryParam times(int t) { return new RetryParam(TIMES, t); }
        public static RetryParam interval(int i) { return new RetryParam(INTERVAL, i); }
        public static RetryParam on(Class<? extends Throwable>... exceptionClasses) { return new RetryParam(ON_EXCEPTION, exceptionClasses); }
    }

    public static void retry(RunnableWithException r, RetryParam... additionalParams) {
        retry(toSupplier(r), additionalParams);
    }

    public static <T> T retry(SupplierWithException<T> s, RetryParam... additionalParams) {
        boolean infinitely = getParam(additionalParams, INFINITELY, false);
        int times = getParam(additionalParams, TIMES, DEFAULT_RETRIES);
        int interval = getParam(additionalParams, INTERVAL, 0);
        Class<Exception>[] exceptionClasses = getParam(additionalParams, ON_EXCEPTION, new Class[] { Exception.class });

        boolean success = true;
        int tries = 0;
        Exception lastException = null;
        do {
            try {
                if (!infinitely) tries++;
                return s.get();
            } catch (Exception e) {
                Stream<Class<Exception>> stream = Arrays.stream(exceptionClasses);
                if (stream.noneMatch(ex -> ex.isAssignableFrom(e.getClass()))) throwOrWrap(e);
                success = false;
                lastException = e;
            }

            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ie) { }
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
