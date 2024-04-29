package ar.edu.itba.pod.grpc.server.exceptions;

public class IncorrectAirlineForCountersException extends RuntimeException {
    public IncorrectAirlineForCountersException(String message) {
        super(message);
    }
}
