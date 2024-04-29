package ar.edu.itba.pod.grpc.server.exceptions;

public class SectorAlreadyExistException extends RuntimeException {

    public SectorAlreadyExistException(String message) {
        super(message);
    }

    public SectorAlreadyExistException() {
        super();
    }
}
