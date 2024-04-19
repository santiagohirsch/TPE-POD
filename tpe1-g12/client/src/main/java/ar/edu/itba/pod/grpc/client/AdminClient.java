package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.admin.AdminServiceGrpc;
import ar.edu.itba.pod.grpc.admin.SectorData;
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

public class AdminClient {
    private static Logger logger = LoggerFactory.getLogger(AdminClient.class);

    public static void main(String[] args) throws InterruptedException {
        // TODO - check logs
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        try {

            CountDownLatch latch = new CountDownLatch(1);
            AdminServiceGrpc.AdminServiceFutureStub stub = AdminServiceGrpc.newFutureStub(channel);
            SectorData request = SectorData.newBuilder().setName("A").build();
            ListenableFuture<BoolValue> response = stub.addSector(request);

            ExecutorService executor = Executors.newCachedThreadPool();
            Futures.addCallback(response, new FutureCallback<BoolValue>(

            ) {
                @Override
                public void onSuccess(BoolValue boolValue) {
                    String out = "Sector " + request.getName() + " added successfully";
                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("fallo");
                    latch.countDown();
                }
            }, executor);

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
