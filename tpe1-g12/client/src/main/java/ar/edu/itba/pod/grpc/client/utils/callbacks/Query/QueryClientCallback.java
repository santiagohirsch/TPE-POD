package ar.edu.itba.pod.grpc.client.utils.callbacks.Query;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.query.*;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

        sb.append("Sector\t Counter\t Airline\t Flight\t Booking\n");
        sb.append("##########################################################\n");

        for(CheckIn checkIn : listCheckIn.getCheckInList()) {
            sb.append(checkIn.getSectorName()).append("\t");
            sb.append(checkIn.getCounter()).append("\t");
            sb.append(checkIn.getAirline()).append("\t");
            sb.append(checkIn.getFlightCode()).append("\t");
            sb.append(checkIn.getBookingCode()).append("\n");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filters.getOutPath()))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
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
