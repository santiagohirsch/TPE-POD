package ar.edu.itba.pod.grpc.server.exceptions;

public class PassangersStillWaitingException extends RuntimeException {
    public PassangersStillWaitingException(String message) {
        super(message);
    }
}
