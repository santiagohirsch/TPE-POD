package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.*;

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
            for (Integer counter : sector.getAssignedCounters().keySet()) {
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

        String targetAirline = null;
        List<Flight> airlineFlights = new ArrayList<>();

        for (Airline a : this.airlines) {


            for (Flight flight : a.getFlights()) {
                if (flightCodes.contains(flight.getFlightCode())) {
                    if (!a.getName().equals(airline)) {
                        return new Pair<>(-1, -1);
                    } else if (flight.getBookings().isEmpty()) {
                        return new Pair<>(-1, -1);
                    } else {
                        airlineFlights.add(flight);
                    }
                }
            }
            if (a.getName().equals(airline)) {
                if (a.getFlights().stream().map(Flight::getFlightCode).toList().containsAll(flightCodes)) {
                    targetAirline = airline;
                }
            }
        }

        if (targetAirline == null) {
            return new Pair<>(-1, -1);
        }


        return targetSector.assignCounters(targetAirline, airlineFlights, count);
    }

    public int getPendingAhead(String sector, List<String> flightCodes, String airline, int count) {
        return this.sectors.get(this.sectors.indexOf(new Sector(sector))).getPendingAhead(new Assignment(airline, flightCodes.stream().map(Flight::new).toList(), count));
    }

    public Optional<Assignment>  freeCounters(String sector, int from, String airline) {
        Sector targetSector = getSectorByName(sector);
        if(targetSector == null) {
            return Optional.empty(); //todo checkear
        }

        return targetSector.freeCounters(from,airline);
    }

    public Optional<Queue<Assignment>> listPendingAssignments(String sectorName) {
        Sector targetSector = getSectorByName(sectorName);
        if(targetSector == null) {
            return Optional.empty(); //todo deberia fallar!
        }
        return targetSector.listPendingAssignments();
    }

     private Sector getSectorByName(String sectorName) {
        Sector targetSector = null;
        for (Sector s : this.sectors) {
            if (s.getName().equals(sectorName)) {
                targetSector = s;
                break;
            }
        }
        return targetSector;
    }

    public List<CounterInfoModel> getCounterInfo(String sectorName, Pair<Integer, Integer> interval) {
        Sector targetSector = getSectorByName(sectorName);
        if (targetSector == null || interval.getLeft() > interval.getRight()) {
            return null;            //todo deberia fallar!
        }

        if (targetSector.getAssignedCounters().isEmpty()) {
            return Collections.emptyList();
        }

        return targetSector.getCounterInfo(interval);

    }
}
