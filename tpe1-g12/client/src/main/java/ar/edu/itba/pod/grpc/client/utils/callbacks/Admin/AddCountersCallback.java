package ar.edu.itba.pod.grpc.client.utils.callbacks.Admin;

import ar.edu.itba.pod.grpc.admin.CounterResponse;
import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddCountersCallback extends CustomFutureCallback<CounterResponse> {

    public AddCountersCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterResponse resp) {
        String out = resp.getCount() + " new counters (" + resp.getInterval().getLowerBound() + "-" + resp.getInterval().getUpperBound() + ") in Sector " + resp.getSector().getName() + " added successfully";
        System.out.println(out);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        getLatch().countDown();
    }
}
