package ar.edu.itba.pod.grpc.server.models;

import java.util.List;

public class Airline {
    private final String name;
    private List<Flight> flights;

    public Airline(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
}
