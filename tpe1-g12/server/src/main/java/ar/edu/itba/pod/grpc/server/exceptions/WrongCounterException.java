package ar.edu.itba.pod.grpc.server.exceptions;

public class WrongCounterException extends RuntimeException {
    public WrongCounterException(String message) {
        super(message);
    }
}
