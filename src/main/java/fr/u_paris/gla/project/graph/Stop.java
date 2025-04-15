package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

//import fr.u_paris.gla.project.utils.Pair;
import org.apache.commons.lang3.tuple.Pair;
import fr.u_paris.gla.project.utils.GPS;


public class Stop implements Comparable<Stop>{

    private double longitude;
    private double latitude;
    private String nameOfAssociatedStation;
    
    // Pour l'algo A*
    private Stop cameFrom;
    private double f;
    private double g;
    private double h;

    //A list of all adjacent stations, with the associated time and distance to get from current station to adjacent station. 
    private HashMap<Stop, Pair<Duration, Float>> timeDistancePerAdjacentStop = new HashMap<>();

    //For each subline that passes through this station, it should have an entry here
    /*if this station is not a departure station, 
    we will need to calculate at which time the trains would arrive into the station recursively from the departure station
    */
    private HashMap<Subline, ArrayList<LocalTime>> departures = new HashMap<>();

    public Stop(double longitude, double latitude, String nameOfAssociatedStation){
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameOfAssociatedStation = nameOfAssociatedStation;
        this.f = Double.POSITIVE_INFINITY;
        this.g = Double.POSITIVE_INFINITY;
    }

    // public Stop(double longitude, double latitude, String nameOfAssociatedStation, int f, int g, int h){
    //     this.longitude = longitude;
    //     this.latitude = latitude;
    //     this.nameOfAssociatedStation = nameOfAssociatedStation;
    //     this.f = f;
    //     this.g = g;
    //     this.h = h;
    // }

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
        timeDistancePerAdjacentStop.put(adjacentStop, Pair.of(timeToNextStation, distanceToNextStation));
    }

    public void addDeparture(Subline subline, ArrayList<LocalTime> times) {
        departures.put(subline, new ArrayList<>(times)); // defensive copy
    }

    public void addDeparture(Subline subline, LocalTime time) {
        departures.computeIfAbsent(subline, k -> new ArrayList<>()).add(time);
    }

    public double distanceBetweenAdjacentStop(Stop stop) {
        return Math.abs(this.latitude - stop.latitude) + Math.abs(this.longitude - stop.longitude);
    }

    public ArrayList<Stop> getAdjacentStops() {
        return new ArrayList<>(timeDistancePerAdjacentStop.keySet());
    }

    public HashMap<Stop, Pair<Duration, Float>> getTimeDistancePerAdjacentStop(){
        return this.timeDistancePerAdjacentStop;
    }
    
    public double getDistanceTo(Stop otherStop) {
        Pair<Duration, Float> data = timeDistancePerAdjacentStop.get(otherStop);
        if (data != null) {
            return data.getRight(); // Retourne la distance
        }
        return Double.POSITIVE_INFINITY; // Arrêt non voisin, retourner un coût infini
    }

    
    //à voir pour toutes les stations adjacentes mais qui sont adjacentes à pied car la durée est pas init je crois
    public Duration getTimeTo(Stop otherStop) {
        Pair<Duration, Float> data = timeDistancePerAdjacentStop.get(otherStop);
        if (data != null) {
            return data.getLeft(); // Retourne la durée 
        }
        return Duration.ofHours(9999); // Arrêt non voisin, retourner une durée maximale
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
        //this.f = g+h;
    }

    public void setH(double h) {
        this.h = h;
        //this.f = g+h;
    }

    public void setF() {
    	this.f = this.h + this.g;
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

    
    //pour pouvoir explorer les noeuds ds l'ordre des f les plus petits
    @Override
    public int compareTo(Stop other) {
        double thisF = this.getF();
        double otherF = other.getF();
        return Double.compare(thisF, otherF);
    }
    
    /* à jeter, normalement...
     //FIXME
    @Override
    public int compareTo(Stop o) {
        return nameOfAssociatedStation.compareTo(o.nameOfAssociatedStation);
    }

    //FIXME
    public int compateTo(Stop o) {
        return Double.compare(f, o.f);
    }*/

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, nameOfAssociatedStation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Stop other = (Stop) o;
        return 
            Double.doubleToLongBits(latitude) == Double.doubleToLongBits(other.latitude) && 
            Objects.equals(nameOfAssociatedStation, other.nameOfAssociatedStation) && 
            Double.doubleToLongBits(longitude) == Double.doubleToLongBits(other.longitude);
    }

    //calculates the distance between two stops
    public Double calculateDistance(Stop s){
        return GPS.distance(this.longitude, this.latitude, s.longitude, s.latitude);
    }
    
    //calculates the distance between a stop and the given coordinates
    public Double calculateDistance(double targetLongitude, double targetLatitude){
        return GPS.distance(this.longitude, this.latitude, targetLongitude, targetLatitude);
    }
}

