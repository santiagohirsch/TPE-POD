package ar.edu.itba.pod.grpc.server.utils;

import java.util.List;

public class CounterInfoModel {
    private final Pair<Integer, Integer> interval;
    private final String airline;
    private final List<String> flightCodes;
    private final int people;
    private String sector;

    public CounterInfoModel(Pair<Integer, Integer> interval, String airline, List<String> flightCodes, int people) {
        this.interval = interval;
        this.airline = airline;
        this.flightCodes = flightCodes;
        this.people = people;
    }

    public CounterInfoModel(Pair<Integer, Integer> interval, String airline, List<String> flightCodes, int people, String sector) {
        this.interval = interval;
        this.airline = airline;
        this.flightCodes = flightCodes;
        this.people = people;
        this.sector = sector;
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

    public String getSector() {
        return sector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        CounterInfoModel counter = (CounterInfoModel) obj;
        return counter.getAirline().equals(this.getAirline()) && counter.getFlightCodes().equals(this.getFlightCodes()) && counter.getInterval().equals(this.getInterval()) && counter.getPeople() == this.getPeople();
    }
}
