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

    public Pair<Integer, Integer> addCounters(int cant, int nextAvailableCounter) throws IllegalArgumentException {
        if (cant < 0) {
            throw new IllegalArgumentException("The amount of counters can't be negative");
        }
        int start = -1;
        int end = -1;

        for (int i = 0; cant >= 0; i++) {
            if (!counters.containsKey(i + nextAvailableCounter)) {
                if (start == -1) {
                    start = i + nextAvailableCounter;
                }
                counters.put(i + nextAvailableCounter, Optional.empty());
                cant--;
                if (cant == 0) {
                    end = i + nextAvailableCounter;
                    break;
                }
            } else if (start != -1) {
                start = -1;
            }
        }

        return new Pair<>(start, end);
    }

    public Pair<Integer, Integer> assignCounters(Airline airline, int count) {
        int start = -1;
        int end = -1;
        int currentCount = 0;

        for (Map.Entry<Integer, Optional<Airline>> entry : counters.entrySet()) {
            if (entry.getValue().isEmpty()) {
                // Counter is available
                if (start == -1) {
                    // Start of a potential block of counters
                    start = entry.getKey();
                }
                currentCount++;

                if (currentCount == count) {
                    // Found a block of 'count' consecutive counters
                    end = entry.getKey();
                    break;
                }
            } else {
                // Counter is not available, reset currentCount and start
                start = -1;
                currentCount = 0;
            }
        }

        if (end == -1) {
            // Unable to find a contiguous block of counters
            return new Pair<>(0, 0);
        } else {
            // Mark the assigned counters for the airline
            for (int i = start; i <= end; i++) {
                counters.put(i, Optional.of(airline));
            }
            return new Pair<>(start, end);
        }
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