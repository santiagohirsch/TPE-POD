package ar.edu.itba.pod.grpc.client.utils.callbacks;

import ar.edu.itba.pod.grpc.admin.Booking;
import com.google.protobuf.BoolValue;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddBookingCallback extends CustomFutureCallback<BoolValue>{
    private final Booking booking;

    public AddBookingCallback(Logger logger, CountDownLatch latch, Booking booking) {
        super(logger, latch);
        this.booking = booking;
    }

    @Override
    public void onSuccess(BoolValue boolValue) {
        String out;
        if(boolValue.getValue()){
            out = "Booking " + booking.getBookingCode() + " for " +
                    booking.getAirline() + " " + booking.getFlightCode() + " added successfuly";
        }else{
            out= "Error on Booking " + booking.getBookingCode() + " for " +
                    booking.getAirline() + " " + booking.getFlightCode() ;
        }

        System.out.println(out);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
