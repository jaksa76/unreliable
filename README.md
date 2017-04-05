# unreliable
A set of utility methods to deal with unreliable components.

# Guide

```java
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
```
