package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.AdminServiceGrpc;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Query.QueryClientCallback;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Query.QueryCountersCallback;
import ar.edu.itba.pod.grpc.query.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;
import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.checkNullArgument;
import static ar.edu.itba.pod.grpc.client.utils.Actions.*;

public class QueryClient {
    private static Logger logger = LoggerFactory.getLogger(QueryClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");

        /*Map<String, String> argsMap = parseArgs(args);

        String serverAddress = getArg(argsMap, SERVER_ADDRESS);
        String action = getArg(argsMap, ACTION);

        checkNullArgument(serverAddress);
        checkNullArgument(action);
        */
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)

//        ManagedChannel channel = ManagedChannelBuilder.forTarget("50051" /* serverAddress */ )
                .usePlaintext()
                .build();

        QueryServiceGrpc.QueryServiceFutureStub stub = QueryServiceGrpc.newFutureStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
//        switch (action) {
//            case QUERY_COUNTER -> {
//                String outPath = getArg(argsMap,OUT_PATH);
//                String sector = getArg(argsMap,SECTOR);
//                latch = new CountDownLatch(1);
//                Filters request = Filters.newBuilder().setSectorName(sector).setOutPath(outPath).build();
//                ListenableFuture<ListCounterResponse> response = stub.queryCounters(request);
//                Futures.addCallback(response, new QueryCountersCallback(logger, latch, request), Executors.newCachedThreadPool());
//            }
//            case CHECKINS -> {
//                String outPath = getArg(argsMap,OUT_PATH);
//                String sector = getArg(argsMap,SECTOR);
//                String airline = getArg(argsMap,AIRLINE);
//                latch = new CountDownLatch(1);
//                Filters checkinsFilters = Filters.newBuilder().setSectorName(sector).setAirline(airline).setOutPath(outPath).build();
//                ListenableFuture<ListCheckIn> checkinsResponse = stub.queryCheckIns(checkinsFilters);
//                ExecutorService checkinsExecutor = Executors.newCachedThreadPool();
//                Futures.addCallback(checkinsResponse, new QueryClientCallback(logger, latch, checkinsFilters), checkinsExecutor);
//            }
//        }

        //5.1
//        Filters filters = Filters.newBuilder().setOutPath("../query1.txt").build();
//        ListenableFuture<ListCounterResponse> listInfoResponse = stub.queryCounters(filters);
//        ExecutorService executor = Executors.newCachedThreadPool();
//        Futures.addCallback(listInfoResponse, new FutureCallback<>() {
//            @Override
//            public void onSuccess(ListCounterResponse listCounterResponse) {
//                StringBuilder sb = new StringBuilder();
//
//                sb.append("Sector\t Counters\t Airline \t Flights \t People \n");
//                sb.append("##########################################################\n");
//
//                for (CounterResponse counterResponse : listCounterResponse.getCounterResponseList()) {
//                    sb.append(counterResponse.getSectorName()).append("\t");
//                    sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
//                    sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");
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
//        }, executor);

        //5.2
//        Filters checkinsFilters = Filters.newBuilder().setOutPath("../query2.txt").build();
        Filters checkinsFilters = Filters.newBuilder().setOutPath("../query2.txt").build();
        ListenableFuture<ListCheckIn> checkinsResponse = stub.queryCheckIns(checkinsFilters);
        ExecutorService checkinsExecutor = Executors.newCachedThreadPool();
        Futures.addCallback(checkinsResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(ListCheckIn listCheckIn) {
                StringBuilder sb = new StringBuilder();

                sb.append("Sector\t Counter\t Airline\t Flight\t Booking\n");
                sb.append("##########################################################\n");

                for(CheckIn checkIn : listCheckIn.getCheckInList()) {
                    sb.append(checkIn.getSectorName()).append("\t");
                    sb.append(checkIn.getCounter()).append("\t");
                    sb.append(checkIn.getAirline()).append("\t");
                    sb.append(checkIn.getFlightCode()).append("\t");
                    sb.append(checkIn.getBookingCode()).append("\n");
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("../query3.txt"))) {
                    writer.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
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
        }, checkinsExecutor);

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
