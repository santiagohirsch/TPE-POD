package ar.edu.itba.pod.grpc.models;

import java.util.List;

public class Flight {

    private final String flightCode;
    private List<String> bookings;


    public Flight(String flightCode) {
        this.flightCode = flightCode;
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

}