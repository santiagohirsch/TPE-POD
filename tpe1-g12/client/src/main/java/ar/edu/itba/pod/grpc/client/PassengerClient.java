package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.client.utils.callbacks.Passenger.*;
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

//        try {
//
//            CountDownLatch latch = new CountDownLatch(1);
//            PassengerServiceGrpc.PassengerServiceFutureStub stub = PassengerServiceGrpc.newFutureStub(channel);
//
//            //CounterInfo request = CounterInfo.newBuilder().setCounters(Interval.newBuilder().setLowerBound(1).setUpperBound(3)).setSector("A").setAirline("Americas").setQueueLen(6).setFlightCode("AA123").build();
//            Booking request = Booking.newBuilder().setBookingCode("YZA456").build();
//            //YZA456
//            //XYZ234
//            ListenableFuture<CounterInfo> response = stub.fetchCounter(request);
//            //SectorData request = SectorData.newBuilder().setName("A").build();
//            //ListenableFuture<BoolValue> response = stub.addSector(request);
//
//            ExecutorService executor = Executors.newCachedThreadPool();
//            Futures.addCallback(response, new FutureCallback<>(
//            ) {
//
//                @Override
//                public void onSuccess(CounterInfo counterInfo) {
//                    String out;
//
//                    out = "Flight " + counterInfo.getFlightCode() + " from " + counterInfo.getAirline() + " is now checking in at counters " + counterInfo.getCounters().getLowerBound() + " - " + counterInfo.getCounters().getUpperBound() + " in Sector " + counterInfo.getSector() + " with " + counterInfo.getQueueLen() + " people in line";
//                    if(counterInfo.getCounters().getLowerBound()==-1){
//                        //   Flight AA123 from AmericanAirlines has no counters assigned yet
//                        out = "Flight " + counterInfo.getFlightCode() + " from " + counterInfo.getAirline() + " has no counters assigned yet";
//                    }
//                    System.out.println(out);
//                    latch.countDown();
//                }
//
//                @Override
//                public void onFailure(Throwable throwable) {
//                    System.out.println("fallo");
//                    latch.countDown();
//                }
//            }, executor);
////
////
//
//
//
////             -------------------------------------------------------------------------------------------------------------------------------------------
//
//
//
//            ExecutorService checkInExecutor = Executors.newCachedThreadPool();
//            CheckInInfo checkInRequest = CheckInInfo.newBuilder()
//                    .setBooking(Booking.newBuilder().setBookingCode("XYZ240").build())
//                    .setCounter(5)
//                    .setSectorName("A")
//                    .build();
//            ListenableFuture<CheckInResponse> checkInresponse = stub.checkIn(checkInRequest);
//            ExecutorService CheckInexecutor = Executors.newCachedThreadPool();
//            Futures.addCallback(checkInresponse, new FutureCallback<>(
//            ) {
//
//                @Override
//                public void onSuccess(CheckInResponse checkInResponse) {
//                    String out;
//
//                    out = "Booking " + checkInResponse.getBooking().getBookingCode() + " for flight " + checkInResponse.getCounterInfo().getFlightCode() + " is now waiting to check-in on counters (" + checkInResponse.getCounterInfo().getCounters().getLowerBound() + "-" + checkInResponse.getCounterInfo().getCounters().getUpperBound() + ") in Sector " + checkInResponse.getCounterInfo().getSector() + " with " + checkInResponse.getCounterInfo().getQueueLen() + " people in line";
//                    //Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line
//
//                    System.out.println(out);
//                    latch.countDown();
//                }
//
//                @Override
//                public void onFailure(Throwable throwable) {
//                    //System.out.println(response);
//                    System.out.println("fallo");
//                    latch.countDown();
//                }
//            }, checkInExecutor);
//
//
//
//            // -------------------------------------------------------------------------------------------------------------------------------------------
//
//
//            Booking statusRequest = Booking.newBuilder().setBookingCode("XYZ239").build();
//            ListenableFuture<StatusResponse> statusResponse = stub.status(statusRequest);
//            ExecutorService statusExecutor = Executors.newCachedThreadPool();
//            Futures.addCallback(statusResponse, new FutureCallback<>(
//            ) {
//
//                @Override
//                public void onSuccess(StatusResponse statusResponse) {
//                    String out;
//
//                    // TODO agregar al statusResponse un int que sea el numero del counter en donde ya se chequeo
//
//                    if (statusResponse.getStatus()==0){
//                        //Booking ABC123 for flight AA123 from AmericanAirlines checked in at counter 4 in Sector C
//                        out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " checked in at counter " + statusResponse.getCheckinResponse().getCounter() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector();
//                    } else if(statusResponse.getStatus()==1){
//                        //Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line
//                        out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " is now waiting to check-in on counters " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getLowerBound() + " - " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getUpperBound() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector() + " with " + statusResponse.getCheckinResponse().getCounterInfo().getQueueLen() + " people in line";
//                    } else if(statusResponse.getStatus()==2){
//                        //Booking ABC123 for flight AA123 from AmericanAirlines can check-in on counters (3-4) in Sector C
//                        out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " can check-in on counters " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getLowerBound() + " - " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getUpperBound() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector();
//                    } else {
//                        out = "error";
//                    }
//
//                    System.out.println(out);
//                    latch.countDown();
//                }
//
//                @Override
//                public void onFailure(Throwable throwable) {
//                    //System.out.println(response);
//                    System.out.println("fallo");
//                    latch.countDown();
//                }
//            }, statusExecutor);
//
//
//
//
//
//
//
//
//            latch.await();
//        } finally {
//            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
//        }
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
