package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssignCountersCallback extends CustomFutureCallback<AssignCounterResponse> {


    public AssignCountersCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }
    @Override
    public void onSuccess(AssignCounterResponse assignCounterResponse) {
        StringBuilder sb = new StringBuilder();
        if (assignCounterResponse.getAssignedInterval().getLowerBound() == -1) {
            sb.append("The assignation could not be done");
            System.out.println(sb);
            getLatch().countDown();
        } else if (assignCounterResponse.getAssignedInterval().getLowerBound() == 0) {
            sb.append(assignCounterResponse.getInfo().getCount())
                    .append(" counters")
                    .append(" in Sector ")
                    .append(assignCounterResponse.getInfo().getSector().getName())
                    .append(" is pending with ")
                    .append(assignCounterResponse.getPendingAhead())
                    .append(" other pendings ahead");
            System.out.println(sb);
            getLatch().countDown();
        } else {
            sb.append(assignCounterResponse.getInfo().getCount())
                    .append(" counters ")
                    .append("(")
                    .append(assignCounterResponse.getAssignedInterval().getLowerBound())
                    .append("-")
                    .append(assignCounterResponse.getAssignedInterval().getUpperBound())
                    .append(")")
                    .append(" in Sector ")
                    .append(assignCounterResponse.getInfo().getSector().getName())
                    .append(" are now checking in passengers from ")
                    .append(assignCounterResponse.getInfo().getAirline())
                    .append(" ");
            Iterator<String> it = assignCounterResponse.getInfo().getFlightCodesList().stream().toList().iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append("|");
                }
            }
            sb.append(" flights");
            System.out.println(sb);
            getLatch().countDown();
        }
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
