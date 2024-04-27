package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.counter.CounterResponse;
import ar.edu.itba.pod.grpc.counter.ListCheckInResponse;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class CheckInCountersCallback extends CustomFutureCallback<ListCheckInResponse> {

    public CheckInCountersCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }
}
