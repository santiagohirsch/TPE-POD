package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.callbacks.Event.UnregisterCallback;
import ar.edu.itba.pod.grpc.event.EventServiceGrpc;
import ar.edu.itba.pod.grpc.event.Notification;
import ar.edu.itba.pod.grpc.event.RegisterInfo;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;
import static ar.edu.itba.pod.grpc.client.utils.Actions.*;

public class EventClient {
    private static Logger logger = LoggerFactory.getLogger(EventClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
        // TODO - check logs
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        Map<String, String> argsMap = parseArgs(args);

        String serverAddress = getArg(argsMap, SERVER_ADDRESS);
        String action = getArg(argsMap, ACTION);

        checkNullArgument(serverAddress);
        checkNullArgument(action);

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();
        EventServiceGrpc.EventServiceFutureStub futureStub = EventServiceGrpc.newFutureStub(channel);
        EventServiceGrpc.EventServiceStub stub = EventServiceGrpc.newStub(channel);

        switch (action) {
            case REGISTER -> {
                String airline = getArg(argsMap, AIRLINE);
                checkNullArgument(airline);
                latch = new CountDownLatch(1);
                RegisterInfo registerRequest = RegisterInfo.newBuilder().setAirline(airline).build();
                StreamObserver<Notification> observer = new StreamObserver<>() {
                    @Override
                    public void onNext(Notification notification) {
                        System.out.println(notification.getMessage());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        latch.countDown();
                        System.out.println(throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                };

                stub.register(registerRequest, observer);
                latch.await();
            }
            case UNREGISTER -> {
                String airline = getArg(argsMap, AIRLINE);
                checkNullArgument(airline);
                latch = new CountDownLatch(1);
                RegisterInfo unRegisterRequest = RegisterInfo.newBuilder().setAirline(airline).build();
                ListenableFuture<Notification> unRegisterResponse = futureStub.unRegister(unRegisterRequest);
                Futures.addCallback(unRegisterResponse, new UnregisterCallback(logger, latch), Executors.newCachedThreadPool());
            }
            default -> {
                System.exit(1);
            }
        }


        //setup
//        CountDownLatch latch = new CountDownLatch(1);
//        EventServiceGrpc.EventServiceFutureStub futureStub = EventServiceGrpc.newFutureStub(channel);
//        EventServiceGrpc.EventServiceStub stub = EventServiceGrpc.newStub(channel);
        //4.1
//        RegisterInfo registerRequest = RegisterInfo.newBuilder().setAirline("AmericanAirlines").build();
//        StreamObserver<Notification> observer = new StreamObserver<>() {
//            @Override
//            public void onNext(Notification notification) {
//                System.out.println(notification.getMessage());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                latch.countDown();
//                System.out.println(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//            }
//        };


//          -----------------------------------------------------------------------------

        //4.2
//        RegisterInfo unRegisterRequest = RegisterInfo.newBuilder().setAirline("AmericanAirlines").build();
//        ListenableFuture<Notification> unRegisterResponse = futureStub.unRegister(unRegisterRequest);
//        Futures.addCallback(unRegisterResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(Notification notification) {
//                System.out.println(notification.getMessage());
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, Executors.newCachedThreadPool());


        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
