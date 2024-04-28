package ar.edu.itba.pod.grpc.server;

import ar.edu.itba.pod.grpc.server.models.Airport;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.servants.AdminServant;
import ar.edu.itba.pod.grpc.server.servants.CounterServant;
import ar.edu.itba.pod.grpc.server.servants.QueryServant;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");
        Airport airport = new Airport();
        Sector a = new Sector("A");
        Sector b = new Sector("B");

        airport.addSector(a);
        airport.addSector(b);

        airport.addCounters(a, 2);
        airport.addCounters(b, 2);
        airport.addCounters(a, 2);

        int port = 50052;
        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new AdminServant(airport))
                .addService(new CounterServant(airport))
                .addService(new QueryServant(airport))
                .build();
        server.start();
        logger.info("Server started, listening on " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }}
