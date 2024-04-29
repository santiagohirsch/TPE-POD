package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.*;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Admin.AddBookingCallback;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Admin.AddCountersCallback;
import ar.edu.itba.pod.grpc.client.utils.callbacks.Admin.AddSectorCallback;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;
import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;
import static ar.edu.itba.pod.grpc.client.utils.Actions.*;


public class AdminClient {
    private static Logger logger = LoggerFactory.getLogger(AdminClient.class);
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
        //ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        AdminServiceGrpc.AdminServiceFutureStub stub = AdminServiceGrpc.newFutureStub(channel);

        switch (action) {
            case ADD_SECTOR -> {
                String sector = getArg(argsMap, SECTOR);
                checkNullArgument(sector);
                latch = new CountDownLatch(1);
                SectorData request = SectorData.newBuilder().setName(sector).build();
                ListenableFuture<BoolValue> response = stub.addSector(request);
                Futures.addCallback(response, new AddSectorCallback(logger, latch, request), Executors.newCachedThreadPool());
            }

            case ADD_COUNTERS -> {
                String sector = getArg(argsMap, SECTOR);
                String counters = getArg(argsMap, COUNTERS);
                checkNullArgument(sector);
                checkNullArgument(counters);
                int counterCount = Integer.parseInt(counters);
                latch = new CountDownLatch(1);
                CounterCount counterRequest = CounterCount.newBuilder()
                        .setSector(SectorData.newBuilder().setName(sector).build())
                        .setCount(counterCount).build();
                ListenableFuture<CounterResponse> counterResponse = stub.addCounters(counterRequest);
                Futures.addCallback(counterResponse, new AddCountersCallback(logger, latch), Executors.newCachedThreadPool());
            }
            case MANIFEST -> {
                String inPath = getArg(argsMap, IN_PATH);
                checkNullArgument(inPath);
                Path path = Paths.get(inPath);
                latch = new CountDownLatch(1);
                try (Stream<String> lines = Files.lines(path).skip(1)) {
                    lines.forEach(linea -> {
                        String[] campos = linea.split(";");
                        Booking booking = Booking.newBuilder()
                                .setBookingCode(campos[0])
                                .setFlightCode(campos[1])
                                .setAirline(campos[2])
                                .build();
                        ListenableFuture<BoolValue> bookingResponse = stub.addBooking(booking);
                        Futures.addCallback(bookingResponse, new AddBookingCallback(logger, latch, booking), Executors.newCachedThreadPool());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default -> {
                System.exit(1);
            }
        }
        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage());
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
