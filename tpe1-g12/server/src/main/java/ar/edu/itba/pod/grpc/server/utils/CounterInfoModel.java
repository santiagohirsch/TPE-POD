package ar.edu.itba.pod.grpc.server.utils;

import java.util.List;

public class CounterInfoModel {
    private final Pair<Integer, Integer> interval;
    private final String airline;
    private final List<String> flightCodes;
    private final int people;

    public CounterInfoModel(Pair<Integer, Integer> interval, String airline, List<String> flightCodes, int people) {
        this.interval = interval;
        this.airline = airline;
        this.flightCodes = flightCodes;
        this.people = people;
    }

    public Pair<Integer, Integer> getInterval() {
        return interval;
    }

    public String getAirline() {
        return airline;
    }

    public List<String> getFlightCodes() {
        return flightCodes;
    }

    public int getPeople() {
        return people;
    }
}
