package me.jaksa;

import org.junit.Test;

import java.net.SocketTimeoutException;

import static me.jaksa.Unreliable.RetryParam.*;
import static me.jaksa.Unreliable.retry;

public class Examples {

    @Test
    public void unreliable() throws Exception {
        UnreliableService unreliableService = new UnreliableService();

        // this will try to invoke the service 5 times
        retry(() -> unreliableService.doSomething(), times(5));

        // this will wait 100ms between retries
        retry(() -> unreliableService.doSomething(), times(2), interval(100));

        // the default number of tries is 3
        retry(() -> unreliableService.doSomething());

        // you can also retrieve values
        String result = retry(() -> unreliableService.getSomething());
        System.out.println(result);

        // if after X times it still fails, a RuntimeException will be thrown
        try {
            retry(() -> unreliableService.doSomething(), times(2));
        } catch (RuntimeException e) {
            System.out.println("I got tired of trying. Last exception: " + e.getCause().getMessage());
        }

        // or keep trying an infinite number of times
        retry(() -> unreliableService.doSomething(), infinitely());

        // ignore only certain exceptions
        retry(() -> unreliableService.doSomething(), on(SocketTimeoutException.class));
    }
}
