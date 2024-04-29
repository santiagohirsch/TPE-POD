package ar.edu.itba.pod.grpc.client.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientUtils {
    public final static String SERVER_ADDRESS = "serverAddress";
    public final static String ACTION = "action";
    public final static String SECTOR = "sector";
    public final static String COUNTERS = "counters";
    public final static String IN_PATH = "inPath";
    public final static String COUNTER_FROM = "counterFrom";
    public final static String COUNTER_TO = "counterTo";
    public final static String FLIGHTS = "flights";
    public final static String AIRLINE = "airline";
    public final static String COUNTER_COUNT = "counterCount";
    public final static String BOOKING = "booking";
    public final static String COUNTER = "counter";
    public final static String OUT_PATH = "outPath";


    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> argsMap = new HashMap<>();

        for(String arg : args) {
            String[] parts = arg.split("=");
            if(parts.length == 2) {
                argsMap.put(parts[0].substring(2), parts[1]);
            }

        }
        return argsMap;
    }

    public static String getArg(Map<String, String> args, String key) {
        return args.get(key);
    }

    public static void checkNullArgument(String arg) {
        if(arg == null) {
            System.exit(1);
        }
    }
}
