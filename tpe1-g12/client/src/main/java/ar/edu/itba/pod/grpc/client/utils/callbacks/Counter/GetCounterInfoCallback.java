package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.counter.CounterResponse;
import ar.edu.itba.pod.grpc.counter.ListCheckInResponse;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class GetCounterInfoCallback extends CustomFutureCallback<CounterInfoResponse> {

    public GetCounterInfoCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterInfoResponse counterInfoResponse) {
        StringBuilder sb = new StringBuilder();
        //todo: hacer mas linda la impresion
        sb.append("Counters\t Airline \t Flights \t People \n");
        sb.append("##########################################################\n");
        for (CounterResponse counterResponse : counterInfoResponse.getCountersList()) {
            sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
            sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");
//                    sb.append(counterResponse.getAirline()).append("\t"); asi no imprime el -

            if (!counterResponse.getFlightCodeList().isEmpty()) {
                Iterator<String> it = counterResponse.getFlightCodeList().stream().toList().iterator();
                while (it.hasNext()) {
                    sb.append(it.next());
                    if (it.hasNext()) {
                        sb.append("|");
                    }
                }
            } else {
                sb.append("-\t");
            }

            sb.append("\t");
//                    sb.append(counterResponse.getPassengers()).append("\n");
            sb.append(counterResponse.getPassengers() != 0 ? counterResponse.getPassengers() : "-\t").append("\n");

        }

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
