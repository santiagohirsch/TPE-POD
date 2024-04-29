package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.callbacks.Passenger.*;
import ar.edu.itba.pod.grpc.counter.SectorData;
import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.passenger.CounterInfo;
import ar.edu.itba.pod.grpc.passenger.Interval;
import ar.edu.itba.pod.grpc.passenger.PassengerServiceGrpc;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;
import static ar.edu.itba.pod.grpc.client.utils.Actions.*;

public class PassengerClient {
    private static Logger logger = LoggerFactory.getLogger(PassengerClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
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

        PassengerServiceGrpc.PassengerServiceFutureStub stub = PassengerServiceGrpc.newFutureStub(channel);

        switch (action){
            case FETCH_COUNTER -> {
                String bookingCode = getArg(argsMap,BOOKING);
                Booking request = Booking.newBuilder().setBookingCode(bookingCode).build();
                latch = new CountDownLatch(1);
                ListenableFuture<CounterInfo> response = stub.fetchCounter(request);
                Futures.addCallback(response, new FetchCounterCallback(logger, latch), Executors.newCachedThreadPool());
            }
            case PASSENGER_CHECKIN -> {
                String bookingCode = getArg(argsMap,BOOKING);
                int counter = Integer.parseInt(getArg(argsMap,COUNTER));
                String sectorName = getArg(argsMap,SECTOR);
                CheckInInfo checkInRequest = CheckInInfo.newBuilder()
                        .setBooking(Booking.newBuilder().setBookingCode(bookingCode).build())
                        .setCounter(counter)
                        .setSectorName(sectorName)
                        .build();

                latch = new CountDownLatch(1);
                ListenableFuture<CheckInResponse> checkInResponse = stub.checkIn(checkInRequest);
                Futures.addCallback(checkInResponse, new PassengerCheckinCallback(logger, latch), Executors.newCachedThreadPool());
            }
            case PASSENGER_STATUS -> {
                String bookingCode = getArg(argsMap,BOOKING);
                Booking statusRequest = Booking.newBuilder().setBookingCode(bookingCode).build();
                latch = new CountDownLatch(1);
                ListenableFuture<StatusResponse> statusResponse = stub.status(statusRequest);
                Futures.addCallback(statusResponse, new PassengerStatusCallback(logger, latch), Executors.newCachedThreadPool());
            }
            default ->{System.exit(1);}
        }

        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
