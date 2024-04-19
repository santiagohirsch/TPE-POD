package ar.edu.itba.pod.grpc.models;

import java.util.Map;

public class Sector {
    private final String name;
    private Map<Integer,Airline> counters;


    public Sector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Airline> getCounters() {
        return counters;
    }

    public void setCounters(Map<Integer, Airline> counters) {
        this.counters = counters;
    }
}
