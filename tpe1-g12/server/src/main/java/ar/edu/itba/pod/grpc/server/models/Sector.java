package ar.edu.itba.pod.grpc.server.models;

import java.util.HashMap;
import java.util.Map;

public class Sector {
    private final String name;
    private Map<Integer, Airline> counters;


    public Sector(String name) {
        this.name = name;
        this.counters = new HashMap<>();
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