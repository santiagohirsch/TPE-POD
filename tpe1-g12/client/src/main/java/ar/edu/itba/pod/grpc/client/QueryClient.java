package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.AdminServiceGrpc;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Query.QueryCountersCallback;
import ar.edu.itba.pod.grpc.query.CounterResponse;
import ar.edu.itba.pod.grpc.query.Filters;
import ar.edu.itba.pod.grpc.query.ListCounterResponse;
import ar.edu.itba.pod.grpc.query.QueryServiceGrpc;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//
//            }
//        }


        Filters filters = Filters.newBuilder().setOutPath("../query1.txt").build();
        ListenableFuture<ListCounterResponse> listInfoResponse = stub.queryCounters(filters);
        ExecutorService executor = Executors.newCachedThreadPool();
        Futures.addCallback(listInfoResponse, new FutureCallback<>() {
            @Override
            public void onSuccess(ListCounterResponse listCounterResponse) {
                StringBuilder sb = new StringBuilder();

                sb.append("Sector\t Counters\t Airline \t Flights \t People \n");
                sb.append("##########################################################\n");

                for(String sector : listCounterResponse.getSectorsList()) {
                    if(!(sector.equals(filters.getSectorName()))) {
                        continue;
                    }
                    sb.append(sector).append("\t");

                    for (CounterResponse counterResponse : listCounterResponse.getCounterResponseList()) {
                        if(counterResponse.getSectorName().equals(sector)){
                            sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
                            sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");

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
                            sb.append(counterResponse.getPassengers() != 0 ? counterResponse.getPassengers() : "-\t").append("\n");
                        }
                    }
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
        }, executor);

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
