package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.*;

public class Airport {
    private List<Sector> sectors;

    public Airport() {
        this.sectors = new ArrayList<>();
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

    public Optional<ar.edu.itba.pod.grpc.passenger.CounterInfo> fetchCounter(Booking request) {
        for (Sector sector: sectors) {
            for (Map.Entry<Integer, Optional<Airline>> counter: sector.getCounters().entrySet()) {
                if (counter.getValue().isPresent()) {
                    for (Flight flight : counter.getValue().get().getFlights()) {
                        for (String booking : flight.getBookings()) {
                            if (booking.equals(request.getBookingCode())) {
                                int lastCounter = 0;
                                for (int i = counter.getKey(); !counter.getValue().get().getFlights().contains(flight); i++) {
                                    lastCounter = i;
                                }
                                return Optional.of(CounterInfo.newBuilder().setAirline(counter.getValue().get().getName())
                                        .setFlightCode(flight.getFlightCode())
                                        .setCounters(Interval.newBuilder().setLowerBound(counter.getKey()).setUpperBound(lastCounter).build())
                                        .setSector(sector.getName())
                                        .setQueueLen(0)//todo
                                        .build());
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Optional<CheckInResponse> checkIn(CheckInInfo checkInInfo) {
        //todo falla si no existe el booking
        //No existe un sector con ese nombre
        //El número de mostrador no corresponde con el inicio de un rango de mostradores asignado a la aerolínea que esté aceptando pasajeros del vuelo de la reserva
        //El pasajero ya ingresó en la cola del rango
        //El pasajero ya realizó el check-in de la reserva
        for(Sector sector : sectors) {
            if(sector.getName().equals(checkInInfo.getSectorName())) {
                if(sector.getCounters().get(checkInInfo.getCounter()).isPresent() ) {
                    //List<Flight> flightsOnCounter = sector.getCounters().get(checkInInfo.getCounter()).get().getFlights();
                    /*for(int  i = checkInInfo.getCounter() ; sector.getCounters().get(i).isPresent() && containsFlightCode(sector.getCounters().get(i).get().getFlights(),flightsOnCounter) ; i++ ) {

                    }*/ //este for es para ver si ese mostrador contiene a los vuelos de el primero (pero deberiamos tener una manera mas facil de buscar los mostradores asignados al vuelo)
                    //todo if de que no este en la cola
                        //todo if de que ya hizo el check-in
                            return Optional.of(CheckInResponse.newBuilder()
                                            .setBooking(checkInInfo.getBooking())
                                            .setCounterInfo(CounterInfo.newBuilder()
                                                    .setSector(checkInInfo.getSectorName())
                                                    .setAirline(sector.getCounters().get(checkInInfo.getCounter()).get().getName())
                                                    .setFlightCode("ABC123" /*todo cuando pueda getear el vuelo dado el pasajero q hace la cola */)
                                                    .setQueueLen(0)
                                                    .build())
                                    .build());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<StatusResponse> status(Booking booking) {
        //todo falla si
        //No existe un pasajero esperado con ese código de reserva
        //No hay un rango de mostradores asignados que atiendan pasajeros del vuelo correspondiente al código de reserva indicado

        //todo buscar estado de check-in (sigue en la fila o no)

        return Optional.empty();
    }

    /*public static boolean containsFlightCode(List<Flight> flights1, List<Flight> flights2) {
        for (Flight flight1 : flights1) {
            for (Flight flight2 : flights2) {
                if (flight1.getFlightCode().equals(flight2.getFlightCode())) {
                    return true;
                }
            }
        }
        return false;
    }*/
}
