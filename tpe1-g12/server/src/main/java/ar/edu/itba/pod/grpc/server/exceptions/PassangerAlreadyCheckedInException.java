package ar.edu.itba.pod.grpc.server.exceptions;

public class PassangerAlreadyCheckedInException extends RuntimeException {
    public PassangerAlreadyCheckedInException(String message) {
        super(message);
    }
}
