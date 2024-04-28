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
            case LIST_PENDING_ASSIGNMENTS -> {
                String sector = getArg(argsMap, SECTOR);
                latch = new CountDownLatch(1);
                SectorData listPendingInfo = SectorData.newBuilder().setName(sector).build();
                ListenableFuture<ListPendingAssignmentResponse> listPendingAssignmentResponse = stub.listPendingAssignments(listPendingInfo);
                Futures.addCallback(listPendingAssignmentResponse, new ListPendingAssignments(logger, latch), Executors.newCachedThreadPool());
            }
        }
//        ExecutorService listCheckInExecutor = Executors.newCachedThreadPool();
//
//        Futures.addCallback(listCheckInResponse, new FutureCallback<ListCheckInResponse>() {
//            @Override
//            public void onSuccess(ListCheckInResponse listCheckInResponse) {
//                StringBuilder sb = new StringBuilder();
//                for (CheckInResponse checkInResponse : listCheckInResponse.getInfoList()) {
//                    if (checkInResponse.getFlightCode().isEmpty()){
//                        sb.append("Counter " + checkInResponse.getCounter() + " is idle\n");
//                    } else {
//                        sb.append("Check-in successful of " + checkInResponse.getCheckinCode() + " for flight " + checkInResponse.getFlightCode() + " at counter " + checkInResponse.getCounter() + "\n");
//                    }
//                }
//                System.out.println(sb);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, listCheckInExecutor);





//
//        //setup
//        CountDownLatch latch = new CountDownLatch(1);
//        //2.1
//        ListenableFuture<ListSectorsResponse> sectorsResponse = stub.listSectors(Empty.newBuilder().build());
//        ExecutorService sectorsExecutor = Executors.newCachedThreadPool();
//        Futures.addCallback(sectorsResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(ListSectorsResponse listSectorsResponse) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("Sectors   Counters\n").append("###################\n");
//
//                for (SectorInfo sectorInfo : listSectorsResponse.getSectorList()) {
//                    sb.append(sectorInfo.getSectorName().getName()).append("\t\t\t");
//                    List<Interval> intervals = sectorInfo.getIntervalsList();
//                    if (!intervals.isEmpty()) {
//                        for (Interval interval : intervals) {
//                            sb.append("(").append(interval.getLowerBound()).append("-").append(interval.getUpperBound()).append(")");
//                        }
//                    } else {
//                        sb.append("-");
//                    }
//
//                    sb.append("\n");
//                }
//                System.out.println(sb);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println("fallo");
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, sectorsExecutor);
//
//        //2.2
//        ListenableFuture<CounterInfoResponse> counterInfoResponse = stub.getCounterInfo(CounterInfo.newBuilder().setName("A").setInterval(Interval.newBuilder().setLowerBound(1).setUpperBound(2).build()).build());
//        ExecutorService counterInfoExecutor = Executors.newCachedThreadPool();
//        Futures.addCallback(counterInfoResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(CounterInfoResponse counterInfoResponse) {
//                StringBuilder sb = new StringBuilder();
//                //todo: hacer mas linda la impresion
//                sb.append("Counters\t Airline \t Flights \t People \n");
//                sb.append("##########################################################\n");
//                for (CounterResponse counterResponse : counterInfoResponse.getCountersList()) {
//                    sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
//                    sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");
////                    sb.append(counterResponse.getAirline()).append("\t"); asi no imprime el -
//
//                    if (!counterResponse.getFlightCodeList().isEmpty()) {
//                        Iterator<String> it = counterResponse.getFlightCodeList().stream().toList().iterator();
//                        while (it.hasNext()) {
//                            sb.append(it.next());
//                            if (it.hasNext()) {
//                                sb.append("|");
//                            }
//                        }
//                    } else {
//                        sb.append("-\t");
//                    }
//
//                    sb.append("\t");
////                    sb.append(counterResponse.getPassengers()).append("\n");
//                    sb.append(counterResponse.getPassengers() != 0 ? counterResponse.getPassengers() : "-\t").append("\n");
//
//                }
//
//                System.out.println(sb);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println("fallo");
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, counterInfoExecutor);
//
//        //2.3
//        List<String> flightCodes = new ArrayList<>();
//        flightCodes.add("AA123");
//        flightCodes.add("AA234");
//        AssignCounterInfo assignCounterInfo = AssignCounterInfo.newBuilder()
//                .setSector(SectorData.newBuilder().setName("A"))
//                .setAirline("AmericanAirlines")
//                .addAllFlightCodes(flightCodes)
//                .setCount(2)
//                .build();
//        ListenableFuture<AssignCounterResponse> assignCounterResponse = stub.assignCounters(assignCounterInfo);
//        ExecutorService assignCounterExecutor = Executors.newCachedThreadPool();
//        Futures.addCallback(assignCounterResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(AssignCounterResponse assignCounterResponse) {
//                StringBuilder sb = new StringBuilder();
//                if (assignCounterResponse.getAssignedInterval().getLowerBound() == -1) {
//                    sb.append("The assignation could not be done");
//                    System.out.println(sb);
//                    latch.countDown();
//                } else if (assignCounterResponse.getAssignedInterval().getLowerBound() == 0) {
//                    sb.append(assignCounterResponse.getInfo().getCount())
//                            .append(" counters")
//                            .append(" in Sector ")
//                            .append(assignCounterResponse.getInfo().getSector().getName())
//                            .append(" is pending with ")
//                            .append(assignCounterResponse.getPendingAhead())
//                            .append(" other pendings ahead");
//                    System.out.println(sb);
//                    latch.countDown();
//                } else {
//                    sb.append(assignCounterResponse.getInfo().getCount())
//                            .append(" counters ")
//                            .append("(")
//                            .append(assignCounterResponse.getAssignedInterval().getLowerBound())
//                            .append("-")
//                            .append(assignCounterResponse.getAssignedInterval().getUpperBound())
//                            .append(")")
//                            .append(" in Sector ")
//                            .append(assignCounterResponse.getInfo().getSector().getName())
//                            .append(" are now checking in passengers from ")
//                            .append(assignCounterResponse.getInfo().getAirline())
//                            .append(" ");
//                    Iterator<String> it = assignCounterResponse.getInfo().getFlightCodesList().stream().toList().iterator();
//                    while (it.hasNext()) {
//                        sb.append(it.next());
//                        if (it.hasNext()) {
//                            sb.append("|");
//                        }
//                    }
//                    sb.append(" flights");
//                    System.out.println(sb);
//                    latch.countDown();
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println("fallo");
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, assignCounterExecutor);
//
//        //2.4
//
//        FreeCounterInfo freeCounterInfo = FreeCounterInfo.newBuilder().setFrom(1).setCounterName("A").setAirline("AmericanAirlines").build();
//        ListenableFuture<FreeCounterResponse> freeCountersResponse = stub.freeCounters(freeCounterInfo);
//        ExecutorService freeCountersExecutor = Executors.newCachedThreadPool();
//
//        Futures.addCallback(freeCountersResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(FreeCounterResponse freeCounterResponse) {
//                StringBuilder sb = new StringBuilder();
//                if (freeCounterResponse.getFreedInterval().getLowerBound() != -1) {
//                    sb.append("Ended check-in for flights ");
//                    for (String flight : freeCounterResponse.getFlightCodesList()) {
//                        sb.append(flight);
//                        sb.append("|");
//                    }
//                    sb.deleteCharAt(sb.lastIndexOf("|"));
//                    sb.append(" on ");
//                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound() - freeCounterResponse.getFreedInterval().getLowerBound() + 1);
//                    sb.append(" counters ");
//                    sb.append("(");
//                    sb.append(freeCounterResponse.getFreedInterval().getLowerBound());
//                    sb.append("-");
//                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound());
//                    sb.append(") ");
//                    sb.append("in Sector ");
//                    sb.append(freeCounterResponse.getSector());
//                    System.out.println(sb);
//                    latch.countDown();
//                }
//                else {
//                    sb.append("Error");
//                    System.out.println(sb);
//                    latch.countDown();
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable throwable){
//                System.out.println("Fallo mal");
//                latch.countDown();
//            }
//        },freeCountersExecutor);
//
//        //2.6
//        SectorData sectorData = SectorData.newBuilder().setName("A").build();
//        ListenableFuture<ListPendingAssignmentResponse> pendingAssignmentResponse = stub.listPendingAssignments(sectorData);
//        ExecutorService pendingAssignmentsExecutor = Executors.newCachedThreadPool();
//
//        Futures.addCallback(pendingAssignmentResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(ListPendingAssignmentResponse pendingAssignments) {
//                StringBuilder sb = new StringBuilder();
//                if( !pendingAssignments.getPendingsList().isEmpty() && pendingAssignments.getPendingsList().get(0).getCounters() >= 0) {
//                    sb.append("Counters\tAirline\t\tFlights\n");
//                    sb.append("##########################################################\n");
//                    for (PendingAssignment pendingAssignment : pendingAssignments.getPendingsList()) {
//                        sb.append(pendingAssignment.getCounters());
//                        sb.append("\t");
//                        sb.append(pendingAssignment.getAirline());
//                        sb.append("\t");
//                        for (String flight : pendingAssignment.getFlightCodesList()) {
//                            sb.append(flight);
//                            sb.append("|");
//                        }
//                        sb.deleteCharAt(sb.lastIndexOf("|"));
//                        sb.append("\n");
//                    }
//                }
//                else if (pendingAssignments.getPendingsList().isEmpty()) {
//                    sb.append("No pending assignments ");
//                }
//                else {
//                    sb.append("Sector inexistente"); //todo cambiar (?
//                }
//                System.out.println(sb);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable){
//                System.out.println("Fallo mal");
//                latch.countDown();
//            }
//        },pendingAssignmentsExecutor);

//        FreeCounterInfo freeCounterInfo = FreeCounterInfo.newBuilder().setFrom(1).setCounterName("A").setAirline("AmericanAirlines").build();
//        ListenableFuture<FreeCounterResponse> freeCountersResponse = stub.freeCounters(freeCounterInfo);
//        ExecutorService freeCountersExecutor = Executors.newCachedThreadPool();
//
//        Futures.addCallback(freeCountersResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(FreeCounterResponse freeCounterResponse) {
//                StringBuilder sb = new StringBuilder();
//                if (freeCounterResponse.getFreedInterval().getLowerBound() != -1) {
//                    sb.append("Ended check-in for flights ");
//                    for (String flight : freeCounterResponse.getFlightCodesList()) {
//                        sb.append(flight);
//                        sb.append("|");
//                    }
//                    sb.deleteCharAt(sb.lastIndexOf("|"));
//                    sb.append(" on ");
//                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound() - freeCounterResponse.getFreedInterval().getLowerBound() + 1);
//                    sb.append(" counters ");
//                    sb.append("(");
//                    sb.append(freeCounterResponse.getFreedInterval().getLowerBound());
//                    sb.append("-");
//                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound());
//                    sb.append(") ");
//                    sb.append("in Sector ");
//                    sb.append(freeCounterResponse.getSector());
//                    System.out.println(sb);
//                    latch.countDown();
//                }
//                else {
//                    sb.append("Error");
//                    System.out.println(sb);
//                    latch.countDown();
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable throwable){
//                System.out.println("Fallo mal");
//                latch.countDown();
//            }
//        },freeCountersExecutor);
//
//        //2.5
//        CheckInInfo checkInInfo = CheckInInfo.newBuilder().setAirline("AmericanAirlines").setSector(SectorData.newBuilder().setName("A").build()).setFrom(5).build();
//        ListenableFuture<ListCheckInResponse> listCheckInResponse = stub.checkInCounters(checkInInfo);
//        ExecutorService listCheckInExecutor = Executors.newCachedThreadPool();
//
//        Futures.addCallback(listCheckInResponse, new FutureCallback<ListCheckInResponse>() {
//            @Override
//            public void onSuccess(ListCheckInResponse listCheckInResponse) {
//                StringBuilder sb = new StringBuilder();
//                for (CheckInResponse checkInResponse : listCheckInResponse.getInfoList()) {
//                    if (checkInResponse.getFlightCode().isEmpty()){
//                        sb.append("Counter " + checkInResponse.getCounter() + " is idle\n");
//                    } else {
//                        sb.append("Check-in successful of " + checkInResponse.getCheckinCode() + " for flight " + checkInResponse.getFlightCode() + " at counter " + checkInResponse.getCounter() + "\n");
//                    }
//                }
//                System.out.println(sb);
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//                latch.countDown();
//            }
//        }, listCheckInExecutor);
//
//
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
