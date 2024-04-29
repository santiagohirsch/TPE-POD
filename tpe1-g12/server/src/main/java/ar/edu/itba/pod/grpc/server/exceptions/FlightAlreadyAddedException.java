package ar.edu.itba.pod.grpc.server.exceptions;

public class FlightAlreadyAddedException extends RuntimeException {
    public FlightAlreadyAddedException(String message) {
        super(message);
    }
}
