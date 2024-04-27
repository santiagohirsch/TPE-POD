package ar.edu.itba.pod.grpc.server.utils;

public class CheckInStatusModel {

    private final Pair<Integer, Integer> interval;

    private final Integer counter;
    private final String airline;
    private final String flightCode;
    private final int peopleAhead;

    private final String sector;
    private final Integer status;

    public CheckInStatusModel(Pair<Integer, Integer> interval, String airline, String flightCode, int peopleAhead, String sector, Integer counter, Integer status) {
        this.interval = interval;
        this.airline = airline;
        this.flightCode = flightCode;
        this.peopleAhead = peopleAhead;
        this.sector = sector;
        this.counter = counter;
        this.status = status;
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

    public String getSector() {
        return sector;
    }

    public Integer getStatus() {
        return status;
    }
}
