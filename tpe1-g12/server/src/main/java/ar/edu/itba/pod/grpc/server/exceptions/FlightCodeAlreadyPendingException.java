package ar.edu.itba.pod.grpc.server.exceptions;

public class FlightCodeAlreadyPendingException extends RuntimeException {
    public FlightCodeAlreadyPendingException(String message) {
        super(message);
    }
}
