package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        System.out.println(sectors);
        System.out.println(sectors.size());
        if (/*sectors.contains(sector)*/ sectors.contains(sector) ) {
            return false;
        } else {
            sectors.add(sector);
            return true;
        }
    }
}
