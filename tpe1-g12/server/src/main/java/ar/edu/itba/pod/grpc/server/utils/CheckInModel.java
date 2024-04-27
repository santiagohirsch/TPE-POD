package ar.edu.itba.pod.grpc.server.utils;

import java.util.List;

public class CheckInModel {

    private final Pair<Integer, Integer> interval;
    private final String airline;
    private final String flightCode;
    private final int peopleAhead;

    public CheckInModel(Pair<Integer, Integer> interval, String airline, String flightCode, int peopleAhead) {
        this.interval = interval;
        this.airline = airline;
        this.flightCode = flightCode;
        this.peopleAhead = peopleAhead;
    }


    public Pair<Integer, Integer> getInterval() {
        return interval;
    }

    public String getAirline() {
        return airline;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public int getPeopleAhead() {
        return peopleAhead;
    }
}
