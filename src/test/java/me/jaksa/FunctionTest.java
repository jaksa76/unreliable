package me.jaksa;

import org.junit.Test;

import static me.jaksa.Transactions.atomically;
import static me.jaksa.Transactions.evaluate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FunctionTest {
    @Test
    public void testFunctionIsRolledBackOnFailure() throws Exception {
        String[] status = new String[] { "init" };

        try {
            atomically(evaluate(() -> {
                        status[0] = "ready";
                        return throwException();
                    }).withRollback(() -> status[0] = "rolled back")
                      .withCommit(r -> status[0] = "committed"));

            fail("should have thrown an exception");
        } catch (Exception e) {} // this is ok

        assertEquals("rolled back", status[0]);
    }


    @Test(expected = Exception.class)
    public void testRollbackWithArgument() throws Exception {
            atomically(evaluate(() -> "failure")
                            .withVerification(result -> "success".equals(result))
                            .withRollback(result -> assertEquals(result, "failure")));
    }


    @Test
    public void testRollbackIsInvokedBeforeEveryAttempt() throws Exception {
        UnreliableService service = new UnreliableService();
        String[] statuses = new String[] { "init" };

        atomically(evaluate(() -> {
                    if (!statuses[0].equals("init")) throwException("not rolled back");
                    service.doSomething();
                    statuses[0] = "ready";
                    return true;
                }).withRollback(() -> statuses[0] = "init"));
    }


    @Test
    public void testVerification() throws Exception {
        int[] counters = new int[] { 0 };

        atomically(evaluate(() -> counters[0]++)
                .withVerification(result -> counters[0] > 2));

        assertEquals(3, counters[0]);
    }


    @Test
    public void testCommitIsInvokedOnlyAtTheEnd() throws Exception {
        UnreliableService service1 = new UnreliableService();

        int[] counters = new int[] { 0 };

        String result = atomically(evaluate(() -> service1.getSomething())
                        .withVerification(r -> counters[0] == 0)
                        .withCommit(r -> counters[0]++));

        assertEquals(1, counters[0]);
        assertEquals("Success!", result);
    }


    @Test
    public void testSpecifyingNumberOfRetries() throws Exception {
        UnreliableService service = new UnreliableService(10);

        int[] counters = new int[] { 0 };

        try {
            atomically(evaluate(() -> {
                        counters[0]++;
                        return service.getSomething();
                    }).retry(6));
            fail("should have thrown an exception");
        } catch (Exception e) {} // this is ok

        assertEquals(6, counters[0]);
    }


    private boolean throwException() {
        return throwException("Boom");
    }

    private boolean throwException(String msg) {
        throw new RuntimeException(msg);
    }
}