package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.query.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.utils.CheckInData;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryServant extends QueryServiceGrpc.QueryServiceImplBase {
    private final Airport airport;

    public QueryServant(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void queryCounters(Filters request, StreamObserver<ListCounterResponse> responseObserver) {

        String sector = request.getSectorName();
        Map<String, List<CounterInfoModel>> response = this.airport.queryCounters(sector);
        ListCounterResponse.Builder builder = ListCounterResponse.newBuilder();

        for(Map.Entry<String, List<CounterInfoModel>> entry : response.entrySet()) {
            for(CounterInfoModel counterInfoModel : entry.getValue()) {
                builder.addCounterResponse(CounterResponse.newBuilder()
                        .setInterval(Interval.newBuilder()
                                .setLowerBound(counterInfoModel.getInterval().getLeft())
                                .setUpperBound(counterInfoModel.getInterval().getRight())
                                .build()
                        )
                        .setAirline(counterInfoModel.getAirline())
                        .addAllFlightCode(counterInfoModel.getFlightCodes())
                        .setPassengers(counterInfoModel.getPeople())
                        .setSectorName(entry.getKey())
                        .build()
                );
            }
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(Filters request, StreamObserver<ListCheckIn> responseObserver) {
        String sector = request.getSectorName();
        String airline = request.getAirline();
        List<CheckInData> checkedInList = this.airport.queryCheckIns(sector,airline);
        ListCheckIn.Builder builder = ListCheckIn.newBuilder();
        for (CheckInData checkInData : checkedInList) {
            builder.addCheckIn(CheckIn.newBuilder()
                    .setAirline(checkInData.getAirline())
                    .setSectorName(checkInData.getSector())
                    .setFlightCode(checkInData.getFlightCode())
                    .setBookingCode(checkInData.getBookingCode())
                    .setCounter(checkInData.getCounter())
                    .build()
            );
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
