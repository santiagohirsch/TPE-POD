package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.event.EventServiceGrpc;
import ar.edu.itba.pod.grpc.event.RegisterInfo;
import ar.edu.itba.pod.grpc.server.models.Airport;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class EventServant extends EventServiceGrpc.EventServiceImplBase {
    private Airport airport;

    public EventServant(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void register(RegisterInfo request,StreamObserver<Empty> streamObserver) {
        Empty response = this.airport.register(request);

        streamObserver.onNext(response);
        streamObserver.onCompleted();
    }

    @Override
    public void unRegister(RegisterInfo request,StreamObserver<BoolValue> streamObserver) {
        boolean response = this.airport.unRegister(request);
        streamObserver.onNext(BoolValue.of(response));
        streamObserver.onCompleted();
    }
}
