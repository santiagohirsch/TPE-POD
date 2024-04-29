package ar.edu.itba.pod.grpc.server.exceptions;

public class BookingAlreadyExistException extends RuntimeException {
    public BookingAlreadyExistException(String message) {
        super(message);
    }
}
