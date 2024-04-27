package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class Assignment {
    private final String airline;
    private final List<Flight> flightCodes;
    private final int cant;

    private List<String> bookings;

    private Queue<String> checkInQueue;


    private List<Pair<String, Integer>> checkedIn;




    public Assignment(String airline, List<Flight> flightCodes, int cant) {
        this.airline = airline;
        this.flightCodes = flightCodes;
        this.cant = cant;
        this.bookings = new LinkedList<>();
        this.checkInQueue = new LinkedList<>();
        this.checkedIn = new LinkedList<>();
    }

    public String getAirline() {
        return airline;
    }

    public List<Flight> getFlightCodes() {
        return flightCodes;
    }

    public int getCant() {
        return cant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return cant == that.cant && airline.equals(that.airline) && flightCodes.equals(that.flightCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airline, flightCodes, cant);
    }

    public List<String> getBookings() {
        return bookings;
    }

    public Pair<String, Integer> getCheckedIn() {
        return new Pair<>(airline, checkedIn.size());
    }

    public Queue<String> getCheckInQueue() {
        return checkInQueue;
    }

    public void addToQueue(String booking) {
        checkInQueue.add(booking);
    }

    public void checkInBooking(String booking, Integer counter) {
        checkInQueue.remove(booking);
        checkedIn.add(new Pair<>(booking, counter));
    }

    public boolean hasCheckedIn(String booking) {
        return checkedIn.stream().anyMatch(pair -> pair.getLeft().equals(booking));
    }

    public Integer getCounter(String booking) {
        return checkedIn.stream().filter(pair -> pair.getLeft().equals(booking)).findFirst().get().getRight();
    }


}
