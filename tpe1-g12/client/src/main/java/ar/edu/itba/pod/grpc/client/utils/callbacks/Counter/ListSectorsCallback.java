package ar.edu.itba.pod.grpc.client.utils.callbacks.Counter;

import ar.edu.itba.pod.grpc.client.utils.callbacks.CustomFutureCallback;
import ar.edu.itba.pod.grpc.counter.Interval;
import ar.edu.itba.pod.grpc.counter.ListSectorsResponse;
import ar.edu.itba.pod.grpc.counter.SectorInfo;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListSectorsCallback extends CustomFutureCallback<ListSectorsResponse> {


    public ListSectorsCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(ListSectorsResponse listSectorsResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sectors   Counters\n").append("###################\n");

        for (SectorInfo sectorInfo : listSectorsResponse.getSectorList()) {
            sb.append(sectorInfo.getSectorName().getName()).append("\t\t\t");
            List<Interval> intervals = sectorInfo.getIntervalsList();
            if (!intervals.isEmpty()) {
                for (Interval interval : intervals) {
                    sb.append("(").append(interval.getLowerBound()).append("-").append(interval.getUpperBound()).append(")");
                }
            } else {
                sb.append("-");
            }

            sb.append("\n");
        }
        System.out.println(sb);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable throwable) {
        System.out.println("fallo");
        System.out.println(throwable.getMessage());
        getLatch().countDown();
    }
}
