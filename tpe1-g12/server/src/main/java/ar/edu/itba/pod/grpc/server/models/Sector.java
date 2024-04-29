package ar.edu.itba.pod.grpc.server.models;



import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

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

    public Pair<Integer, Integer> assignCounters(String airline, List<Flight> airlineFlights, int count) {
        int start = -1;
        int end = -1;
        int currentCount = 0;

        // Check if any of the flight codes have been already assigned
        for (Map.Entry<Integer, Optional<Assignment>> entry : assignedCounters.entrySet()) {
            if (entry.getValue().isPresent()) {
                for (Flight flightCode : airlineFlights) {
                    if (entry.getValue().get().getFlightCodes().contains(flightCode)) {
                        //TODO exception
                        return new Pair<>(-1, -1); // Flight code already assigned
                    }
                }
            }
        }

        // Check if any of the flight codes are already pending
        for (Assignment assignment : pendingAssignments) {
            for (Flight flightCode : airlineFlights) {
                if (assignment.getFlightCodes().contains(flightCode)) {
                    return new Pair<>(-1, -1); // Flight code is already pending
                }
            }
        }



        // Create the assignment
        Assignment newAssignment = new Assignment(airline, airlineFlights, count);

        // Search for contiguous counters
        for (Map.Entry<Integer, Optional<Assignment>> entry : assignedCounters.entrySet()) {
            if (entry.getValue().isEmpty()) {
                // Counter is available
                if (start == -1) {
                    // Start of a potential block of counters
                    start = entry.getKey();
                }
                if(entry.getKey() != start+currentCount){
                    start=entry.getKey();
                    currentCount=0;
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


    public Optional<Assignment> freeCounters(int from, String airline) {
        int toRemove = -1;
        Optional<Assignment> toReturn = Optional.empty();

        if (assignedCounters.containsKey(from) &&
                assignedCounters.get(from).isPresent() &&
                assignedCounters.get(from).get().getAirline().equals(airline)) {
            if (!assignedCounters.containsKey(from - 1) ||
                    assignedCounters.get(from - 1).isEmpty() ||
                    !assignedCounters.get(from - 1).get().equals(assignedCounters.get(from).get())) {
                toRemove = from;
                toReturn = assignedCounters.get(from);
            }
        } else {
            return toReturn;
        }
        assignedCounters.get(toRemove).get().getFlightCodes().forEach((flight -> flight.setAlreadyCheckedIn(true)));
        while (assignedCounters.containsKey(toRemove) && assignedCounters.get(toRemove).isPresent() && assignedCounters.get(toRemove).get().equals(toReturn.get())){

            assignedCounters.replace(toRemove, Optional.empty());
            toRemove += 1;
        }

        return toReturn;
    }

    public Optional<Queue<Assignment>> listPendingAssignments() {
        return Optional.of(pendingAssignments);
    }

    public List<CounterInfoModel> getCounterInfo(Pair<Integer, Integer> interval) {
        List<CounterInfoModel> toReturn = new ArrayList<>();
        boolean isWithinInterval;
        Assignment previous = null;
        int emptyLowerBound = -1;
        int emptyUpperBound = -1;
        int nextEmptyLowerBound = -1;

        for (Map.Entry<Integer, Optional<Assignment>> entry : this.assignedCounters.entrySet()) {
            isWithinInterval = interval.getLeft() == -1 ? true :  entry.getKey() >= interval.getLeft() && entry.getKey() <= interval.getRight();

            if (isWithinInterval && entry.getValue().isPresent() && !entry.getValue().get().equals(previous)) {
                previous = entry.getValue().get();
                int lowerBound = entry.getKey();
                int upperBound = lowerBound + previous.getCant() - 1;

                toReturn.add(new CounterInfoModel(new Pair<>(lowerBound, upperBound), previous.getAirline(), previous.getFlightCodes().stream().map(Flight::getFlightCode).toList(), previous.getFlightCodes().stream().map(Flight::getBookings).mapToInt(List::size).sum()));
            }

            if (isWithinInterval && entry.getValue().isEmpty() && emptyLowerBound != -1) {
                if (entry.getKey() != emptyUpperBound + 1) {
                    nextEmptyLowerBound = entry.getKey();
                } else {
                    emptyUpperBound = entry.getKey();
                }
            }

            if (isWithinInterval && entry.getValue().isEmpty() && emptyLowerBound == -1) {
                emptyLowerBound = entry.getKey();
                emptyUpperBound = entry.getKey();
            }

            if (entry.getValue().isPresent() && emptyLowerBound != -1 || nextEmptyLowerBound != -1) {
                toReturn.add(new CounterInfoModel(new Pair<>(emptyLowerBound, emptyUpperBound), "", Collections.emptyList(), 0));
                emptyLowerBound = nextEmptyLowerBound;
                emptyUpperBound = nextEmptyLowerBound;
                nextEmptyLowerBound = -1;
            }
        }

        if (emptyLowerBound != -1 && emptyUpperBound != -1) {
            toReturn.add(new CounterInfoModel(new Pair<>(emptyLowerBound, emptyUpperBound), "", Collections.emptyList(), 0));
        }

        return toReturn;
    }

    public Map<Assignment, Pair<Integer,Integer>> solvePendingAssignments(){
        Map<Assignment, Pair<Integer,Integer>> toReturn = Collections.synchronizedMap(new LinkedHashMap<>());
        for (Assignment assignment : pendingAssignments){
            Pair<Integer, Integer> interval = assignCountersPending(assignment);
            if(interval.getLeft()!=0){
                toReturn.put(assignment, interval);
            }
        }
//        for (Assignment assignment : toReturn.keySet()) {
//            pendingAssignments.remove(assignment);
//        }
        return toReturn;
    }

    public List<Pair<Assignment,Integer>> removeFromPending(Assignment assignment) {
        List<Pair<Assignment,Integer>> toReturn = new ArrayList<>();
        int i = 0;
        int index=-1;
        for (Assignment a : pendingAssignments){
            if(index!=-1){
                toReturn.add(new Pair<>(a, i - 1));
            }
            if(a.equals(assignment)){
                index=i;
            }
            i++;
        }
        pendingAssignments.remove(assignment);
        return toReturn;
    }

    public Pair<Integer,Integer> assignCountersPending(Assignment newAssignment) {
        int count = newAssignment.getCant();
//        String airline, List<Flight> airlineFlights, int count
        int start = -1;
        int end = -1;
        int currentCount = 0;



        // Search for contiguous counters
        for (Map.Entry<Integer, Optional<Assignment>> entry : assignedCounters.entrySet()) {
            if (entry.getValue().isEmpty()) {
                // Counter is available
                if (start == -1) {
                    // Start of a potential block of counters
                    start = entry.getKey();
                }
                if(entry.getKey() != start+currentCount){
                    start=entry.getKey();
                    currentCount=0;
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
            //pendingAssignments.add(newAssignment);
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