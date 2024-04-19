package ar.edu.itba.pod.grpc.server.models;

import java.util.HashMap;
import java.util.Map;

public class Sector  {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Sector other = (Sector) obj;
        if (this.name.equals(other.name)) {
            return true;
        }

        return false;
    }
}