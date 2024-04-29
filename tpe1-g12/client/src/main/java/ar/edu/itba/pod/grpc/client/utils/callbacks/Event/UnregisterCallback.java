package ar.edu.itba.pod.grpc.client.utils.callbacks.Event;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.event.Notification;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class UnregisterCallback extends CustomFutureCallback<Notification> {
    public UnregisterCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(Notification notification) {
        System.out.println(notification.getMessage());
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
