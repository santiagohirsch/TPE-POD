package ar.edu.itba.pod.grpc.client.utils.callbacks.Passenger;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import org.slf4j.Logger;
import ar.edu.itba.pod.grpc.passenger.*;

import java.util.concurrent.CountDownLatch;

public class PassengerCheckinCallback extends CustomFutureCallback<CheckInResponse> {
    public PassengerCheckinCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CheckInResponse checkInResponse) {
        String out;

        out = "Booking " + checkInResponse.getBooking().getBookingCode() + " for flight " + checkInResponse.getCounterInfo().getFlightCode() + " is now waiting to check-in on counters (" + checkInResponse.getCounterInfo().getCounters().getLowerBound() + "-" + checkInResponse.getCounterInfo().getCounters().getUpperBound() + ") in Sector " + checkInResponse.getCounterInfo().getSector() + " with " + checkInResponse.getCounterInfo().getQueueLen() + " people in line";
        //Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line

        System.out.println(out);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        //System.out.println(response);
        System.out.println("fallo");
        getLatch().countDown();
    }
}
