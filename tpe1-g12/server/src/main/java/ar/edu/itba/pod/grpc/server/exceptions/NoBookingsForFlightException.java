package ar.edu.itba.pod.grpc.server.exceptions;

public class NoBookingsForFlightException extends RuntimeException {
    public NoBookingsForFlightException(String message) {
        super(message);
    }
}
