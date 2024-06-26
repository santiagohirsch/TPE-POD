package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Flight {

    private final String flightCode;
    private List<String> bookings;
    private boolean alreadyCheckedIn;


    public Flight(String flightCode) {
        this.flightCode = flightCode;
        this.bookings = new ArrayList<>();
        this.alreadyCheckedIn = false;
    }

    public List<String> getBookings() {
        return bookings;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setBookings(List<String> bookings) {
        this.bookings = bookings;
    }

    public void addBooking(String booking_code) {
        this.bookings.add(booking_code);
    }

    public boolean isAlreadyCheckedIn() {
        return alreadyCheckedIn;
    }

    public void setAlreadyCheckedIn(boolean alreadyCheckedIn) {
        this.alreadyCheckedIn = alreadyCheckedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightCode, flight.flightCode);
    }

}
