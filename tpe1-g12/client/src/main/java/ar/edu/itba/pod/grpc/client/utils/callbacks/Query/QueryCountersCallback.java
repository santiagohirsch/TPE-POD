package ar.edu.itba.pod.grpc.client.utils.callbacks.Query;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.query.CounterResponse;
import ar.edu.itba.pod.grpc.query.Filters;
import ar.edu.itba.pod.grpc.query.ListCounterResponse;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class QueryCountersCallback extends CustomFutureCallback<ListCounterResponse> {
    private final Filters filters;
    public QueryCountersCallback(Logger logger, CountDownLatch latch, Filters filters) {
        super(logger, latch);
        this.filters = filters;
    }

    @Override
    public void onSuccess(ListCounterResponse listCounterResponse) {
        StringBuilder sb = new StringBuilder();

        sb.append("Sector\t Counters\t Airline \t Flights \t People \n");
        sb.append("##########################################################\n");

        for (CounterResponse counterResponse : listCounterResponse.getCounterResponseList()) {
            sb.append(counterResponse.getSectorName()).append("\t");
            sb.append("(").append(counterResponse.getInterval().getLowerBound()).append("-").append(counterResponse.getInterval().getUpperBound()).append(")\t");
            sb.append(counterResponse.getAirline().isEmpty() ? "-\t" : counterResponse.getAirline()).append("\t");

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
            sb.append(counterResponse.getPassengers() != 0 ? counterResponse.getPassengers() : "-\t").append("\n");

        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filters.getOutPath()))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
