package ar.edu.itba.pod.grpc.client;

import ar.edu.itba.pod.grpc.event.EventServiceGrpc;
import ar.edu.itba.pod.grpc.event.RegisterInfo;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventClient {
    private static Logger logger = LoggerFactory.getLogger(EventClient.class);

    public static void main(String[] args) throws InterruptedException {
        // TODO - check logs
        logger.info("tpe1-g12 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        try {
            //setup
            CountDownLatch latch = new CountDownLatch(1);
            EventServiceGrpc.EventServiceFutureStub stub = EventServiceGrpc.newFutureStub(channel);
            //1.1
            RegisterInfo request = RegisterInfo.newBuilder().setAirline("Americas").build();
            ListenableFuture<Empty> response = stub.register(request);

            ExecutorService executor = Executors.newCachedThreadPool();
            Futures.addCallback(response, new FutureCallback<>(

            ) {
                @Override
                public void onSuccess(Empty e) {
                    String out = request.getAirline() + "  registered successfully for events" ;

                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("fallo");
                    latch.countDown();
                }
        }, executor);

            // -----------------------------------------------------------------------------
            RegisterInfo unRegisterRequest = RegisterInfo.newBuilder().setAirline("Americas").build();
            ListenableFuture<BoolValue> unRegisterResponse = stub.unRegister(unRegisterRequest);

            ExecutorService unRegisterExecutor = Executors.newCachedThreadPool();
            Futures.addCallback(unRegisterResponse, new FutureCallback<>(

            ) {
                @Override
                public void onSuccess(BoolValue boolValue) {
                    String out = request.getAirline() + "  unregistered successfully for events" ;

                    System.out.println(out);
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("fallo");
                    latch.countDown();
                }
            }, unRegisterExecutor);

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
