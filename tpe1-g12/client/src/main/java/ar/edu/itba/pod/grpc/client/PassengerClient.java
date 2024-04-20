package ar.edu.itba.pod.grpc.client;

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PassengerClient {
    private static Logger logger = LoggerFactory.getLogger(PassengerClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        try {

            CountDownLatch latch = new CountDownLatch(1);
            PassengerServiceGrpc.PassengerServiceFutureStub stub = PassengerServiceGrpc.newFutureStub(channel);
            //CounterInfo request = CounterInfo.newBuilder().setCounters(Interval.newBuilder().setLowerBound(1).setUpperBound(3)).setSector("A").setAirline("Americas").setQueueLen(6).setFlightCode("AA123").build();
            Booking request = Booking.newBuilder().setBookingCode("XYZ345").build();
            ListenableFuture<CounterInfo> response = stub.fetchCounter(request);
            //SectorData request = SectorData.newBuilder().setName("A").build();
            //ListenableFuture<BoolValue> response = stub.addSector(request);

            ExecutorService executor = Executors.newCachedThreadPool();
            Futures.addCallback(response, new FutureCallback<>(
            ) {

                @Override
                public void onSuccess(CounterInfo counterInfo) {
                    String out;

                    out = "Flight " + counterInfo.getFlightCode() + "from " + counterInfo.getAirline() + "is now checking in at counters " + counterInfo.getCounters() + "in Sector " + counterInfo.getSector() + "with " + counterInfo.getQueueLen();

                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("fallo");
                    latch.countDown();
                }
            }, executor);

            // -------------------------------------------------------------------------------------------------------------------------------------------

            CheckInInfo checkInRequest = CheckInInfo.newBuilder()
                    .setBooking(Booking.newBuilder().setBookingCode("ABC123").build())
                    .setCounter(1)
                    .setSectorName("A")
                    .build();
            ListenableFuture<CheckInResponse> checkInresponse = stub.checkIn(checkInRequest);
            ExecutorService CheckInexecutor = Executors.newCachedThreadPool();
            Futures.addCallback(checkInresponse, new FutureCallback<>(
            ) {

                @Override
                public void onSuccess(CheckInResponse checkInResponse) {
                    String out;

                    out = "Booking" + checkInResponse.getBooking().getBookingCode() + " for flight" + checkInResponse.getCounterInfo().getFlightCode() + " is now waiting to check-in on counters " + checkInResponse.getCounterInfo().getCounters() + " in Sector " + checkInResponse.getCounterInfo().getSector() + " with " + checkInResponse.getCounterInfo().getQueueLen() + " people in line";
                    //Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line

                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println(response);
                    System.out.println("fallo");
                    latch.countDown();
                }
            }, executor);


            Booking booking = Booking.newBuilder()
                    .setBookingCode("ABC123")
                    .build();
                    /*CheckInInfo.newBuilder()
                    .setBooking(Booking.newBuilder().setBookingCode("ABC123").build())
                    .setCounter(1)
                    .setSectorName("A")
                    .build();*/
            ListenableFuture<StatusResponse> statusResponse = stub.status(booking);
            ExecutorService statuExecutor = Executors.newCachedThreadPool();
            Futures.addCallback(statusResponse, new FutureCallback<>(
            ) {

                @Override
                public void onSuccess(StatusResponse stResponse) {
                    String out;

                    out = "Booking" + stResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight" + stResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " checked in at counter " + stResponse.getCheckinResponse().getCounterInfo().getCounters() + " in Sector " + stResponse.getCheckinResponse().getCounterInfo().getSector();

                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println(response);
                    System.out.println("fallo");
                    latch.countDown();
                }
            }, executor);

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
