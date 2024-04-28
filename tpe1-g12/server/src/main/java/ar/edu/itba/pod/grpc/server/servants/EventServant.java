package ar.edu.itba.pod.grpc.server.servants;

import ar.edu.itba.pod.grpc.event.EventServiceGrpc;
import ar.edu.itba.pod.grpc.event.Notification;
import ar.edu.itba.pod.grpc.event.RegisterInfo;
import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.NotificationCenter;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class EventServant extends EventServiceGrpc.EventServiceImplBase {
    private Airport airport;
    private NotificationCenter notificationCenter;

    public EventServant(Airport airport, NotificationCenter notificationCenter) {
        this.airport = airport;
        this.notificationCenter = notificationCenter;
    }

    @Override
    public void register(RegisterInfo request, StreamObserver<Notification> responseObserver) {
        String airline = request.getAirline();
        this.airport.checkIfAirlineExists(airline);
        this.notificationCenter.registerAirline(airline, responseObserver);
        this.notificationCenter.notifyRegistration(airline);
    }

    @Override
    public void unRegister(RegisterInfo request, StreamObserver<Notification> responseObserver) {
        String airline = request.getAirline();
        StreamObserver<Notification> notificationObserver = this.notificationCenter.unRegisterAirline(airline);
        notificationObserver.onCompleted();
        responseObserver.onNext(Notification.newBuilder()
                .setMessage(String.format("%s unregistered successfully for events", airline))
                .build());
        responseObserver.onCompleted();
    }
}
