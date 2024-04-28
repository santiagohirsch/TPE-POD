package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.counter.CounterResponse;
import ar.edu.itba.pod.grpc.counter.*;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class CheckInCountersCallback extends CustomFutureCallback<ListCheckInResponse> {

    public CheckInCountersCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);

    }

    @Override
    public void onSuccess(ListCheckInResponse listCheckInResponse) {
        StringBuilder sb = new StringBuilder();
        for (CheckInResponse checkInResponse : listCheckInResponse.getInfoList()) {
            if (checkInResponse.getFlightCode().isEmpty()){
                sb.append("Counter " + checkInResponse.getCounter() + " is idle\n");
            } else {
                sb.append("Check-in successful of " + checkInResponse.getCheckinCode() + " for flight " + checkInResponse.getFlightCode() + " at counter " + checkInResponse.getCounter() + "\n");
            }
        }
        System.out.println(sb);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
