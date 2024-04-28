package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.event.Notification;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationCenter {

    private final Map<String, StreamObserver<Notification>> registeredAirlines = new ConcurrentHashMap<>();
    private final Lock notificationLock = new ReentrantLock();

    public void registerAirline(String airline, StreamObserver<Notification> observer) {
        notificationLock.lock();
        try {
            if (registeredAirlines.containsKey(airline)) {
                //TODO exceptions
                throw new IllegalArgumentException("Airline has already been registered for notifications");
            } else {
                registeredAirlines.put(airline, observer);
            }
        } finally {
            notificationLock.unlock();
        }
    }

    public StreamObserver<Notification> unRegisterAirline(String airline) {
        notificationLock.lock();
        try {
            if (!registeredAirlines.containsKey(airline)) {
                //TODO exceptions
                throw new IllegalArgumentException("Airline was not registered for notifications");
            } else {
                StreamObserver<Notification> toReturn = registeredAirlines.get(airline);
                registeredAirlines.remove(airline);

                return toReturn;
            }
        } finally {
            notificationLock.unlock();
        }
    }

    public void notify(String airline, String message) {
        notificationLock.lock();
        try {
            if (registeredAirlines.containsKey(airline)) {
                registeredAirlines.get(airline).onNext(Notification.newBuilder().setMessage(message).build());
            }
        } finally {
            notificationLock.unlock();
        }
    }

    public void notifyRegistration(String airline) {
        notify(airline, String.format("%s registered successfully for check-in events"
                , airline));
    }

    public void notifyAssignCounters(String airline, int countersCant, Pair<Integer, Integer> interval, String sectorName, List<String> flightCodes) {
        StringBuilder flightCodesString = new StringBuilder();
        Iterator<String> it = flightCodes.iterator();
        while (it.hasNext()) {
            flightCodesString.append(it.next());
            if (it.hasNext()) {
                flightCodesString.append("|");
            }
        }


        notify(airline, String.format("%d counters (%d-%d) in Sector %s are now checking in passengers from %s %s flights"
                , countersCant, interval.getLeft(), interval.getRight(), sectorName, airline, flightCodesString));
    }

    public void notifyIntoQueue(String airline, String booking, Pair<Integer, Integer> interval, String sectorName, String flightCode, Integer people) {
        notify(airline, String.format("Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line"
                , booking, flightCode, airline, sectorName, airline, interval.getLeft(), interval.getRight(), sectorName, people));
    }

    public void notifyCheckIn(String airline, String bookingCode, String flightCode, int counter, String sector) {
        notify(airline, String.format("Check-in successful of %s for flight %s at counter %d in Sector %s"
                , bookingCode, flightCode, counter, sector));
    }

    public void notifyFreeCounters(String airline, List<String> flightCodes, Pair<Integer, Integer> interval, String sector) {
        StringBuilder flightCodesString = new StringBuilder();
        Iterator<String> it = flightCodes.iterator();
        while (it.hasNext()) {
            flightCodesString.append(it.next());
            if (it.hasNext()) {
                flightCodesString.append("|");
            }
        }

        notify(airline, String.format("Ended check-in for flights %s on counters (%d-%d) from Sector %s"
                , flightCodesString, interval.getLeft(), interval.getRight(), sector));
    }

    public void notifyPending(String airline, Integer cant, String sectorName,List<String> flightCodes , Integer pending) {
        StringBuilder flightCodesString = new StringBuilder();
        Iterator<String> it = flightCodes.iterator();
        while (it.hasNext()) {
            flightCodesString.append(it.next());
            if (it.hasNext()) {
                flightCodesString.append("|");
            }
        }

        notify(airline, String.format("%d counters in Sector %s for flights %s is pending with %d other pendings ahead"
                , cant, sectorName, flightCodesString, pending));
    }

    }
