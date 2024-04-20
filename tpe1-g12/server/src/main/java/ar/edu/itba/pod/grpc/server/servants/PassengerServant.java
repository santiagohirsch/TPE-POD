package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.admin.CounterResponse;
import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import io.grpc.stub.StreamObserver;

import java.util.Optional;

public class PassengerServant extends PassengerServiceGrpc.PassengerServiceImplBase {
    private Airport airport;

    public PassengerServant(Airport airport) { this.airport = airport; }

    @Override
    public void fetchCounter(Booking request, StreamObserver<CounterInfo> responseObserver) {
        //todo si no existe el codigo de reserva falla
        Optional<CounterInfo> counterInfo = this.airport.fetchCounter(request);
        counterInfo.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(counterInfo.get());
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onNext(CounterInfo.newBuilder()
                            .setQueueLen(-1).build());
                    responseObserver.onCompleted();
                }
        );
    }

    @Override
    public void checkIn(CheckInInfo request, StreamObserver<CheckInResponse> responseObserver) {
        Optional <CheckInResponse> checkInResponse = this.airport.checkIn(request);
        checkInResponse.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(checkInResponse.get());
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onNext(CheckInResponse.newBuilder()
                            .setCounterInfo(CounterInfo.newBuilder().setQueueLen(-1).build()).build());
                    responseObserver.onCompleted();
                }
        );
    }

    @Override
    public void status(Booking request, StreamObserver<StatusResponse> responseObserver) {
        Optional <StatusResponse> statusResponse = this.airport.status(request);
        statusResponse.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(statusResponse.get());
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onNext(StatusResponse.newBuilder()
                            .setStatus(-1).build());
                    responseObserver.onCompleted();
                }
        );
    }

}
