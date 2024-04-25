package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.counter.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public void getCounterInfo(CounterInfo request, StreamObserver<CounterResponse> responseObserver) {
        super.getCounterInfo(request, responseObserver);
    }

    @Override
    public void assignCounters(AssignCounterInfo request, StreamObserver<AssignCounterResponse> responseObserver) {
        String sector = request.getSector().getName();
        List<String> flightCodes = request.getFlightCodesList().stream().toList();
        String airline = request.getAirline();
        int count = request.getCount();
        Pair<Integer, Integer> assignedInterval = this.airport.assignCounters(sector, flightCodes, airline, count);
        if (assignedInterval.getLeft() == 0) {
            // TODO: getPending -> returns how many assignments are in front of this one -> use this value for the response
        }
        AssignCounterResponse response = AssignCounterResponse.newBuilder()
                .setAssignedInterval(
                        Interval.newBuilder()
                        .setLowerBound(assignedInterval.getLeft())
                                .setUpperBound(assignedInterval.getRight()))
                .setPendingAhead(0)
                .setInfo(request)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounters(FreeCounterInfo request, StreamObserver<FreeCounterResponse> responseObserver) {
        super.freeCounters(request, responseObserver);
    }

    @Override
    public void checkInCounters(CheckInInfo request, StreamObserver<CheckInResponse> responseObserver) {
        super.checkInCounters(request, responseObserver);
    }

    @Override
    public void listPendingAssignments(SectorData request, StreamObserver<PendingAssignment> responseObserver) {
        super.listPendingAssignments(request, responseObserver);
    }
}
