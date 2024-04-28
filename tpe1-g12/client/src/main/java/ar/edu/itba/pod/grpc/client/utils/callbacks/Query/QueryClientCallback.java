package ar.edu.itba.pod.grpc.client.utils.callbacks.Query;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.query.CounterResponse;
import ar.edu.itba.pod.grpc.query.Filters;
import ar.edu.itba.pod.grpc.query.ListCheckIn;
import ar.edu.itba.pod.grpc.query.ListCounterResponse;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class QueryClientCallback extends CustomFutureCallback<ListCheckIn> {

    private final Filters filters;

    public QueryClientCallback(Logger logger, CountDownLatch latch, Filters filters) {
        super(logger, latch);
        this.filters = filters;
    }

    @Override
    public void onSuccess(ListCheckIn listCheckIn) {
        StringBuilder sb = new StringBuilder();


        System.out.println(sb);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }

}
