package ar.edu.itba.pod.grpc.client.utils.callbacks;

import ar.edu.itba.pod.grpc.admin.SectorData;
import com.google.protobuf.BoolValue;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddSectorCallback extends CustomFutureCallback<BoolValue>{
    private final SectorData request;

    public AddSectorCallback(Logger logger, CountDownLatch latch, SectorData request) {
        super(logger, latch);
        this.request = request;
    }

    @Override
    public void onSuccess(BoolValue boolValue) {
        String out;
        if(!boolValue.getValue()){
            out = "Sector " + request.getName() + " already exists";
        }else{
            out = "Sector " + request.getName() + " added successfully";
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
