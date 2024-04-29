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

       Map<String, String> argsMap = parseArgs(args);

        String serverAddress = getArg(argsMap, SERVER_ADDRESS);
        String action = getArg(argsMap, ACTION);

        checkNullArgument(serverAddress);
        checkNullArgument(action);

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        QueryServiceGrpc.QueryServiceFutureStub stub = QueryServiceGrpc.newFutureStub(channel);
        switch (action) {
            case QUERY_COUNTERS -> {
                String outPath = getArg(argsMap,OUT_PATH);
                String sector = getArg(argsMap,SECTOR);
                latch = new CountDownLatch(1);
                Filters request = Filters.newBuilder().setSectorName(sector == null ? "" : sector).setOutPath(outPath).build();
                ListenableFuture<ListCounterResponse> response = stub.queryCounters(request);
                Futures.addCallback(response, new QueryCountersCallback(logger, latch, request), Executors.newCachedThreadPool());
            }
            case CHECKINS -> {
                String outPath = getArg(argsMap,OUT_PATH);
                String sector = getArg(argsMap,SECTOR);
                String airline = getArg(argsMap,AIRLINE);
                latch = new CountDownLatch(1);
                Filters checkinsFilters = Filters.newBuilder().setSectorName(sector == null ? "" : sector).setAirline(airline == null ? "" : airline).setOutPath(outPath).build();
                ListenableFuture<ListCheckIn> checkinsResponse = stub.queryCheckIns(checkinsFilters);
                ExecutorService checkinsExecutor = Executors.newCachedThreadPool();
                Futures.addCallback(checkinsResponse, new QueryClientCallback(logger, latch, checkinsFilters), checkinsExecutor);
            }
            default -> {System.exit(1);}
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
