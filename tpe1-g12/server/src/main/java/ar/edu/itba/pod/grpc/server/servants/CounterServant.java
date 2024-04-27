package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.counter.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.Assignment;
import ar.edu.itba.pod.grpc.server.models.Flight;
import ar.edu.itba.pod.grpc.server.utils.CheckInResponseModel;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class CounterServant extends CounterServiceGrpc.CounterServiceImplBase {
    private Airport airport;

    public CounterServant(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void listSectors(Empty request, StreamObserver<ListSectorsResponse> responseObserver) {
        Optional<Map<String, List<Pair<Integer,Integer>>>> response = this.airport.listSectors();
        response.ifPresentOrElse(
                sectorDataMap -> {
                    ListSectorsResponse.Builder builder = ListSectorsResponse.newBuilder();
                    sectorDataMap.forEach((sectorName, pairs) -> {
                        SectorInfo.Builder sectorInfoBuilder = SectorInfo.newBuilder();
                        SectorData sectorData = SectorData.newBuilder().setName(sectorName).build();
                        sectorInfoBuilder.setSectorName(sectorData);
                        pairs.forEach(pair -> {
                            Interval.Builder intervalBuilder = Interval.newBuilder()
                                    .setLowerBound(pair.getLeft())
                                    .setUpperBound(pair.getRight());
                            sectorInfoBuilder.addIntervals(intervalBuilder);
                        });
                        builder.addSector(sectorInfoBuilder);
                    });
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                },
                () -> {
                    responseObserver.onNext(ListSectorsResponse.newBuilder().build());
                    responseObserver.onCompleted();
                }
        );
    }


    @Override
    public void getCounterInfo(CounterInfo request, StreamObserver<CounterInfoResponse> responseObserver) {
        String sector = request.getName();
        Pair<Integer, Integer> interval = new Pair<>(request.getInterval().getLowerBound(), request.getInterval().getUpperBound());
        List<CounterInfoModel> response = this.airport.getCounterInfo(sector, interval);
        if (response == null) {
            responseObserver.onError(new IllegalArgumentException("Non-existent sector"));
        } else {
            CounterInfoResponse.Builder builder = CounterInfoResponse.newBuilder();
            response.forEach((counterInfoModel -> builder.addCounters(
                    CounterResponse.newBuilder()
                            .setInterval(Interval.newBuilder()
                                    .setLowerBound(counterInfoModel.getInterval().getLeft())
                                    .setUpperBound(counterInfoModel.getInterval().getRight())
                                    .build())
                            .setAirline(counterInfoModel.getAirline())
                            .addAllFlightCode(counterInfoModel.getFlightCodes())
                            .setPassengers(counterInfoModel.getPeople())
            )));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void assignCounters(AssignCounterInfo request, StreamObserver<AssignCounterResponse> responseObserver) {
        String sector = request.getSector().getName();
        List<String> flightCodes = request.getFlightCodesList().stream().toList();
        String airline = request.getAirline();
        int count = request.getCount();
        int pendingAhead = 0;
        Pair<Integer, Integer> assignedInterval = this.airport.assignCounters(sector, flightCodes, airline, count);
        if (assignedInterval.getLeft() == 0) {
            pendingAhead = this.airport.getPendingAhead(sector, flightCodes, airline, count);
        }
        AssignCounterResponse response = AssignCounterResponse.newBuilder()
                .setAssignedInterval(
                        Interval.newBuilder()
                        .setLowerBound(assignedInterval.getLeft())
                                .setUpperBound(assignedInterval.getRight()))
                .setPendingAhead(pendingAhead)
                .setInfo(request)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounters(FreeCounterInfo request, StreamObserver<FreeCounterResponse> responseObserver) {
        String sector = request.getCounterName();
        int from = request.getFrom();
        String airline = request.getAirline();
        Optional<Assignment> freedCounters = this.airport.freeCounters(sector,from,airline); //todo cambiar

        if (freedCounters.isPresent()) {
            FreeCounterResponse freeCounterResponse = FreeCounterResponse.newBuilder()
                    .addAllFlightCodes(freedCounters.get().getFlightCodes().stream().map(Flight::getFlightCode).toList())
                    .setSector(SectorData.newBuilder()
                            .setName(request.getCounterName())
                            .build())
                    .setFreedInterval(Interval.newBuilder()
                            .setLowerBound(request.getFrom())
                            .setUpperBound(request.getFrom() + freedCounters.get().getCant() - 1)
                            .build())
                    .build();
            responseObserver.onNext(freeCounterResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkInCounters(CheckInInfo request, StreamObserver<ListCheckInResponse> responseObserver) {
        List<CheckInResponseModel> checkIns = this.airport.checkInCounters(request.getSector().getName(), request.getFrom(), request.getAirline());
        ListCheckInResponse.Builder builder = ListCheckInResponse.newBuilder();
        checkIns.forEach((checkIn) -> {
            builder.addInfo(CheckInResponse.newBuilder()
                    .setCounter(checkIn.getCounter())
                    .setFlightCode(checkIn.getFlightCode())
                    .setCheckinCode(checkIn.getBookingCode())
                    .build());
        });
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public void listPendingAssignments(SectorData request, StreamObserver<ListPendingAssignmentResponse> responseObserver) {
        Optional<Queue<Assignment>> pendingAssignments = this.airport.listPendingAssignments(request.getName());
        List<PendingAssignment> pendingAssignmentsResponse = new LinkedList<>();
        if(pendingAssignments.isPresent()) {
            for (Assignment assignment: pendingAssignments.get()) {
                List<String> flightCodes = new LinkedList<>(assignment.getFlightCodes().stream().map(Flight::getFlightCode).toList());
                pendingAssignmentsResponse.add(PendingAssignment.newBuilder().setCounters(assignment.getCant()).setAirline(assignment.getAirline()).addAllFlightCodes(flightCodes).build());
            }
            ListPendingAssignmentResponse listPendingAssignmentResponse = ListPendingAssignmentResponse.newBuilder().addAllPendings(pendingAssignmentsResponse)
                    .build();
            responseObserver.onNext(listPendingAssignmentResponse);
            responseObserver.onCompleted();
        }
        else {
            responseObserver.onNext(ListPendingAssignmentResponse.newBuilder().setPendings(0, PendingAssignment.newBuilder().setCounters(-1).build()).build());
        }
    }
}
