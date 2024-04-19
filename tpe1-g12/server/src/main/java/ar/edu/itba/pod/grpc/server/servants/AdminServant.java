package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.admin.*;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.Sector;
import com.google.protobuf.BoolValue;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

public class AdminServant extends AdminServiceGrpc.AdminServiceImplBase {
    private Airport airport;

    public AdminServant(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void addSector(SectorData request, StreamObserver<BoolValue> responseObserver) {
        boolean response = this.airport.addSector(new Sector(request.getName()));
        responseObserver.onNext(BoolValue.of(response));
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(CounterCount request, StreamObserver<CounterResponse> responseObserver) {
        super.addCounters(request, responseObserver);
    }

    @Override
    public StreamObserver<Booking> addBooking(StreamObserver<BoolValue> responseObserver) {
        return super.addBooking(responseObserver);
    }
}
