package me.jaksa;

import java.util.ArrayList;
import java.util.List;

class LongTransaction implements AutoCloseable {
    private final List<FunctionalTransaction> steps = new ArrayList<>();

    // TODO add state management and checks for correct usage

    public <T> void addStep(FunctionalTransaction<T> transaction) {
        steps.add(transaction);
    }

    public void commit() {
        for (FunctionalTransaction step : steps) {
            Transactions.commitAll(step);
        }
        steps.clear();
    }

    public void rollback() {
        for (FunctionalTransaction step : steps) {
            Transactions.rollbackAll(step);
        }
        steps.clear();
    }

    @Override
    public void close() throws Exception {
        commit();
    }
}
