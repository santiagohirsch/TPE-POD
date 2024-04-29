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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Airport {
    private List<Sector> sectors;
    private List<Airline> airlines;
    private static int nextAvailableCounter = 1;
    private List<List<CheckInData>> checkedInList = new ArrayList<>();
    private List<CheckInData> checkedInList2 = new ArrayList<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

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
        readWriteLock.writeLock().lock();
        try {
            if (sectors.contains(sector) ) {
                return false;
            } else {
                sectors.add(sector);
                return true;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public Optional<Pair<Integer,Integer>> addCounters(Sector sector, int cant){
        readWriteLock.writeLock().lock();
        try {
            for (Sector s : sectors) {
                if (s.equals(sector)) {
                    Optional<Pair<Integer,Integer>> toReturn = Optional.of(s.addCounters(cant, nextAvailableCounter));
                    nextAvailableCounter = toReturn.get().getRight() + 1;
                    return toReturn;
                }
            }
            return Optional.empty();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public Optional<CounterInfoBookingModel> fetchCounter(String bookingCode) {
        readWriteLock.readLock().lock();
        try {
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
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    public Optional<CheckInModel> intoQueue(String bookingCode, Sector s, int initialCounter) {
        readWriteLock.writeLock().lock();
        try {
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

                                    } else {
                                        throw new WrongCounterException("El número de mostrador no corresponde con el inicio de un rango de mostradores asignado a la aerolínea que esté aceptando pasajeros del vuelo de la reserva");
                                    }
                                }
                            }
                        }
                    }
                }

            }

            return Optional.empty();
        } finally {
            readWriteLock.writeLock().unlock();
        }


    }

    public Optional<CheckInStatusModel> status(String bookingCode) {

        readWriteLock.readLock().lock();
        try {
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
        } finally {
            readWriteLock.readLock().unlock();
        }


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

    public boolean addBooking(String booking_code, String flight_code, String airline) {
        readWriteLock.writeLock().lock();
        try {
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
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public Optional<Map<String, List<Pair<Integer, Integer>>>> listSectors() {
        readWriteLock.readLock().lock();
        try {
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
        } finally {
            readWriteLock.readLock().unlock();
        }

    }



    public Pair<Integer, Integer> assignCounters(String sector, List<String> flightCodes, String airline, int count) {
        readWriteLock.writeLock().lock();
        try {
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
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public int getPendingAhead(String sector, List<String> flightCodes, String airline, int count) {
        readWriteLock.readLock().lock();
        try {
            return this.sectors.get(this.sectors.indexOf(new Sector(sector))).getPendingAhead(new Assignment(airline, flightCodes.stream().map(Flight::new).toList(), count));

        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Optional<Assignment>  freeCounters(String sector, int from, String airline) {
        readWriteLock.writeLock().lock();
        try {
            Sector targetSector = getSectorByName(sector);
            if(targetSector == null) {
                return Optional.empty(); //todo checkear
            }

            return targetSector.freeCounters(from,airline);
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public Optional<Queue<Assignment>> listPendingAssignments(String sectorName) {
        readWriteLock.readLock().lock();
        try {
            Sector targetSector = getSectorByName(sectorName);
            if(targetSector == null) {
                return Optional.empty(); //todo deberia fallar!
            }
            return targetSector.listPendingAssignments();
        } finally {
            readWriteLock.readLock().unlock();
        }



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
        readWriteLock.writeLock().lock();
        try {
            Sector targetSector = getSectorByName(sectorName);
            if (targetSector == null || interval.getLeft() > interval.getRight()) {
                return null;            //todo deberia fallar!
            }

            if (targetSector.getAssignedCounters().isEmpty()) {
                return Collections.emptyList();
            }

            return targetSector.getCounterInfo(interval);
        } finally {
            readWriteLock.writeLock().unlock();
        }


    }

    public Map<String, List<CounterInfoModel>> queryCounters(String sectorName) {
        readWriteLock.writeLock().lock();
        try {
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
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public List<CheckInResponseModel> checkInCounters(String sectorName, int from, String airline) {
        readWriteLock.writeLock().lock();
        try {
            for (Sector sector: sectors) {
                if(sector.getName().equals(sectorName)){
                    Optional<Assignment> assignment = sector.getAssignedCounters().getOrDefault(from, Optional.empty());
                    if (assignment.isEmpty()){
                        // NO EXISTE COUNTER
                        // TODO exception
                        return Collections.emptyList();
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
                        //existe pero en otra airline
                        // TODO exception
                        return Collections.emptyList();
                    }
                }

            }

            // no existe el sector
            // TODO excetption
            return Collections.emptyList();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public List<CheckInData> queryCheckIns(String sectorName, String airline) {

        readWriteLock.readLock().lock();
        try {
            List<CheckInData> filteredCheckedInList = new ArrayList<>();


            if(sectorName.isEmpty() && airline.isEmpty()) {
                return checkedInList2;
            }

            else if(!sectorName.isEmpty() && airline.isEmpty()) {
                for(CheckInData checkInData : checkedInList2) {
                    if(checkInData.getSector().equals(sectorName))
                        filteredCheckedInList.add(checkInData);
                    break;
                }
            }
            else if(sectorName.isEmpty()) {
                for (CheckInData checkInData : checkedInList2) {
                    if (checkInData.getAirline().equals(airline)) {
                        filteredCheckedInList.add(checkInData);
                        break;
                    }
                }
            }
            else {
                for (CheckInData checkInData : checkedInList2) {
                    if (checkInData.getAirline().equals(airline) && checkInData.getSector().equals(sectorName)) {
                        filteredCheckedInList.add(checkInData);
                        break;
                    }
                }
            }

            return filteredCheckedInList;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Map<Assignment, Pair<Integer,Integer>> solvePendingAssignments(String sector) {
        readWriteLock.writeLock().lock();
        try {
            Sector targetSector = getSectorByName(sector);
            return targetSector.solvePendingAssignments();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public List<Pair<Assignment,Integer>> removeFromPending(String sector, Assignment assignment) {
        readWriteLock.writeLock().lock();
        try {
            Sector targetSector = getSectorByName(sector);
            return targetSector.removeFromPending(assignment);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
