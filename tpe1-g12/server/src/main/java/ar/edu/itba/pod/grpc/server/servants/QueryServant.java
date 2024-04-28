package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.query.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
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
        List<CounterInfoModel> response = this.airport.queryCounters(sector);
        List<CounterResponse> counterResponses = new ArrayList<>();
        List<String> sectors = new ArrayList<>();
        for(CounterInfoModel counter : response) {
            counterResponses.add(CounterResponse.newBuilder()
                    .setAirline(counter.getAirline())
                    .setInterval(
                        Interval.newBuilder()
                                .setLowerBound(counter.getInterval().getLeft())
                                .setUpperBound(counter.getInterval().getRight())
                                .build()
                    )
                    .addAllFlightCode(counter.getFlightCodes())
                    .setPassengers(0) //todo
                    .setSectorName(counter.getSector())
                    .build());
            if(!sectors.contains(counter.getSector())) {
                sectors.add(counter.getSector());
            }
        }
        ListCounterResponse listCounterResponse = ListCounterResponse.newBuilder()
                .addAllCounterResponse(counterResponses)
                .addAllSectors(sectors)
                .build();
        responseObserver.onNext(listCounterResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(Filters request, StreamObserver<ListCheckIn> responseObserver) {
        super.queryCheckIns(request, responseObserver);
    }
}
