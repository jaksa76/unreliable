# Unreliable
A set of utility methods to deal with unreliable components.

# Guide

## Maven, Gradle, etc.

Maven:
```xml
<dependency>
    <groupId>me.jaksa</groupId>
    <artifactId>unreliable</artifactId>
    <version>1.0</version>
</dependency>
```

Gradle:
```groovy
compile group: 'me.jaksa', name: 'unreliable', version: '1.0'
```

## Basics

```java
import me.jaksa.Unreliable.*;
```

```java
        // this will try to invoke the service 5 times
        tenaciusly(() -> unreliableService.doSomething(), 5);

        // the default number of tries is 3
        tenaciusly(() -> unreliableService.doSomething());

        // you can also retrieve values
        String result = tenaciusly(() -> unreliableService.getSomething());
        System.out.println(result);

        // if after X times it still fails, a RuntimeException will be thrown
        try {
            tenaciusly(() -> unreliableService.doSomething(), 2);
        } catch (RuntimeException e) {
            System.out.println("I got tired of trying. Last exception: " + e.getCause().getMessage());
        }

        // or keep trying an infinite number of times
        keepTrying(() -> unreliableService.doSomething());
```


## Lightweight Transactions

The unreliable library also provides lightweight transactions. Unreliable's lightweight 
transactions **don't** provide full ACID semantics. The methods in this class allow weak atomicity:
either all or no transactions will be performed as long as the process doesn't crash. 
The user must provide rollback and/or commit mechanisms.
For critical applications where preserving atomicity even
in crash scenarios is more important than performance, you should use a full transaction manager.
Consistency, Isolation and Durability must be implemented by the user.

```java
import me.jaksa.Transactions.*;
```

```java
        // there are two types of transactions
        atomically(perform(() -> service.doSomething())); // void ones
        String result = atomically(evaluate(() -> service.getSomething())); // ones with a result

        // we can specify a rollback function which will be invoked in case of an exception
        Room room = atomically(evaluate(() -> unreliableHotel.bookRoom(getDates()))
                .withRollback(() -> unreliableHotel.cancelAllBookings())
                .retry(5)); // and the number of times to retry before giving up

        // we can also specify a verification function for when we don't rely on exceptions
        // in that case we can also use the variant of the rollback function that accepts the produced value
        room = atomically(evaluate(() -> reliableHotel.bookRoom(getDates()))
                .withVerification(r -> r.price() < maxPrice)
                .withRollback(r -> reliableHotel.cancelBooking(r)));

        // alternatively we can specify a commit
        Flight flight = atomically(evaluate(() -> airline.reserveFlight(getDates()))
                .withCommit(f -> airline.confirmReservation(f)));

        // we can also chain transactions
        Itinerary itinerary = atomically(evaluate(() -> unreliableHotel.bookRoom(getDates()))
                .then(r -> new Itinerary(r, airline.reserveFlight(r.getDates()))));

        // we can also chain transactions using the 'and' function which wraps the results
        Pair<Room, Flight> itinerary2 = atomically(evaluate(() -> unreliableHotel.bookRoom(getDates()))
                .and(r -> airline.reserveFlight(r.getDates())));
```
