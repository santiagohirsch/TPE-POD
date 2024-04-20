package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.BoolValue;

import java.util.*;

public class Airport {
    private List<Sector> sectors;
    private List<Airline> airlines;

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
                return Optional.of(s.addCounters(cant));
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
}
