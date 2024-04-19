package ar.edu.itba.pod.grpc.models;

import java.util.List;

public class Airport {
    private List<Sector> sectors;


    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public boolean addSector(Sector sector) {
        if (sectors.contains(sector)) {
            return false;
        } else {

        }
    }
}
