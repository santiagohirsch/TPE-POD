package ar.edu.itba.pod.grpc.server.exceptions;

public class PassangerAlreadyInQueueException extends RuntimeException {
    public PassangerAlreadyInQueueException(String message) {
        super(message);
    }
}
