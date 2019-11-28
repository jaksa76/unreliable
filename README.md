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
import me.jaksa.Unreliable.*;
```

```java
        // this will try to invoke the service 5 times
        Unreliable.tenaciously(() -> unreliableService.doSomething(), 5);

        // the default number of tries is 3
        Unreliable.tenaciously(() -> unreliableService.doSomething());

        // you can also retrieve values
        String result = Unreliable.tenaciously(() -> unreliableService.getSomething());
        System.out.println(result);

        // if after X times it still fails, a RuntimeException will be thrown
        try {
            Unreliable.tenaciously(() -> unreliableService.doSomething(), 2);
        } catch (RuntimeException e) {
            System.out.println("I got tired of trying. Last exception: " + e.getCause().getMessage());
        }

        // or keep trying an infinite number of times
        keepTrying(() -> unreliableService.doSomething());
        
        // ignore only certain exceptions
        Unreliable.retryOn(SocketTimeoutException.class, () -> unreliableService.doSomething());
```


## Lightweight Transactions

Lightweight Transactions have moved into a separate project on https://github.com/jaksa76/ltx