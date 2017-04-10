package me.jaksa;

import java.io.IOException;

public class UnreliableService {

    int tries;
    private int timesForSuccess;
    boolean success = false;

    public UnreliableService() {
        this(2);
    }

    public UnreliableService(int timesForSuccess) {
        this.timesForSuccess = timesForSuccess;
    }

    public void doSomething() throws Exception {
        tries++;
        if (tries < timesForSuccess) throw new RuntimeException("Oops, I failed again.");

        success = true;
    }

    public String getSomething() throws Exception {
        tries++;
        if (tries < timesForSuccess) throw new RuntimeException("Oops, I failed again.");

        success = true;
        return "Success!";
    }
}
