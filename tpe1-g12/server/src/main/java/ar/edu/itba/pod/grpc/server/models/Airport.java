package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
}
