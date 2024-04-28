package ar.edu.itba.pod.grpc.server.utils;

public class CheckInData extends CheckInResponseModel {
    private final String sector;
    private final String airline;

    public CheckInData(String bookingCode, String flightCode, int counter, String sector, String airline) {
        super(bookingCode, flightCode, counter);
        this.sector = sector;
        this.airline = airline;
    }

    public CheckInData(CheckInResponseModel checkInResponseModel, String sector, String airline) {
        super(checkInResponseModel.getBookingCode(),checkInResponseModel.getFlightCode(), checkInResponseModel.getCounter());
        this.sector = sector;
        this.airline = airline;
    }

    public String getSector() {
        return sector;
    }

    public String getAirline() {
        return airline;
    }
}
