package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.BoolValue;

import java.util.*;
import java.util.stream.Collectors;

public class Airport {
    private List<Sector> sectors;
    private List<Airline> airlines;
    private static int nextAvailableCounter = 1;

    public Airport() {
        this.sectors = new ArrayList<>();
        this.airlines = new ArrayList<>();
    }

    public List<Airline> getAirlines() {
        return airlines;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public boolean addSector(Sector sector) {
        if (sectors.contains(sector) ) {
            return false;
        } else {
            sectors.add(sector);
            return true;
        }
    }

    public Optional<Pair<Integer,Integer>> addCounters(Sector sector, int cant){
        for (Sector s : sectors) {
            if (s.equals(sector)) {
                Optional<Pair<Integer,Integer>> toReturn = Optional.of(s.addCounters(cant, nextAvailableCounter));
                nextAvailableCounter = toReturn.get().getRight() + 1;
                return toReturn;
            }
        }
        return Optional.empty();
    }

    public synchronized boolean addBooking(String booking_code, String flight_code, String airline) {
        Airline target_airline = null;
        for (Airline a : this.airlines) {
            for(Flight flight : a.getFlights()) {
                if(Objects.equals(flight_code, flight.getFlightCode())) {
                    if(!Objects.equals(a.getName(), airline))
                        return false;

                }
                if(flight.getBookings().contains(booking_code))
                    return false;
            }
            if(Objects.equals(a.getName(), airline)) {
                target_airline = a;
            }
        }

        Flight new_flight = new Flight(flight_code);
        if (target_airline == null) {
            target_airline = new Airline(airline);
            target_airline.addFlight(new_flight);
            new_flight.addBooking(booking_code);
            this.airlines.add(target_airline);
        } else {
            if (!target_airline.getFlights().contains(new_flight)){
                new_flight.addBooking(booking_code);
                target_airline.addFlight(new_flight);
            } else {
                target_airline.addBookingToFlight(flight_code,booking_code);
            }
        }
        return true;
    }

    public Optional<Map<String, List<Pair<Integer, Integer>>>> listSectors() {
        if (this.sectors.isEmpty()) {
            return Optional.empty();
        }

        Map<String, List<Pair<Integer, Integer>>> countersPerSectors = new HashMap<>();

        for (Sector sector : this.sectors) {
            countersPerSectors.put(sector.getName(), new ArrayList<>());
            int start = -1;
            int end = -1;
            for (Integer counter : sector.getCounters().keySet()) {
                if (start == -1) {
                    start = counter;
                    end = counter;
                } else if (counter == end + 1) {
                    end = counter;
                } else {
                    countersPerSectors.get(sector.getName()).add(new Pair<>(start, end));
                    start = counter;
                    end = counter;
                }
            }
            // Add the last pair if there's any remaining counter
            if (start != -1) {
                countersPerSectors.get(sector.getName()).add(new Pair<>(start, end));
            }
        }
        return Optional.of(countersPerSectors);
    }

    public Pair<Integer, Integer> assignCounters(String sector, List<String> flightCodes, String airline, int count) {
        // (-1,-1) = error | (0, 0) = pending
        Sector targetSector = null;
        for (Sector s : this.sectors) {
            if (s.getName().equals(sector)) {
                targetSector = s;
                break;
            }
        }
        if (targetSector == null) {
            return new Pair<>(-1, -1);
        }

        Airline targetAirline = null;

        for (Airline a : this.airlines) {
            for (Flight flight : a.getFlights()) {
                if (flightCodes.contains(flight.getFlightCode())) {
                    if (flight.getBookings().isEmpty()) {
                        return new Pair<>(-1, -1);
                    } else if (!a.getName().equals(airline)) {
                        return new Pair<>(-1, -1);
                    }
                }
            }
            if (a.getName().equals(airline)) {
                if (flightCodes.containsAll(a.getFlights().stream().map(Flight::getFlightCode).toList())) {
                    targetAirline = a;
                }
            }
        }

        if (targetAirline == null) {
            return new Pair<>(-1, -1);
        }

        // TODO: check pending (add a queue of Assignments -> objects that contain all the info of a pending assignment)
        // TODO: check that none of the flights have been assigned before (change the counters Map in Sector from Airline to Assignment)
        Pair<Integer, Integer> assignedInterval = targetSector.assignCounters(targetAirline, count);
        if (assignedInterval.getLeft() == 0) {
            //TODO: add to pending
        }

        return assignedInterval;
    }
}
