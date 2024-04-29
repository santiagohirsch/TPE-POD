package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.admin.*;
import ar.edu.itba.pod.grpc.server.models.*;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.BoolValue;
import com.google.protobuf.MapEntry;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminServant extends AdminServiceGrpc.AdminServiceImplBase {
    private Airport airport;
    private NotificationCenter notificationCenter;

    public AdminServant(Airport airport, NotificationCenter notificationCenter) {
        this.airport = airport;
        this.notificationCenter = notificationCenter;
    }

    @Override
    public void addSector(SectorData request, StreamObserver<BoolValue> responseObserver)  {
        boolean response = this.airport.addSector(new Sector(request.getName()));

        responseObserver.onNext(BoolValue.of(response));
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(CounterCount request, StreamObserver<CounterResponse> responseObserver) {

        Optional<Pair<Integer, Integer>> interval = this.airport.addCounters(new Sector(request.getSector().getName()), request.getCount());
        interval.ifPresentOrElse(
                aux -> {
                    Map<Assignment, Pair<Integer,Integer>> solvedAssignments = this.airport.solvePendingAssignments(request.getSector().getName());
                    synchronized (solvedAssignments) {
                        for (Map.Entry<Assignment, Pair<Integer,Integer>> entry : solvedAssignments.entrySet()) {
                            Assignment a = entry.getKey();
                            Pair<Integer,Integer> assignmentInterval = entry.getValue();
                            this.notificationCenter.notifyAssignCounters(a.getAirline(), a.getCant(), assignmentInterval, request.getSector().getName(), a.getFlightCodes().stream().map(Flight::getFlightCode).collect(Collectors.toList()));
                            List<Pair<Assignment, Integer>> toNotify = this.airport.removeFromPending(request.getSector().getName(), a);
                            for (Pair<Assignment, Integer> assignmentToNotify : toNotify) {
                                Assignment assignment = assignmentToNotify.getLeft();
                                this.notificationCenter.notifyPending(assignment.getAirline(), assignment.getCant(), request.getSector().getName(), assignment.getFlightCodes().stream().map(Flight::getFlightCode).collect(Collectors.toList()), assignmentToNotify.getRight());
                            }
                        }
                    }

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
