package ar.edu.itba.pod.grpc.server.exceptions;

public class NoAssignedCountersException extends RuntimeException {
    public NoAssignedCountersException(String message) {
        super(message);
    }
}
