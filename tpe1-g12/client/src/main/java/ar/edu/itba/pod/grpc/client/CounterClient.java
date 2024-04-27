package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.AdminServiceGrpc;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CounterClient {

    private static Logger logger = LoggerFactory.getLogger(CounterClient.class);

    public static void main(String[] args) throws InterruptedException {
        // TODO - check logs
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //setup
        CountDownLatch latch = new CountDownLatch(1);
        CounterServiceGrpc.CounterServiceFutureStub stub = CounterServiceGrpc.newFutureStub(channel);

        //2.1
        ListenableFuture<ListSectorsResponse> sectorsResponse = stub.listSectors(Empty.newBuilder().build());
        ExecutorService sectorsExecutor = Executors.newCachedThreadPool();
        Futures.addCallback(sectorsResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(ListSectorsResponse listSectorsResponse) {
                StringBuilder sb = new StringBuilder();
                sb.append("Sectors   Counters\n").append("###################\n");

                for (SectorInfo sectorInfo : listSectorsResponse.getSectorList()) {
                    sb.append(sectorInfo.getSectorName().getName()).append("\t\t\t");
                    List<Interval> intervals = sectorInfo.getIntervalsList();
                    if (!intervals.isEmpty()) {
                        for (Interval interval : intervals) {
                            sb.append("(").append(interval.getLowerBound()).append("-").append(interval.getUpperBound()).append(")");
                        }
                    } else {
                        sb.append("-");
                    }

                    sb.append("\n");
                }
                System.out.println(sb);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("fallo");
                System.out.println(throwable.getMessage());
                latch.countDown();
            }
        }, sectorsExecutor);

        //2.2
        ListenableFuture<CounterInfoResponse> counterInfoResponse = stub.getCounterInfo(CounterInfo.newBuilder().setName("A").setInterval(Interval.newBuilder().setLowerBound(1).setUpperBound(2).build()).build());
        ExecutorService counterInfoExecutor = Executors.newCachedThreadPool();
        Futures.addCallback(counterInfoResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(CounterInfoResponse counterInfoResponse) {
                StringBuilder sb = new StringBuilder();
                //todo: hacer mas linda la impresion
                sb.append("Counters\t Airline \t Flights \t People \n");
                sb.append("##########################################################\n");
                for (CounterResponse counterResponse : counterInfoResponse.getCountersList()) {
                    sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
                    sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");
//                    sb.append(counterResponse.getAirline()).append("\t"); asi no imprime el -

                    if (!counterResponse.getFlightCodeList().isEmpty()) {
                        Iterator<String> it = counterResponse.getFlightCodeList().stream().toList().iterator();
                        while (it.hasNext()) {
                            sb.append(it.next());
                            if (it.hasNext()) {
                                sb.append("|");
                            }
                        }
                    } else {
                        sb.append("-\t");
                    }

                    sb.append("\t");
//                    sb.append(counterResponse.getPassengers()).append("\n");
                    sb.append(counterResponse.getPassengers() != 0 ? counterResponse.getPassengers() : "-\t").append("\n");

                }

                System.out.println(sb);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("fallo");
                System.out.println(throwable.getMessage());
                latch.countDown();
            }
        }, counterInfoExecutor);

        //2.3
        List<String> flightCodes = new ArrayList<>();
        flightCodes.add("AA123");
        flightCodes.add("AA234");
        AssignCounterInfo assignCounterInfo = AssignCounterInfo.newBuilder()
                .setSector(SectorData.newBuilder().setName("A"))
                .setAirline("AmericanAirlines")
                .addAllFlightCodes(flightCodes)
                .setCount(2)
                .build();
        ListenableFuture<AssignCounterResponse> assignCounterResponse = stub.assignCounters(assignCounterInfo);
        ExecutorService assignCounterExecutor = Executors.newCachedThreadPool();
        Futures.addCallback(assignCounterResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(AssignCounterResponse assignCounterResponse) {
                StringBuilder sb = new StringBuilder();
                if (assignCounterResponse.getAssignedInterval().getLowerBound() == -1) {
                    sb.append("The assignation could not be done");
                    System.out.println(sb);
                    latch.countDown();
                } else if (assignCounterResponse.getAssignedInterval().getLowerBound() == 0) {
                    sb.append(assignCounterResponse.getInfo().getCount())
                            .append(" counters")
                            .append(" in Sector ")
                            .append(assignCounterResponse.getInfo().getSector().getName())
                            .append(" is pending with ")
                            .append(assignCounterResponse.getPendingAhead())
                            .append(" other pendings ahead");
                    System.out.println(sb);
                    latch.countDown();
                } else {
                    sb.append(assignCounterResponse.getInfo().getCount())
                            .append(" counters ")
                            .append("(")
                            .append(assignCounterResponse.getAssignedInterval().getLowerBound())
                            .append("-")
                            .append(assignCounterResponse.getAssignedInterval().getUpperBound())
                            .append(")")
                            .append(" in Sector ")
                            .append(assignCounterResponse.getInfo().getSector().getName())
                            .append(" are now checking in passengers from ")
                            .append(assignCounterResponse.getInfo().getAirline())
                            .append(" ");
                    Iterator<String> it = assignCounterResponse.getInfo().getFlightCodesList().stream().toList().iterator();
                    while (it.hasNext()) {
                        sb.append(it.next());
                        if (it.hasNext()) {
                            sb.append("|");
                        }
                    }
                    sb.append(" flights");
                    System.out.println(sb);
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("fallo");
                System.out.println(throwable.getMessage());
                latch.countDown();
            }
        }, assignCounterExecutor);

        //2.4

        FreeCounterInfo freeCounterInfo = FreeCounterInfo.newBuilder().setFrom(1).setCounterName("A").setAirline("AmericanAirlines").build();
        ListenableFuture<FreeCounterResponse> freeCountersResponse = stub.freeCounters(freeCounterInfo);
        ExecutorService freeCountersExecutor = Executors.newCachedThreadPool();

        Futures.addCallback(freeCountersResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(FreeCounterResponse freeCounterResponse) {
                StringBuilder sb = new StringBuilder();
                if (freeCounterResponse.getFreedInterval().getLowerBound() != -1) {
                    sb.append("Ended check-in for flights ");
                    for (String flight : freeCounterResponse.getFlightCodesList()) {
                        sb.append(flight);
                        sb.append("|");
                    }
                    sb.deleteCharAt(sb.lastIndexOf("|"));
                    sb.append(" on ");
                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound() - freeCounterResponse.getFreedInterval().getLowerBound() + 1);
                    sb.append(" counters ");
                    sb.append("(");
                    sb.append(freeCounterResponse.getFreedInterval().getLowerBound());
                    sb.append("-");
                    sb.append(freeCounterResponse.getFreedInterval().getUpperBound());
                    sb.append(") ");
                    sb.append("in Sector ");
                    sb.append(freeCounterResponse.getSector());
                    System.out.println(sb);
                    latch.countDown();
                }
                else {
                    sb.append("Error");
                    System.out.println(sb);
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Throwable throwable){
                System.out.println("Fallo mal");
                latch.countDown();
            }
        },freeCountersExecutor);

        //2.6
        SectorData sectorData = SectorData.newBuilder().setName("A").build();
        ListenableFuture<ListPendingAssignmentResponse> pendingAssignmentResponse = stub.listPendingAssignments(sectorData);
        ExecutorService pendingAssignmentsExecutor = Executors.newCachedThreadPool();

        Futures.addCallback(pendingAssignmentResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(ListPendingAssignmentResponse pendingAssignments) {
                StringBuilder sb = new StringBuilder();
                if( !pendingAssignments.getPendingsList().isEmpty() && pendingAssignments.getPendingsList().get(0).getCounters() >= 0) {
                    sb.append("Counters\tAirline\t\tFlights\n");
                    sb.append("##########################################################\n");
                    for (PendingAssignment pendingAssignment : pendingAssignments.getPendingsList()) {
                        sb.append(pendingAssignment.getCounters());
                        sb.append("\t");
                        sb.append(pendingAssignment.getAirline());
                        sb.append("\t");
                        for (String flight : pendingAssignment.getFlightCodesList()) {
                            sb.append(flight);
                            sb.append("|");
                        }
                        sb.deleteCharAt(sb.lastIndexOf("|"));
                        sb.append("\n");
                    }
                }
                else if (pendingAssignments.getPendingsList().isEmpty()) {
                    sb.append("No pending assignments ");
                }
                else {
                    sb.append("Sector inexistente"); //todo cambiar (?
                }
                System.out.println(sb);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable){
                System.out.println("Fallo mal");
                latch.countDown();
            }
        },pendingAssignmentsExecutor);

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
