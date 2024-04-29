package ar.edu.itba.pod.grpc.server.exceptions;

public class NoCheckInsRegisteredException extends RuntimeException {
    public NoCheckInsRegisteredException(String message) {
        super(message);
    }
}
