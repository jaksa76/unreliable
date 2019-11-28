# Unreliable
A set of utility methods to deal with unreliable components.

# Guide

## Maven, Gradle, etc.

Maven:
```xml
<dependency>
    <groupId>me.jaksa</groupId>
    <artifactId>unreliable</artifactId>
    <version>2.1</version>
</dependency>
```

Gradle:
```groovy
compile group: 'me.jaksa', name: 'unreliable', version: '2.1'
```

## Basics

```java
import static me.jaksa.Unreliable.RetryParam.*;
import static me.jaksa.Unreliable.retry;
```

```java
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
```


## Lightweight Transactions

Lightweight Transactions have moved into a separate project on https://github.com/jaksa76/ltx