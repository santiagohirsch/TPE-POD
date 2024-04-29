package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.AdminServiceGrpc;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Counter.*;
import ar.edu.itba.pod.grpc.counter.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ar.edu.itba.pod.grpc.client.utils.Actions.*;
import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;
import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.checkNullArgument;

public class CounterClient {

    private static Logger logger = LoggerFactory.getLogger(CounterClient.class);
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

        CounterServiceGrpc.CounterServiceFutureStub stub = CounterServiceGrpc.newFutureStub(channel);


        switch (action) {
            case LIST_SECTORS -> {
                latch = new CountDownLatch(1);
                ListenableFuture<ListSectorsResponse> response = stub.listSectors(Empty.newBuilder().build());
                Futures.addCallback(response, new ListSectorsCallback(logger,latch), Executors.newCachedThreadPool());
            }
            case LIST_COUNTERS -> {
                String sector = getArg(argsMap,SECTOR);
                String counterFromArg = getArg(argsMap,COUNTER_FROM);
                String counterToArg = getArg(argsMap,COUNTER_TO);
                int counterFrom = Integer.parseInt(counterFromArg);
                int counterTo = Integer.parseInt(counterToArg);
                latch = new CountDownLatch(1);
                CounterInfo counterRequest = CounterInfo.newBuilder().setName(sector).setInterval(Interval.newBuilder().setLowerBound(counterFrom).setUpperBound(counterTo).build()).build();
                ListenableFuture<CounterInfoResponse> counterInfoResponse = stub.getCounterInfo(counterRequest);
                Futures.addCallback(counterInfoResponse, new GetCounterInfoCallback(logger,latch), Executors.newCachedThreadPool());
            }
            case ASSIGN_COUNTERS -> {
                String sector = getArg(argsMap,SECTOR);
                String flightsArg = getArg(argsMap,FLIGHTS);
                String airline = getArg(argsMap,AIRLINE);
                String counterCountArg = getArg(argsMap,COUNTER_COUNT);
                int counterCount = Integer.parseInt(counterCountArg);
                // Crear una lista para almacenar los elementos
                List<String> flights = Arrays.asList(flightsArg.split("\\|"));

                latch = new CountDownLatch(1);
                AssignCounterInfo assignCounterRequest = AssignCounterInfo.newBuilder().addAllFlightCodes(flights).setCount(counterCount).setAirline(airline).setSector(SectorData.newBuilder().setName(sector).build()).build();
                ListenableFuture<AssignCounterResponse> assignCounterResponse = stub.assignCounters(assignCounterRequest);
                Futures.addCallback(assignCounterResponse, new AssignCountersCallback(logger,latch), Executors.newCachedThreadPool());
            }
            case FREE_COUNTERS -> {
                String sector = getArg(argsMap,SECTOR);
                String counterFromArg = getArg(argsMap,COUNTER_FROM);
                String airline = getArg(argsMap,AIRLINE);
                int counterFrom = Integer.parseInt(counterFromArg);
                latch = new CountDownLatch(1);
                FreeCounterInfo freeCounterInfo = FreeCounterInfo.newBuilder().setCounterName(sector).setAirline(airline).setFrom(counterFrom).build();
                ListenableFuture<FreeCounterResponse> freeCounterResponse = stub.freeCounters(freeCounterInfo);
                Futures.addCallback(freeCounterResponse, new FreeCountersCallback(logger,latch), Executors.newCachedThreadPool());
            }
            case LIST_PENDING_ASSIGNMENTS -> {
                String sector = getArg(argsMap, SECTOR);
                latch = new CountDownLatch(1);
                SectorData listPendingInfo = SectorData.newBuilder().setName(sector).build();
                ListenableFuture<ListPendingAssignmentResponse> listPendingAssignmentResponse = stub.listPendingAssignments(listPendingInfo);
                Futures.addCallback(listPendingAssignmentResponse, new ListPendingAssignments(logger, latch), Executors.newCachedThreadPool());
            }

            case CHECKIN_COUNTERS -> {
                String sector = getArg(argsMap, SECTOR);
                String counterFromArg = getArg(argsMap, COUNTER_FROM);
                String airline = getArg(argsMap, AIRLINE);
                int counterFrom = Integer.parseInt(counterFromArg);
                latch = new CountDownLatch(1);
                CheckInInfo checkInInfo = CheckInInfo.newBuilder().setAirline(airline).setSector(SectorData.newBuilder().setName(sector).build()).setFrom(counterFrom).build();
                ListenableFuture<ListCheckInResponse> listCheckInResponse = stub.checkInCounters(checkInInfo);
                Futures.addCallback(listCheckInResponse, new CheckInCountersCallback(logger, latch), Executors.newCachedThreadPool());
            }

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
