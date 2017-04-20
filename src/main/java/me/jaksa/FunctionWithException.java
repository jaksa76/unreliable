package me.jaksa;

/**
 * A variant of the {@link java.util.function.Function} interface that throws an exception.
 */
public interface FunctionWithException<T, R> {
    public R apply(T t) throws Exception;
}
