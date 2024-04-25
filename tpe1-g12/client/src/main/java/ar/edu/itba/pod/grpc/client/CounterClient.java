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
