package fr.u_paris.gla.project.graph;

<<<<<<< HEAD
=======
import java.time.Duration;
>>>>>>> prototype_staging
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.lang3.tuple.MutablePair;

import fr.u_paris.gla.project.utils.GPS;

import org.apache.commons.lang3.tuple.MutablePair;


public class Stop implements Comparable<Stop>{

    private double longitude;
    private double latitude;
    private String nameOfAssociatedStation;
    private Stop cameFrom;

    private double f;
    private double g;
    private double h;


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
        this.f = Double.POSITIVE_INFINITY;
        this.g = Double.POSITIVE_INFINITY;
    }

    public Stop(double longitude, double latitude, String nameOfAssociatedStation, int f, int g, int h){
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameOfAssociatedStation = nameOfAssociatedStation;
        this.f = f;
        this.g = g;
        this.h = h;
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

    public HashMap<Subline, ArrayList<LocalTime>> getDepartures() {
        return departures;
    }

    public void addAdjacentStop(Stop adjacentStop, Duration timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(adjacentStop, new MutablePair<>(timeToNextStation, distanceToNextStation));
    }

    public void addDeparture(Subline subline, ArrayList<LocalTime> times){
        departures.put(subline, times);
    }
<<<<<<< HEAD
=======

    public double distanceBetweenAdjacentStop(Stop stop) {
        return Math.abs(this.latitude - stop.latitude) + Math.abs(this.longitude - stop.longitude);
    }

    public ArrayList<Stop> getAdjacentStops() {
        return new ArrayList<>(timeDistancePerAdjacentStop.keySet());
    }

    public HashMap<Stop, MutablePair<Duration, Float>> getTimeDistancePerAdjacentStop(){
        return this.timeDistancePerAdjacentStop;
    }

    public double getF(){
        return f;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public void setCameFrom(Stop cameFrom) {
        this.cameFrom = cameFrom;
    }

    public void setG(double g) {
        this.g = g;
        this.f = g+h;
    }

    public void setH(double h) {
        this.h = h;
        this.f = g+h;
    }

    public Stop getCameFrom() {
        return cameFrom;
    }

    @Override
    public String toString() {
        return String.format(
            "Stop [" +
            "name: '%s'," +
            " longitude: %.6f," +
            " latitude: %.6f" +
            " connecting stops : %s" +
            "]\n",
            nameOfAssociatedStation, longitude, latitude, getAllConnections()
        );    
    }

    public String getAllConnections(){

        ArrayList<String> temp = new ArrayList<>();

        timeDistancePerAdjacentStop.forEach((k,v) -> temp.add(k.getNameOfAssociatedStation()));
        return String.join(", ", temp);
    }

    //FIXME
    @Override
    public int compareTo(Stop o) {
        return nameOfAssociatedStation.compareTo(o.nameOfAssociatedStation);
    }

    //FIXME
    public int compateTo(Stop o) {
        return Double.compare(f, o.f);
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

    //calculates the distance between two stops
    public Double calculateDistance(Stop s){
        return GPS.distance(this.longitude, this.latitude, s.longitude, s.latitude);
    }
    
    //calculates the distance between a stop and the given coordinates
    public Double calculateDistance(double targetLongitude, double targetLatitude){
        return GPS.distance(this.longitude, this.latitude, targetLongitude, targetLatitude);
    }
>>>>>>> prototype_staging
}

