package ar.edu.itba.pod.grpc.server.models;

import java.util.List;
import java.util.Objects;

public class Assignment {
    private final String airline;
    private final List<Flight> flightCodes;
    private final int cant;

    public Assignment(String airline, List<Flight> flightCodes, int cant) {
        this.airline = airline;
        this.flightCodes = flightCodes;
        this.cant = cant;
    }

    public String getAirline() {
        return airline;
    }

    public List<Flight> getFlightCodes() {
        return flightCodes;
    }

    public int getCant() {
        return cant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return cant == that.cant && airline.equals(that.airline) && flightCodes.equals(that.flightCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airline, flightCodes, cant);
    }
}
