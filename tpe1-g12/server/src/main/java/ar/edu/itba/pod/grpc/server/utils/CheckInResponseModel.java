package ar.edu.itba.pod.grpc.server.utils;

public class CheckInResponseModel {

    private final String bookingCode;
    private final String flightCode;
    private final int counter;

    public CheckInResponseModel(String bookingCode, String flightCode, int counter) {
        this.bookingCode = bookingCode;
        this.flightCode = flightCode;
        this.counter = counter;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public int getCounter() {
        return counter;
    }
}
