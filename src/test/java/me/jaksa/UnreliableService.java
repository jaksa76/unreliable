package me.jaksa;

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

    public void doSomething() {
        tries++;
        if (tries < timesForSuccess) throw new RuntimeException("Oops, I failed again.");

        success = true;
    }

    public String getSomething() {
        tries++;
        if (tries < timesForSuccess) throw new RuntimeException("Oops, I failed again.");

        success = true;
        return "Success!";
    }
}
