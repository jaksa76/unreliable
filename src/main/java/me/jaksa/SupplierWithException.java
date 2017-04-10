package me.jaksa;

/**
 * A variant of the {@link java.util.function.Supplier} interface that throws an exception.
 */
public interface SupplierWithException<T> {
    public T get() throws Exception;
}
