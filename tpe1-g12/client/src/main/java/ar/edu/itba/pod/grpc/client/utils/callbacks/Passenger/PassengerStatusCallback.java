package ar.edu.itba.pod.grpc.client.utils.callbacks.Passenger;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import org.slf4j.Logger;
import ar.edu.itba.pod.grpc.passenger.*;
import java.util.concurrent.CountDownLatch;

public class PassengerStatusCallback extends CustomFutureCallback<StatusResponse> {
    public PassengerStatusCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(StatusResponse statusResponse) {
        String out;

        // TODO agregar al statusResponse un int que sea el numero del counter en donde ya se chequeo

        if (statusResponse.getStatus()==0){
            //Booking ABC123 for flight AA123 from AmericanAirlines checked in at counter 4 in Sector C
            out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " checked in at counter " + statusResponse.getCheckinResponse().getCounter() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector();
        } else if(statusResponse.getStatus()==1){
            //Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line
            out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " is now waiting to check-in on counters " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getLowerBound() + " - " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getUpperBound() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector() + " with " + statusResponse.getCheckinResponse().getCounterInfo().getQueueLen() + " people in line";
        } else if(statusResponse.getStatus()==2){
            //Booking ABC123 for flight AA123 from AmericanAirlines can check-in on counters (3-4) in Sector C
            out = "Booking " + statusResponse.getCheckinResponse().getBooking().getBookingCode() + " for flight " + statusResponse.getCheckinResponse().getCounterInfo().getFlightCode() + " can check-in on counters " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getLowerBound() + " - " + statusResponse.getCheckinResponse().getCounterInfo().getCounters().getUpperBound() + " in Sector " + statusResponse.getCheckinResponse().getCounterInfo().getSector();
        } else {
            out = "error";
        }

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
