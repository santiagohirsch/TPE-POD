package ar.edu.itba.pod.grpc.server.exceptions;

public class AirlineNotRegisteredForNotificationsException extends RuntimeException {
    public AirlineNotRegisteredForNotificationsException(String message) {
        super(message);
    }
}
