package me.jaksa;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static me.jaksa.Unreliable.RetryParam.*;
import static me.jaksa.Unreliable.retry;
import static org.junit.Assert.*;

public class UnreliableTest {

    @Test
    public void testTryingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        retry(() -> service.doSomething());

        assertTrue(service.success);
        assertEquals(2, service.tries);
    }


    @Test
    public void testTryingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        retry(() -> service.doSomething(), times(5));

        assertTrue(service.success);
        assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testTryingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        retry(() -> service.doSomething(), times(3));
    }


    @Test
    public void testTrying100Times() {
        UnreliableService service = new UnreliableService(100);

        retry(() -> service.doSomething(), infinitely());

        assertTrue(service.success);
        assertEquals(100, service.tries);
    }


    @Test
    public void testGettingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        String result = retry(() -> service.getSomething());

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(2, service.tries);
    }


    @Test
    public void testGettingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        String result = retry(() -> service.getSomething(), times(5));

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testGettingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        retry(() -> service.getSomething(), times(3));
    }


    @Test
    public void testGetting100Times() {
        UnreliableService service = new UnreliableService(100);

        String result = retry(() -> service.getSomething(), infinitely());

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(100, service.tries);
    }


    @Test
    public void testRetryingOnSpecificException() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 3);

        String result = retry(() -> service.getSomething(), on(IOException.class));

        assertEquals("Success!", result);
        assertTrue(service.success);
        assertEquals(3, service.tries);
    }


    @Test
    public void testRetryingOnSpecificExceptionAndFail() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 4);
        try {
            retry(() -> service.doSomething(), on(IOException.class));
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
            retry(() -> service.doSomething(), on(IOException.class));
            fail("should have thrown a NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(1, service.tries);
        }
    }

    @Test
    public void testRetryingOnOneOfSpecifiedExceptions() {
        UnreliableService service = new UnreliableService(new FileNotFoundException(), 10);

        retry(() -> service.doSomething(), on(NullPointerException.class, IOException.class), times(10));

        assertTrue(service.success);
        assertEquals(10, service.tries);
    }

}