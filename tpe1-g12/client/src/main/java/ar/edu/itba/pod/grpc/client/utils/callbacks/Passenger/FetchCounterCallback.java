package ar.edu.itba.pod.grpc.client.utils.callbacks.Passenger;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.*;
import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.passenger.CounterInfo;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class FetchCounterCallback extends CustomFutureCallback<CounterInfo> {

    public FetchCounterCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterInfo counterInfo) {
        String out;

        out = "Flight " + counterInfo.getFlightCode() + " from " + counterInfo.getAirline() + " is now checking in at counters " + counterInfo.getCounters().getLowerBound() + " - " + counterInfo.getCounters().getUpperBound() + " in Sector " + counterInfo.getSector() + " with " + counterInfo.getQueueLen() + " people in line";
        if(counterInfo.getCounters().getLowerBound()==-1){
            //   Flight AA123 from AmericanAirlines has no counters assigned yet
            out = "Flight " + counterInfo.getFlightCode() + " from " + counterInfo.getAirline() + " has no counters assigned yet";
        }
        System.out.println(out);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        getLatch().countDown();
    }
}
