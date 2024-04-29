package ar.edu.itba.pod.grpc.client.utils;

public class Actions {
    //1
    public static final String ADD_SECTOR = "addSector";
    public static final String ADD_COUNTERS = "addCounters";
    public static final String MANIFEST = "manifest";

    //2
    public static final String LIST_SECTORS = "listSectors";
    public static final String LIST_COUNTERS = "listCounters";
    public static final String ASSIGN_COUNTERS = "assignCounters";
    public static final String FREE_COUNTERS = "freeCounters";
    public static final String CHECKIN_COUNTERS = "checkinCounters";
    public static final String LIST_PENDING_ASSIGNMENTS = "listPendingAssignments";

    //3
    public static final String FETCH_COUNTER = "fetchCounter";
    public static final String PASSENGER_CHECKIN = "passengerCheckin";
    public static final String PASSENGER_STATUS = "passengerStatus";

    //4
    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";

    //5
    public static final String QUERY_COUNTERS = "queryCounters";
    public static final String CHECKINS = "checkins";

}
