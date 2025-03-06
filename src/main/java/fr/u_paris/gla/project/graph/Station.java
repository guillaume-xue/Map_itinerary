package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;


public class Station {

    private float gpsCoordX;
    private float gpsCoordY;
    private String name;

    //A list of all adjacent stations, with the associated time and distance to get from current station to adjacent station. 
    private HashMap<Station, MutablePair<LocalTime, Float>> timeDistancePerAdjacentStation = new HashMap<>();

    //For each subline that passes through this station, it should have an entry here
    /*if this station is not a departure station, 
    we will need to calculate at which time the trains would arrive into the station recursivemy from the departure station
    */
    private HashMap<Subline, ArrayList<LocalTime>> departures = new HashMap<>();

    public Station(float gpsCoordX, float gpsCoordY, String name){
        this.gpsCoordX = gpsCoordX;
        this.gpsCoordY = gpsCoordY;
        this.name = name;
    }

    public float getGpsCoordX() {
        return gpsCoordX;
    }

    public float getGpsCoordY() {
        return gpsCoordY;
    }

    public String getName() {
        return name;
    }

    public void addAdjacentStation(Station adjacentStation, LocalTime timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStation.put(adjacentStation, new MutablePair<>(timeToNextStation, distanceToNextStation));
    }

    public void addDeparture(Subline subline, ArrayList<LocalTime> times){
        departures.put(subline, times);
    }
}

