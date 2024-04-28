package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.event.RegisterInfo;
import ar.edu.itba.pod.grpc.counter.CounterInfoResponse;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import ar.edu.itba.pod.grpc.passenger.*;
import ar.edu.itba.pod.grpc.server.utils.*;

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

    public void checkIfAirlineExists(String airline) {
        if(!this.airlines.contains(new Airline(airline))){
            //TODO excepciones
            throw new IllegalArgumentException("no existe la aerolinea");
        }
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

    public Optional<CounterInfoBookingModel> fetchCounter(String bookingCode) {
        for (Sector sector: sectors) {
            for (Map.Entry<Integer, Optional<Assignment>> counter: sector.getAssignedCounters().entrySet()) {
                if (counter.getValue().isPresent()) {
                    for (Flight flight : counter.getValue().get().getFlightCodes()) {
                        for (String booking : flight.getBookings()) {
                            if (booking.equals(bookingCode)) {
                                return Optional.of(new CounterInfoBookingModel(new Pair<>(counter.getKey(), counter.getValue().get().getCant() + counter.getKey() - 1), counter.getValue().get().getAirline(), counter.getValue().get().getCheckInQueue().size(), sector.getName(), flight.getFlightCode()));
                            }
                        }
                    }
                }
            }
        }


        for(Sector sector: sectors){
            for(Airline airline: airlines){
                for(Flight flight: airline.getFlights()){
                    for(String booking: flight.getBookings()){
                        if(booking.equals(bookingCode)){
                            return Optional.of(new CounterInfoBookingModel(new Pair<>(-1, -1), airline.getName(), 0, sector.getName(), flight.getFlightCode()));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Optional<CheckInModel> intoQueue(String bookingCode, Sector s, int initialCounter) {

        for (Sector sector: sectors) {
            //Chequeo que haya un sector
            if(sector.equals(s)){
                for (Map.Entry<Integer, Optional<Assignment>> counter: sector.getAssignedCounters().entrySet()) {
                    if (counter.getValue().isPresent()) {
                        for (Flight flight : counter.getValue().get().getFlightCodes()) {
                            for (String booking : flight.getBookings()) {
                                // Chequeo si existe un booking
                                if (booking.equals(bookingCode)) {
                                    //Chequeo si el mostrador es el correcto
                                    if (initialCounter==counter.getKey()){
                                        // Chequeo si ya esta haciendo la cola o si ya hizo check in
                                        if(counter.getValue().get().getCheckInQueue().contains(bookingCode) || counter.getValue().get().hasCheckedIn(bookingCode)){
                                            return Optional.empty();
                                        }
                                        counter.getValue().get().addToQueue(bookingCode);
                                        int lastCounter = 0;
                                        for (int i = counter.getKey(); !counter.getValue().get().getFlightCodes().contains(flight); i++) {
                                            lastCounter = i;
                                        }
                                        return Optional.of(new CheckInModel(new Pair<>(counter.getKey(), counter.getKey() + counter.getValue().get().getCant() - 1), counter.getValue().get().getAirline(), flight.getFlightCode(), counter.getValue().get().getCheckInQueue().size(), bookingCode));

                                    }

                                }
                            }
                        }
                    }
                }
            }

        }

        return Optional.empty();
    }

    public Optional<CheckInStatusModel> status(String bookingCode) {
        for (Sector sector: sectors) {
            for (Map.Entry<Integer, Optional<Assignment>> counter: sector.getAssignedCounters().entrySet()) {
                if (counter.getValue().isPresent()) {
                    for (Flight flight : counter.getValue().get().getFlightCodes()) {
                        for (String booking : flight.getBookings()) {
                            if (booking.equals(bookingCode)) {
                                Assignment counterInfo = counter.getValue().get();
                                //Ya se chequeo
                                if(counterInfo.hasCheckedIn(bookingCode)){

                                    return Optional.of(new CheckInStatusModel(new Pair<>(-1, -1), counter.getValue().get().getAirline(), flight.getFlightCode(), 0, sector.getName(), counterInfo.getCounter(bookingCode),0, bookingCode));
                                }
                                int lastCounter = 0;
                                for (int i = counter.getKey(); !counter.getValue().get().getFlightCodes().contains(flight); i++) {
                                    lastCounter = i;
                                }
                                //esta en la cola
                                if(counterInfo.getCheckInQueue().contains(bookingCode)){

                                    return Optional.of(new CheckInStatusModel(new Pair<>(counter.getKey(), counter.getKey() + counter.getValue().get().getCant() - 1), counter.getValue().get().getAirline(), flight.getFlightCode(), counter.getValue().get().getCheckInQueue().size(), sector.getName(),0,1, bookingCode));
                                }
                                //ni esta en la cola
                                return Optional.of(new CheckInStatusModel(new Pair<>(counter.getKey(), counter.getKey() + counter.getValue().get().getCant() - 1), counter.getValue().get().getAirline(), flight.getFlightCode(), 0, sector.getName(),0,2, bookingCode));


                            }
                        }
                    }
                }
            }
        }

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


            //TODO EXCEPTIONS!
            for (Flight flight : a.getFlights()) {
                if (flightCodes.contains(flight.getFlightCode())) {
                    if (!a.getName().equals(airline)) {
                        return new Pair<>(-1, -1);
                    } else if (flight.getBookings().isEmpty()) {
                        return new Pair<>(-1, -1);
                    } else if(flight.isAlreadyCheckedIn()) {
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

    public List<CheckInResponseModel> checkInCounters(String sectorName, int from, String airline) {

        for (Sector sector: sectors) {
            //Chequeo que haya un sector
            if(sector.getName().equals(sectorName)){
                Optional<Assignment> assignment = sector.getAssignedCounters().getOrDefault(from, Optional.empty());
                if (assignment.isEmpty()){
                    // NO EXISTE COUNTER
                    // TODO exception
                    return Collections.emptyList();
                }
                if (assignment.get().getAirline().equals(airline)){
                    return assignment.get().checkAll(from);
                } else {
                    //existe pero en otra airline
                    // TODO exception
                    return Collections.emptyList();
                }
            }

        }

        // no existe el sector
        // TODO excetption
        return Collections.emptyList();
    }
}
