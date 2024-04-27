package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.utils.CheckInModel;
import ar.edu.itba.pod.grpc.server.utils.CheckInStatusModel;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoBookingModel;
import io.grpc.stub.StreamObserver;

import java.util.Optional;

public class PassengerServant extends PassengerServiceGrpc.PassengerServiceImplBase {
    private Airport airport;

    public PassengerServant(Airport airport) { this.airport = airport; }

    /*
    return Optional.of(CounterInfo.newBuilder().setAirline(counter.getValue().get().getName())
                                        .setFlightCode(flight.getFlightCode())
                                        .setCounters(Interval.newBuilder().setLowerBound(counter.getKey()).setUpperBound(lastCounter).build())
                                        .setSector(sector.getName())
                                        .setQueueLen(0)//todo
                                        .build());
     */
    @Override
    public void fetchCounter(Booking request, StreamObserver<CounterInfo> responseObserver) {

        Optional<CounterInfoBookingModel> counterInfo = this.airport.fetchCounter(request.getBookingCode());


        counterInfo.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(CounterInfo.newBuilder()
                            .setCounters(Interval.newBuilder().setLowerBound(counterInfo.get().getInterval().getLeft()).setUpperBound(counterInfo.get().getInterval().getRight()).build())
                            .setAirline(counterInfo.get().getAirline())
                            .setSector(counterInfo.get().getSector())
                            .setQueueLen(counterInfo.get().getPeople())
                            .setFlightCode(counterInfo.get().getFlightCode())
                            .build()

                    );
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
        Optional <CheckInModel> checkInResponse = this.airport.checkIn(request.getBooking().getBookingCode(),new Sector( request.getSectorName()), request.getCounter());
        checkInResponse.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(CheckInResponse.newBuilder()
                            .setCounterInfo(CounterInfo.newBuilder()
                                    .setAirline(checkInResponse.get().getAirline())
                                    .setFlightCode(checkInResponse.get().getFlightCode())
                                    .setCounters(Interval.newBuilder().setLowerBound(checkInResponse.get().getInterval().getLeft()).setUpperBound(checkInResponse.get().getInterval().getRight()).build())
                                    .setSector(request.getSectorName())
                                    .setQueueLen(checkInResponse.get().getPeopleAhead())
                                    .build()
                            ).build()
                    );
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
        Optional <CheckInStatusModel> statusResponse = this.airport.status(request.getBookingCode());
        statusResponse.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(StatusResponse.newBuilder()
                            .setStatus(0)
                            .setCheckinResponse(CheckInResponse.newBuilder()
                                    .setCounterInfo(CounterInfo.newBuilder()
                                            .setAirline(statusResponse.get().getAirline())
                                            .setFlightCode(statusResponse.get().getFlightCode())
                                            .setCounters(Interval.newBuilder().setLowerBound(statusResponse.get().getInterval().getLeft()).setUpperBound(statusResponse.get().getInterval().getRight()).build())
                                            .setSector(statusResponse.get().getSector())
                                            .setQueueLen(statusResponse.get().getPeopleAhead())
                                            .build()
                            ).build()
                    ).build());

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
