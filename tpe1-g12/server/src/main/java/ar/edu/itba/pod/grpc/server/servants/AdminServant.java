package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.admin.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.NotificationCenter;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.BoolValue;
import io.grpc.stub.StreamObserver;

import java.util.Optional;

public class AdminServant extends AdminServiceGrpc.AdminServiceImplBase {
    private Airport airport;
    private NotificationCenter notificationCenter;

    public AdminServant(Airport airport, NotificationCenter notificationCenter) {
        this.airport = airport;
        this.notificationCenter = notificationCenter;
    }

    @Override
    public void addSector(SectorData request, StreamObserver<BoolValue> responseObserver) {
        boolean response = this.airport.addSector(new Sector(request.getName()));

        responseObserver.onNext(BoolValue.of(response));
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(CounterCount request, StreamObserver<CounterResponse> responseObserver) {

        Optional<Pair<Integer, Integer>> interval = this.airport.addCounters(new Sector(request.getSector().getName()), request.getCount());
        interval.ifPresentOrElse(
                aux -> {
                    responseObserver.onNext(CounterResponse.newBuilder()
                                    .setSector(request.getSector())
                                    .setCount(request.getCount())
                                    .setInterval(Interval.newBuilder()
                                            .setLowerBound(interval.get().getLeft())
                                            .setUpperBound(interval.get().getRight())
                                            .build()).build());
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onNext(CounterResponse.newBuilder()
                            .setCount(-1).build());
                    responseObserver.onCompleted();
                }
        );
    }

    @Override
    public void addBooking(Booking request, StreamObserver<BoolValue> responseObserver) {
        boolean response = this.airport.addBooking(request.getBookingCode(), request.getFlightCode(), request.getAirline());
        responseObserver.onNext(BoolValue.of(response));
        responseObserver.onCompleted();
    }
}
