package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;


public class Stop {

    private float gpsCoordX;
    private float gpsCoordY;
    private String nameOfAssociatedStation;

    //A list of all adjacent stations, with the associated time and distance to get from current station to adjacent station. 
    private HashMap<Stop, MutablePair<LocalTime, Float>> timeDistancePerAdjacentStop = new HashMap<>();

    //For each subline that passes through this station, it should have an entry here
    /*if this station is not a departure station, 
    we will need to calculate at which time the trains would arrive into the station recursivemy from the departure station
    */
    private HashMap<Subline, ArrayList<LocalTime>> departures = new HashMap<>();

    public Stop(float gpsCoordX, float gpsCoordY, String nameOfAssociatedStation){
        this.gpsCoordX = gpsCoordX;
        this.gpsCoordY = gpsCoordY;
        this.nameOfAssociatedStation = nameOfAssociatedStation;
    }

    public float getGpsCoordX() {
        return gpsCoordX;
    }

    public float getGpsCoordY() {
        return gpsCoordY;
    }

    public String getNameOfAssociatedStation() {
        return nameOfAssociatedStation;
    }

    public void addAdjacentStop(Stop adjacentStop, LocalTime timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(adjacentStop, new MutablePair<>(timeToNextStation, distanceToNextStation));
    }

    public void addDeparture(Subline subline, ArrayList<LocalTime> times){
        departures.put(subline, times);
    }
}

