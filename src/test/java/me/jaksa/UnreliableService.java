package me.jaksa;

import java.io.IOException;

public class UnreliableService {

    int tries;
    private int timesForSuccess;
    boolean success = false;
    private Exception exception;

    public UnreliableService() {
        this(2);
    }

    public UnreliableService(int timesForSuccess) {
        this(new RuntimeException("Oops, I failed again."), timesForSuccess);
    }
    public UnreliableService(Exception e, int timesForSuccess) {
        this.timesForSuccess = timesForSuccess;
        this.exception = e;
    }

    public void doSomething() throws Exception {
        tries++;
        if (tries < timesForSuccess) throw exception;

        success = true;
    }

    public String getSomething() throws Exception {
        tries++;
        if (tries < timesForSuccess) throw exception;

        success = true;
        return "Success!";
    }
}
