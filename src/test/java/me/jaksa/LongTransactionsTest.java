package me.jaksa;

import org.junit.Test;

import static me.jaksa.LongTransactions.longTransaction;
import static me.jaksa.LongTransactions.transactionally;
import static me.jaksa.Transactions.evaluate;
import static org.junit.Assert.*;

public class LongTransactionsTest {

    @Test
    public void testCommit() throws Exception {
        int[] counters = new int[3];

        longTransaction(() -> {
            transactionally(evaluate(() -> counters[0]++)
                    .withCommit(val -> counters[0]++));

            transactionally(evaluate(() -> counters[1]++)
                    .withCommit(val -> counters[1]++));

            transactionally(evaluate(() -> counters[2]++)
                    .withCommit(val -> counters[2]++));
        });

        assertEquals(2, counters[0]);
        assertEquals(2, counters[1]);
        assertEquals(2, counters[2]);
    }

    @Test
    public void testRollbackFromException() throws Exception {
        int[] counters = new int[3];

        longTransaction(() -> {
            transactionally(evaluate(() -> counters[0]++)
                    .withCommit(val -> counters[0]++)
                    .withRollback(val -> counters[0]=0));

            transactionally(evaluate(() -> counters[1]++)
                    .withCommit(val -> counters[1]++)
                    .withRollback(val -> counters[1]=0));

            throwException();

            transactionally(evaluate(() -> counters[2]++)
                    .withCommit(val -> counters[2]++));
        });

        assertEquals(0, counters[0]);
        assertEquals(0, counters[1]);
        assertEquals(0, counters[2]);
    }

    @Test
    public void testRollbackFromVerification() throws Exception {
        int[] counters = new int[3];

        longTransaction(() -> {
            transactionally(evaluate(() -> counters[0]++)
                    .withCommit(val -> counters[0]++)
                    .withRollback(val -> counters[0]=0));

            transactionally(evaluate(() -> counters[1]++)
                    .withVerification(val -> false) // this will make the transaction fail
                    .withCommit(val -> counters[1]++)
                    .withRollback(val -> counters[1]=0));

            transactionally(evaluate(() -> counters[2]++)
                    .withCommit(val -> counters[2]++));
        });

        assertEquals(0, counters[0]);
        assertEquals(0, counters[1]);
        assertEquals(0, counters[2]);
    }

    @Test
    public void testRollbackWithSubSteps() throws Exception {
        int[] counters = new int[6];

        longTransaction(() -> {
            transactionally(evaluate(() -> counters[0]++)
                    .withRollback(val -> counters[0] = 0)
                .and(val -> counters[1]++)
                    .withRollback(val -> counters[1]=0));

            transactionally(evaluate(() -> counters[2]++)
                    .withRollback(val -> counters[2]=0)
                .then(val -> counters[3]++)
                    .withRollback(val -> counters[3]=0)
                .then(val -> counters[4]++)
                    .withVerification(val -> false)
                    .withRollback(val -> counters[4]=0));

            transactionally(evaluate(() -> counters[5]++)
                    .withCommit(val -> counters[5]++));
        });

        assertEquals(0, counters[0]);
        assertEquals(0, counters[1]);
        assertEquals(0, counters[2]);
        assertEquals(0, counters[3]);
        assertEquals(0, counters[4]);
        assertEquals(0, counters[5]);
    }

    @Test
    public void testRollbackWithSubStepsExceptionFromWithin() throws Exception {
        int[] counters = new int[6];

        longTransaction(() -> {
            transactionally(evaluate(() -> counters[0]++)
                    .withRollback(val -> counters[0]=0)
                    .and(val -> counters[1]++)
                    .withRollback(val -> counters[1]=0));

            transactionally(evaluate(() -> counters[2]++)
                    .withRollback(val -> counters[2]=0)
                    .then(val -> counters[3]++)
                    .withRollback(val -> counters[3]=0)
                    .then(val -> throwException())
                    .withRollback(val -> counters[4] = 0));

            transactionally(evaluate(() -> counters[5]++)
                    .withCommit(val -> counters[5]++));
        });

        assertEquals(0, counters[0]);
        assertEquals(0, counters[1]);
        assertEquals(0, counters[2]);
        assertEquals(0, counters[3]);
        assertEquals(0, counters[4]);
        assertEquals(0, counters[5]);
    }

    private int throwException() {
        throw new RuntimeException();
    }
}