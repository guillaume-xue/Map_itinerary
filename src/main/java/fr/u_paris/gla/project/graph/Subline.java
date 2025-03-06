package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;

public class Subline {

    private String name;

    //The first station should ALWAYS be the departure station.
    private ArrayList<Station> listOfStations = new ArrayList<>();
    private ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();


    public Subline(String name, Station departureStation){
        this.name = name;
    }

    public void addNextStation(Station station){
        listOfStations.add(station);
    }
    //station should be the departure station. 
    public void addDepartureTimes(Station station, ArrayList<LocalTime> departureTimes) throws Exception{
        if(!station.equals(listOfStations.get(0))){
            throw new Exception("The departure station indicated does not match the first station in the list of stations.");
        }
        departureTimesAtStartOfSubline.addAll(departureTimes);
    }
}


