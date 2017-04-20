package me.jaksa;

import org.junit.Test;

import static me.jaksa.Transactions.perform;
import static me.jaksa.Transactions.atomically;
import static org.junit.Assert.*;

public class OperationTest {
    @Test
    public void testAllOperationsAreRolledBackOnFailure() throws Exception {
        String[] statuses = new String[] { "init", "init", "init" };

        try {
            atomically(
                    perform(() -> statuses[0] = "ready")
                            .withRollback(() -> statuses[0] = "rolled back")
                            .withCommit(() -> statuses[0] = "committed"),
                    perform(() -> statuses[1] = "ready")
                            .withRollback(() -> statuses[1] = "rolled back")
                            .withCommit(() -> statuses[1] = "committed"),
                    perform(() -> throwException())
                            .withRollback(() -> statuses[2] = "rolled back")
                            .withCommit(() -> statuses[2] = "committed")
            );

            fail("should have thrown an exception");
        } catch (Exception e) {
            // this is ok
        }

        assertEquals("rolled back", statuses[0]);
        assertEquals("rolled back", statuses[1]);
        assertEquals("rolled back", statuses[2]);
    }

    @Test
    public void testRollbackIsInvokedBeforeEveryAttempt() throws Exception {
        UnreliableService service = new UnreliableService();
        String[] statuses = new String[] { "init" };

        atomically(
                perform(() -> {
                    if (!statuses[0].equals("init")) throwException("not rolled back");
                    service.doSomething();
                    statuses[0] = "ready";
                }).withRollback(() -> statuses[0] = "init")
        );
    }

    @Test
    public void testVerification() throws Exception {
        int[] counters = new int[] { 0 };

        atomically(
                perform(() -> counters[0]++)
                .withVerification(() -> counters[0] > 2)
        );

        assertEquals(3, counters[0]);
    }

    @Test
    public void testCommitIsInvokedOnlyAtTheEnd() throws Exception {
        UnreliableService service1 = new UnreliableService();
        UnreliableService service2 = new UnreliableService();
        UnreliableService service3 = new UnreliableService();

        int[] counters = new int[] { 0, 0, 0 };

        atomically(
                perform(() -> service1.doSomething())
                        .withVerification(() -> counters[0] + counters[1] + counters[2] == 0)
                        .withCommit(() -> counters[0]++),
                perform(() -> service2.doSomething())
                        .withVerification(() -> counters[0] + counters[1] + counters[2] == 0)
                        .withCommit(() -> counters[1]++),
                perform(() -> service3.doSomething())
                        .withVerification(() -> counters[0] + counters[1] + counters[2] == 0)
                        .withCommit(() -> counters[2]++)
        );
    }


    @Test
    public void testSpecifyingNumberOfRetries() throws Exception {
        UnreliableService service = new UnreliableService(10);

        int[] counters = new int[] { 0 };

        try {
            atomically(
                    perform(() -> {
                        counters[0]++;
                        service.doSomething();
                    }).retry(6));
            fail("should have thrown an exception");
        } catch (Exception e) {
            // this is ok
        }

        assertEquals(6, counters[0]);
    }

    @Test
    public void testSpecifyingDifferentRetires() throws Exception {
        UnreliableService service1 = new UnreliableService(2);
        UnreliableService service2 = new UnreliableService(3);
        UnreliableService service3 = new UnreliableService(4);

        int[] counters = new int[] { 0, 0, 0 };

        atomically(
                perform(() -> {
                    counters[0]++;
                    service1.doSomething();
                }).retry(3),
                perform(() -> {
                    counters[1]++;
                    service2.doSomething();
                }).retry(4),
                perform(() -> {
                    counters[2]++;
                    service3.doSomething();
                }).retry(5)
        );

        assertEquals(2, counters[0]);
        assertEquals(3, counters[1]);
        assertEquals(4, counters[2]);
    }

    private void throwException() {
        throwException("Boom");
    }

    private void throwException(String msg) {
        throw new RuntimeException(msg);
    }
}