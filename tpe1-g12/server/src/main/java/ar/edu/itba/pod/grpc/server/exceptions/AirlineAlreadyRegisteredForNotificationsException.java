package ar.edu.itba.pod.grpc.server.exceptions;

public class AirlineAlreadyRegisteredForNotificationsException extends RuntimeException {
    public AirlineAlreadyRegisteredForNotificationsException(String message) {
        super(message);
    }
}
