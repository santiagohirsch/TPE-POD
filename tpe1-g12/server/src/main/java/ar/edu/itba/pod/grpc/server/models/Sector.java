package ar.edu.itba.pod.grpc.server.models;



import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.*;

public class Sector  {
    private final String name;
    private Map<Integer, Optional<Assignment>> assignedCounters;
    private Queue<Assignment> pendingAssignments;

    private Queue<Assignment> finishedAssignments;


    public Sector(String name) {
        this.name = name;
        this.assignedCounters = new HashMap<>();
        this.pendingAssignments = new LinkedList<>();
        this.finishedAssignments = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Optional<Assignment>> getAssignedCounters() {
        return assignedCounters;
    }

    public void setAssignedCounters(Map<Integer, Optional<Assignment>> assignedCounters) {
        this.assignedCounters = assignedCounters;
    }

    public Pair<Integer, Integer> addCounters(int cant, int nextAvailableCounter) throws IllegalArgumentException {
        if (cant < 0) {
            throw new IllegalArgumentException("The amount of counters can't be negative");
        }
        int start = -1;
        int end = -1;

        for (int i = 0; cant >= 0; i++) {
            if (!assignedCounters.containsKey(i + nextAvailableCounter)) {
                if (start == -1) {
                    start = i + nextAvailableCounter;
                }
                assignedCounters.put(i + nextAvailableCounter, Optional.empty());
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

    public Pair<Integer, Integer> assignCounters(String airline, List<String> flightCodes, int count) {
        int start = -1;
        int end = -1;
        int currentCount = 0;

        // Check if any of the flight codes have been already assigned
        for (Map.Entry<Integer, Optional<Assignment>> entry : assignedCounters.entrySet()) {
            if (entry.getValue().isPresent()) {
                for (String flightCode : flightCodes) {
                    if (entry.getValue().get().getFlightCodes().contains(flightCode)) {
                        return new Pair<>(-1, -1); // Flight code already assigned
                    }
                }
            }
        }

        // Check if any of the flight codes are already pending
        for (Assignment assignment : pendingAssignments) {
            for (String flightCode : flightCodes) {
                if (assignment.getFlightCodes().contains(flightCode)) {
                    return new Pair<>(-1, -1); // Flight code is already pending
                }
            }
        }

        // Create the assignment
        Assignment newAssignment = new Assignment(airline, flightCodes, count);

        // Search for contiguous counters
        for (Map.Entry<Integer, Optional<Assignment>> entry : assignedCounters.entrySet()) {
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
            // Add the assignment as pending
            pendingAssignments.add(newAssignment);
            return new Pair<>(0, 0);
        } else {
            // Mark the assigned counters for the flight codes
            // Add the assignment to its assignedCounters
            for (int i = start; i <= end; i++) {
                assignedCounters.put(i, Optional.of(newAssignment));
            }
            return new Pair<>(start, end);
        }
    }

    public int getPendingAhead(Assignment assignment) {
        int count = 0;
        for (Assignment pendingAssignment : pendingAssignments) {
            if (pendingAssignment.equals(assignment)) {
                // Found the given assignment, stop counting
                break;
            }
            count++;
        }
        return count;
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

    public Optional<Pair<Integer,List<String>>> freeCounters(int from, String airline) {
        boolean verifiedCounter = assignedCounters.get(from).isPresent() && assignedCounters.get(from).get().getAirline().equals(airline);
        int i = 0;
        List<String> flights = assignedCounters.get(from).get().getFlightCodes();
        while(verifiedCounter) {
            finishedAssignments.add(assignedCounters.get(from + i).get()); //todo ver si sirve dsp
            assignedCounters.remove(from + i);
            i++;
            verifiedCounter =  assignedCounters.get(from + i).isPresent() &&  assignedCounters.get(from + i).get().getAirline().equals(airline);
        }
        return Optional.of(new Pair<>(i,flights));
    }

    public Optional<Queue<Assignment>> listPendingAssignments() {
        return Optional.of(pendingAssignments);
    }
}