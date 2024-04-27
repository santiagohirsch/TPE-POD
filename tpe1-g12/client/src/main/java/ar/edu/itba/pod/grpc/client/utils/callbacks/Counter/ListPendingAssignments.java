package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.ListPendingAssignmentResponse;
import ar.edu.itba.pod.grpc.counter.PendingAssignment;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ListPendingAssignments extends CustomFutureCallback<ListPendingAssignmentResponse> {

    public ListPendingAssignments(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(ListPendingAssignmentResponse pendingAssignments) {
        StringBuilder sb = new StringBuilder();
        if( !pendingAssignments.getPendingsList().isEmpty() && pendingAssignments.getPendingsList().get(0).getCounters() >= 0) {
            sb.append("Counters\tAirline\t\tFlights\n");
            sb.append("##########################################################\n");
            for (PendingAssignment pendingAssignment : pendingAssignments.getPendingsList()) {
                sb.append(pendingAssignment.getCounters());
                sb.append("\t");
                sb.append(pendingAssignment.getAirline());
                sb.append("\t");
                for (String flight : pendingAssignment.getFlightCodesList()) {
                    sb.append(flight);
                    sb.append("|");
                }
                sb.deleteCharAt(sb.lastIndexOf("|"));
                sb.append("\n");
            }
        }
        else if (pendingAssignments.getPendingsList().isEmpty()) {
            sb.append("No pending assignments ");
        }
        else {
            sb.append("Sector inexistente"); //todo cambiar (?
        }
        System.out.println(sb);
        getLatch().countDown();    }

    @Override
    public void onFailure(Throwable throwable){
        System.out.println("Fallo mal");
        getLatch().countDown();    }

}
