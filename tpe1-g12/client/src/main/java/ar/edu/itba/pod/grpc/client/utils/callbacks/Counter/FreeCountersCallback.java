package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.AssignCounterResponse;
import ar.edu.itba.pod.grpc.counter.FreeCounterResponse;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class FreeCountersCallback extends CustomFutureCallback<FreeCounterResponse> {

    public FreeCountersCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(FreeCounterResponse freeCounterResponse) {
        StringBuilder sb = new StringBuilder();
        if (freeCounterResponse.getFreedInterval().getLowerBound() != -1) {
            sb.append("Ended check-in for flights ");
            for (String flight : freeCounterResponse.getFlightCodesList()) {
                sb.append(flight);
                sb.append("|");
            }
            sb.deleteCharAt(sb.lastIndexOf("|"));
            sb.append(" on ");
            sb.append(freeCounterResponse.getFreedInterval().getUpperBound() - freeCounterResponse.getFreedInterval().getLowerBound() + 1);
            sb.append(" counters ");
            sb.append("(");
            sb.append(freeCounterResponse.getFreedInterval().getLowerBound());
            sb.append("-");
            sb.append(freeCounterResponse.getFreedInterval().getUpperBound());
            sb.append(") ");
            sb.append("in Sector ");
            sb.append(freeCounterResponse.getSector());
            System.out.println(sb);
            getLatch().countDown();
        }
        else {
            sb.append("Error");
            System.out.println(sb);
            getLatch().countDown();
        }
    }

    @Override
    public void onFailure(Throwable throwable){
        System.out.println("Fallo mal");
        getLatch().countDown();
    }
}
