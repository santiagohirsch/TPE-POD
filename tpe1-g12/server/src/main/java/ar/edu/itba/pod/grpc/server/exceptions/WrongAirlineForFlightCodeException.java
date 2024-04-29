package ar.edu.itba.pod.grpc.server.exceptions;

public class WrongAirlineForFlightCodeException extends RuntimeException {
    public WrongAirlineForFlightCodeException(String message) {
        super(message);
    }
}
