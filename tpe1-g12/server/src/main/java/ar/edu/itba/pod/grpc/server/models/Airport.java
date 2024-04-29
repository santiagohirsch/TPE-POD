package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.exceptions.*;
import ar.edu.itba.pod.grpc.server.utils.CounterInfoModel;
import ar.edu.itba.pod.grpc.server.utils.Pair;
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
    private List<CheckInData> checkedInList = new ArrayList<>();

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
        Airline airlineObj = new Airline(airline);
        if(!this.airlines.contains(airlineObj)){
            throw new InvalidArgumentException("airline " + airline + " does not exist");
        }
        else if(airlineObj.getFlights().isEmpty()) {
            throw new InvalidArgumentException("airline " + airline + " has no flights");
        }
        else {
            int emptyQueues = 0;
            for(Flight flight : airlineObj.getFlights()){
                if (flight.getBookings().isEmpty())
                    emptyQueues++;
            }
            if(emptyQueues == airlineObj.getFlights().size()){
                throw new InvalidArgumentException("airline " + airline + " no passengers");
            }
        }
    }

    public boolean addSector(Sector sector) {
        if (sectors.contains(sector) ) {
            throw new SecurityException(String.format("Sector %s already exists", sector.getName()));
        } else {
            sectors.add(sector);
            return true;
        }
    }

    public Optional<Pair<Integer,Integer>> addCounters(Sector sector, int cant){
        if (cant <= 0) {
            throw new InvalidArgumentException("The amount of counters must be greater than 0");
        }
        for (Sector s : sectors) {
            if (s.equals(sector)) {
                Optional<Pair<Integer,Integer>> toReturn = Optional.of(s.addCounters(cant, nextAvailableCounter));
                nextAvailableCounter = toReturn.get().getRight() + 1;
                return toReturn;
            }
        }
        throw new InvalidArgumentException(String.format("Sector %s does not exist", sector.getName()));
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
        throw new InvalidArgumentException(String.format("Booking %s does not exists", bookingCode));
    }

    public Optional<CheckInModel> intoQueue(String bookingCode, Sector s, int initialCounter) {
        if(!sectors.contains(s)){
            throw new InvalidArgumentException(String.format("Sector %s does not exists", s.getName()));
        }
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
                                        if(counter.getValue().get().getCheckInQueue().contains(bookingCode) ){
                                            throw new PassangerAlreadyInQueueException("Passenger is already in the queue");
                                        }
                                        if(counter.getValue().get().hasCheckedIn(bookingCode)){
                                            throw new PassangerAlreadyCheckedInException("Passenger has already checked in");
                                        }
                                        counter.getValue().get().addToQueue(bookingCode);
                                        int lastCounter = 0;
                                        for (int i = counter.getKey(); !counter.getValue().get().getFlightCodes().contains(flight); i++) {
                                            lastCounter = i;
                                        }
                                        return Optional.of(new CheckInModel(new Pair<>(counter.getKey(), counter.getKey() + counter.getValue().get().getCant() - 1), counter.getValue().get().getAirline(), flight.getFlightCode(), counter.getValue().get().getCheckInQueue().size(), bookingCode));

                                    } else {
                                        throw new WrongCounterException("El número de mostrador no corresponde con el inicio de un rango de mostradores asignado a la aerolínea que esté aceptando pasajeros del vuelo de la reserva");
                                    }

                                }
                            }
                        }
                    }
                }
            }

        }
        throw new InvalidArgumentException(String.format("Booking %s does not exists", bookingCode));
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
        throw new InvalidArgumentException(String.format("Booking %s does not exists or there is no counter", bookingCode));
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
                        throw new FlightAlreadyAddedException(String.format("Flight %s already added to airline %s", flight_code, airline));

                }
                if(flight.getBookings().contains(booking_code))
                    throw new BookingAlreadyExistException(String.format("Booking %s already exists",booking_code));
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
            throw new NoSectorsAvailableException("There are no sectors in the Airport yet");
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
            throw new InvalidArgumentException(String.format("Sector %s does not exists", sector));
        }

        String targetAirline = null;
        List<Flight> airlineFlights = new ArrayList<>();

        for (Airline a : this.airlines) {


            //TODO EXCEPTIONS!
            for (Flight flight : a.getFlights()) {
                if (flightCodes.contains(flight.getFlightCode())) {
                    if (!a.getName().equals(airline)) {
                        //Existe ese flightcode pero en otra aerolinea
                        throw new WrongAirlineForFlightCodeException(String.format("flight code %s belongs to airline %s", flight.getFlightCode(), airline));
                    } else if (flight.getBookings().isEmpty()) {      
                        //No se agregaron pasajeros esperados con el código de vuelo, para al menos uno de los vuelos solicitados
                        throw new NoBookingsForFlightException(String.format("No passangers for flight code %s", flight.getFlightCode()));
                    } else if(flight.isAlreadyCheckedIn()) {
                        return new Pair<>(-1, -1);
                    } 
                    
                    else {
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
            throw new InvalidArgumentException(String.format("Airline %s does not exists", airline));
        }


        return targetSector.assignCounters(targetAirline, airlineFlights, count);
    }

    public int getPendingAhead(String sector, List<String> flightCodes, String airline, int count) {
        return this.sectors.get(this.sectors.indexOf(new Sector(sector))).getPendingAhead(new Assignment(airline, flightCodes.stream().map(Flight::new).toList(), count));
    }

    public Optional<Assignment>  freeCounters(String sector, int from, String airline) {
        Sector targetSector = getSectorByName(sector);
        if(targetSector == null) {
            throw new InvalidArgumentException(String.format("Sector %s does not exists", sector));
        }
        //todo:
        //El rango de mostradores no existe en ese sector (los mostradores no están asignados)
        //El rango de mostradores existe pero no corresponde a esa aerolínea (una aerolínea sólo puede liberar sus rangos)
        //Existen pasajeros esperando a ser atendidos en la cola del rango
        return targetSector.freeCounters(from,airline);
    }

    public Optional<Queue<Assignment>> listPendingAssignments(String sectorName) {
        Sector targetSector = getSectorByName(sectorName);
        if(targetSector == null) {
            throw new InvalidArgumentException(String.format("Sector %s does not exists", sectorName));
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
        if (targetSector == null ) {
            throw new InvalidArgumentException(String.format("Sector %s does not exists", sectorName));
        }
        if (interval.getLeft() > interval.getRight() || targetSector.getAssignedCounters().isEmpty()) {
            throw new InvalidArgumentException("Invalid interval");
        }

        return targetSector.getCounterInfo(interval);

    }

    public Map<String, List<CounterInfoModel>> queryCounters(String sectorName) {
        int emptyCounters = 0;
        for(Sector sector: sectors) {
            if(sector.getAssignedCounters().isEmpty()) {
                emptyCounters++; // Incrementa el contador si todos los contadores están vacíos
            }
        }

        if (emptyCounters == sectors.size()) {
            throw new NoAssignedCountersException("No counters assigned in airport");
        }
        List<CounterInfoModel> counterInfoModelList = new ArrayList<>();
        Map<String, List<CounterInfoModel>> counterInfo = new HashMap<>();
        Pair<Integer, Integer> interval = new Pair<>(-1, -1);

        if(sectorName.isEmpty()){
            for(Sector sector : sectors){
                counterInfo.putIfAbsent(sector.getName(), sector.getCounterInfo(interval));
            }
        }
        else{
            counterInfoModelList = getCounterInfo(sectorName, interval);
            if(counterInfoModelList == null){
                counterInfoModelList = Collections.emptyList();
            }
            counterInfo.putIfAbsent(sectorName, counterInfoModelList);
        }

        return counterInfo;
    }

    public List<CheckInResponseModel> checkInCounters(String sectorName, int from, String airline) {
        for (Sector sector: sectors) {
            if(sector.getName().equals(sectorName)){
                Optional<Assignment> assignment = sector.getAssignedCounters().getOrDefault(from, Optional.empty());
                if (assignment.isEmpty()){
                    // NO EXISTE COUNTER
                    // TODO exception
                    throw new NoAssignedCountersException(String.format("No assigned counters for sector %s", sectorName));
                }
                if (assignment.get().getAirline().equals(airline)){
                    List<CheckInResponseModel> recentCheckedInList = assignment.get().checkAll(from);
                    List<CheckInData> aux = new ArrayList<>();
                    for (CheckInResponseModel checkedIn : recentCheckedInList) {
                            if(checkedIn.getFlightCode() == null || checkedIn.getFlightCode().isEmpty()) {
                               continue;
                            }
                            else {
                                checkedInList.add(new CheckInData(checkedIn, sectorName, airline));
                            }
                        }
                    return recentCheckedInList;
                } else {
                   throw new IncorrectAirlineForCountersException(String.format("Incorrect range of counters for airline %s", airline));
                }
            }

        }

        throw new InvalidArgumentException(String.format("Sector %s does not exists", sectorName));
    }

    public List<CheckInData> queryCheckIns(String sectorName, String airline) {
        List<CheckInData> filteredCheckedInList = new ArrayList<>();
        if(checkedInList.isEmpty()) {
            throw new NoCheckInsRegisteredException("No check in's registered at the moment");
        }

        if(sectorName.isEmpty() && airline.isEmpty()) {
            return checkedInList;
        }

        else if(!sectorName.isEmpty() && airline.isEmpty()) {
            for(CheckInData checkInData : checkedInList) {
                if(checkInData.getSector().equals(sectorName))
                    filteredCheckedInList.add(checkInData);
                break;
            }
        }
        else if(sectorName.isEmpty()) {
            for (CheckInData checkInData : checkedInList) {
                if (checkInData.getAirline().equals(airline)) {
                    filteredCheckedInList.add(checkInData);
                    break;
                }
            }
        }
        else {
            for (CheckInData checkInData : checkedInList) {
                if (checkInData.getAirline().equals(airline) && checkInData.getSector().equals(sectorName)) {
                    filteredCheckedInList.add(checkInData);
                    break;
                }
            }
        }

        return filteredCheckedInList;
    }

    public Map<Assignment, Pair<Integer,Integer>> solvePendingAssignments(String sector) {
        Sector targetSector = getSectorByName(sector);
        return targetSector.solvePendingAssignments();
    }

    public List<Pair<Assignment,Integer>> removeFromPending(String sector, Assignment assignment) {
        Sector targetSector = getSectorByName(sector);
        return targetSector.removeFromPending(assignment);
    }
}
