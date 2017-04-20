package me.jaksa;

import org.junit.Test;

import static me.jaksa.Transactions.atomically;
import static me.jaksa.Transactions.evaluate;
import static me.jaksa.Transactions.perform;
import static me.jaksa.Unreliable.keepTrying;
import static me.jaksa.Unreliable.tenaciusly;

public class Examples {

    @Test
    public void unreliable() throws Exception {
        UnreliableService unreliableService = new UnreliableService();

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
    }

    @Test
    public void transactions() throws Exception {
        UnreliableService service = new UnreliableService();
        Hotel reliableHotel = new Hotel();
        Hotel unreliableHotel = reliableHotel;
        Airline airline = new Airline();
        int maxPrice = 100;

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
    }

    private Dates getDates() {
        return new Dates();
    }

    private static class Dates {}
    private static class Room {
        public Dates getDates() { return new Dates(); }
        public int price() { return 42; }
    }
    private static class Flight {}

    private static class Hotel {
        public Room bookRoomInBestPeriod() { return new Room(); }
        public Room bookRoom(Dates d) { return new Room(); }
        public void cancelBooking(Room room) {}
        public void cancelAllBookings() {}
    }

    private static class Airline {
        public Flight reserveFlight(Dates dates) { return new Flight(); }
        public void confirmReservation(Flight flight) {}
    }

    private static class Itinerary {
        Itinerary(Room room, Flight flight) {}
    }
}
