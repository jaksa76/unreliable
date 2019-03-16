package me.jaksa;

import org.junit.Assert;
import org.junit.Test;

public class UnreliableTest {

    @Test
    public void testTryingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        Unreliable.tenaciously(() -> service.doSomething());

        Assert.assertTrue(service.success);
        Assert.assertEquals(2, service.tries);
    }


    @Test
    public void testTryingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        Unreliable.tenaciously(() -> service.doSomething(), 5);

        Assert.assertTrue(service.success);
        Assert.assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testTryingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        Unreliable.tenaciously(() -> service.doSomething(), 3);
    }


    @Test
    public void testTrying100Times() {
        UnreliableService service = new UnreliableService(100);

        Unreliable.keepTrying(() -> service.doSomething());

        Assert.assertTrue(service.success);
        Assert.assertEquals(100, service.tries);
    }


    @Test
    public void testGettingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        String result = Unreliable.tenaciously(() -> service.getSomething());

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(2, service.tries);
    }


    @Test
    public void testGettingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        String result = Unreliable.tenaciously(() -> service.getSomething(), 5);

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testGettingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        Unreliable.tenaciously(() -> service.getSomething(), 3);
    }


    @Test
    public void testGetting100Times() {
        UnreliableService service = new UnreliableService(100);

        String result = Unreliable.keepTrying(() -> service.getSomething());

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(100, service.tries);
    }
}