package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.time.Duration;

import org.apache.commons.lang3.tuple.MutablePair;


public class Stop implements Comparable<Stop>{

    private double longitude;
    private double latitude;
    private String nameOfAssociatedStation;

    //A list of all adjacent stations, with the associated time and distance to get from current station to adjacent station. 
    private HashMap<Stop, MutablePair<Duration, Float>> timeDistancePerAdjacentStop = new HashMap<>();

    //For each subline that passes through this station, it should have an entry here
    /*if this station is not a departure station, 
    we will need to calculate at which time the trains would arrive into the station recursivemy from the departure station
    */
    private HashMap<Subline, ArrayList<LocalTime>> departures = new HashMap<>();

    public Stop(double longitude, double latitude, String nameOfAssociatedStation){
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameOfAssociatedStation = nameOfAssociatedStation;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getNameOfAssociatedStation() {
        return nameOfAssociatedStation;
    }

    public void addAdjacentStop(Stop adjacentStop, Duration timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(adjacentStop, new MutablePair<>(timeToNextStation, distanceToNextStation));
    }

    public void addDeparture(Subline subline, ArrayList<LocalTime> times){
        departures.put(subline, times);
    }

    @Override
    public String toString() {
        return String.format(
            "Stop [" +
            "name: '%s'," +
            " longitude: %.6f," +
            " latitude: %.6f" +
            "]\n",
            nameOfAssociatedStation, longitude, latitude
        );    
    }

    @Override
    public int compareTo(Stop o) {
        return nameOfAssociatedStation.compareTo(o.nameOfAssociatedStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, nameOfAssociatedStation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stop other = (Stop) obj;
        return Double.doubleToLongBits(latitude) == Double
                .doubleToLongBits(other.latitude) && Objects.equals(nameOfAssociatedStation, other.nameOfAssociatedStation)
                && Double.doubleToLongBits(longitude) == Double
                        .doubleToLongBits(other.longitude);
    }
}

