package ar.edu.itba.pod.grpc.server.exceptions;

public class NoSectorsAvailableException extends RuntimeException {
    public NoSectorsAvailableException(String message) {
        super(message);
    }
}
