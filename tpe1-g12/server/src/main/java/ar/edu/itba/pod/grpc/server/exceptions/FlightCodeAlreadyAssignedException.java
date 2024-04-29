package ar.edu.itba.pod.grpc.server.exceptions;

public class FlightCodeAlreadyAssignedException extends RuntimeException {
    public FlightCodeAlreadyAssignedException(String message) {
        super(message);
    }
}
