package ar.edu.itba.pod.grpc.server.utils;

import java.util.List;

public class CounterInfoBookingModel {

    private final Pair<Integer, Integer> interval;
    private final String airline;
    private final int people;

    private final String sector;

    private final String flightCode;

    public CounterInfoBookingModel(Pair<Integer, Integer> interval, String airline, int people, String sector, String flightCode) {
        this.interval = interval;
        this.airline = airline;
        this.people = people;
        this.sector = sector;
        this.flightCode = flightCode;
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

    public int getPeople() {
        return people;
    }

    public String getSector() { return sector; }

}
