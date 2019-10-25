package me.jaksa;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static me.jaksa.Unreliable.keepTrying;
import static me.jaksa.Unreliable.retryOn;
import static me.jaksa.Unreliable.tenaciously;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnreliableTest {

    @Test
    public void testTryingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        tenaciously(() -> service.doSomething());

        assertTrue(service.success);
        assertEquals(2, service.tries);
    }


    @Test
    public void testTryingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        tenaciously(() -> service.doSomething(), 5);

        assertTrue(service.success);
        assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testTryingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        tenaciously(() -> service.doSomething(), 3);
    }


    @Test
    public void testTrying100Times() {
        UnreliableService service = new UnreliableService(100);

        keepTrying(() -> service.doSomething());

        assertTrue(service.success);
        assertEquals(100, service.tries);
    }


    @Test
    public void testGettingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        String result = tenaciously(() -> service.getSomething());

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(2, service.tries);
    }


    @Test
    public void testGettingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        String result = tenaciously(() -> service.getSomething(), 5);

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testGettingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        tenaciously(() -> service.getSomething(), 3);
    }


    @Test
    public void testGetting100Times() {
        UnreliableService service = new UnreliableService(100);

        String result = keepTrying(() -> service.getSomething());

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(100, service.tries);
    }


    @Test
    public void testRetryingOnSpecificException() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 3);

        String result = retryOn(IOException.class, () -> service.getSomething());

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(3, service.tries);
    }


    @Test
    public void testRetryingOnSpecificExceptionAndFail() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 4);
        try {
            retryOn(IOException.class, () -> service.doSomething());
            fail("should have thrown a wrapped FileNotFoundException");
        } catch (RuntimeException e) {
            assertEquals(FileNotFoundException.class, e.getCause().getClass());
            assertEquals(3, service.tries);
        }
    }


    @Test
    public void testThrowingDifferentException() {
        UnreliableService service = new UnreliableService(new NullPointerException(), 4);
        try {
            retryOn(IOException.class, () -> service.doSomething());
            fail("should have thrown a NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(1, service.tries);
        }
    }

    @Test
    public void testRetryingOnOneOfSpecifiedExceptions() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 10);

        List<Class> ignoredExceptions = asList(NullPointerException.class, IOException.class);
        retryOn(ignoredExceptions, () -> service.doSomething(), 10);

        assertTrue(service.success);
        assertEquals(10, service.tries);
    }

}