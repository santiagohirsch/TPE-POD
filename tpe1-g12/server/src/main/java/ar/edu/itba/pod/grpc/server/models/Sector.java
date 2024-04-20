package ar.edu.itba.pod.grpc.server.models;



import ar.edu.itba.pod.grpc.admin.Interval;
import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.*;

public class Sector  {
    private final String name;
    private Map<Integer, Optional<Airline>> counters;


    public Sector(String name) {
        this.name = name;
        this.counters = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Optional<Airline>> getCounters() {
        return counters;
    }

    public void setCounters(Map<Integer, Optional<Airline>> counters) {
        this.counters = counters;
    }

    public Pair<Integer, Integer> addCounters(int cant) throws IllegalArgumentException {
        if (cant < 0) {
            throw new IllegalArgumentException("The amount of counters can't be negative");
        }
        int start = -1;
        int end = -1;

        for (int i = 0; cant >= 0; i++) {
            if (!counters.containsKey(i)) {
                if (start == -1) {
                    start = i;
                }
                counters.put(i, Optional.empty());
                cant--;
                if (cant == 0) {
                    end = i;
                    break;
                }
            } else if (start != -1) {
                start = -1;
            }
        }

        return new Pair<>(start, end);
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
        return this.name.equals(other.name);
    }
}