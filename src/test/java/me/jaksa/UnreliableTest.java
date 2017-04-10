package me.jaksa;

import org.junit.Assert;
import org.junit.Test;

public class UnreliableTest {

    @Test
    public void testTryingTwice() throws Exception {
        UnreliableService service = new UnreliableService(2);

        Unreliable.tenaciusly(() -> service.doSomething());

        Assert.assertTrue(service.success);
        Assert.assertEquals(2, service.tries);
    }


    @Test
    public void testTryingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        Unreliable.tenaciusly(() -> service.doSomething(), 5);

        Assert.assertTrue(service.success);
        Assert.assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testTryingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        Unreliable.tenaciusly(() -> service.doSomething(), 3);
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

        String result = Unreliable.tenaciusly(() -> service.getSomething());

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(2, service.tries);
    }


    @Test
    public void testGettingFiveTimes() throws Exception {
        UnreliableService service = new UnreliableService(5);

        String result = Unreliable.tenaciusly(() -> service.getSomething(), 5);

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(5, service.tries);
    }


    @Test(expected = RuntimeException.class)
    public void testGettingThreeTimesAndFailing() throws Exception {
        UnreliableService service = new UnreliableService(4);

        Unreliable.tenaciusly(() -> service.getSomething(), 3);
    }


    @Test
    public void testGetting100Times() {
        UnreliableService service = new UnreliableService(100);

        String result = Unreliable.keepTrying(() -> service.getSomething());

        Assert.assertEquals("Success!", result);
        Assert.assertTrue(service.success);
        Assert.assertEquals(100, service.tries);
    }


    @Test
    public void testExample() throws Exception {
        UnreliableService unreliableService = new UnreliableService();

        // this will try to invoke the service 5 times
        Unreliable.tenaciusly(() -> unreliableService.doSomething(), 5);

        // the default number of tries is 3
        Unreliable.tenaciusly(() -> unreliableService.doSomething());

        // you can also retrieve values
        String result = Unreliable.tenaciusly(() -> unreliableService.getSomething());
        System.out.println(result);

        // if after X times it still fails, a RuntimeException will be thrown
        try {
            Unreliable.tenaciusly(() -> unreliableService.doSomething(), 2);
        } catch (RuntimeException e) {
            System.out.println("I got tired of trying. Last exception: " + e.getCause().getMessage());
        }

        // or keep trying an infinite number of times
        Unreliable.keepTrying(() -> unreliableService.doSomething());
    }
}