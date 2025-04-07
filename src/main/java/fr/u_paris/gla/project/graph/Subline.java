package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;

public class Subline {

    private String name;

    //The first Stop should ALWAYS be the departure Stop.
    private ArrayList<Stop> listOfStops = new ArrayList<>();
    private ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();
    

    public Subline(String name){
        this.name = name;
    }

    public void addNextStop(Stop Stop){
        listOfStops.add(Stop);
    }

    public void setListOfStops(ArrayList<Stop> listOfStops){
        this.listOfStops = listOfStops;
    }
    
    //Stop should be the departure Stop. 
    public void addDepartureTimes(Stop Stop, ArrayList<LocalTime> departureTimes) throws Exception{
        if(!Stop.equals(listOfStops.get(0))){
            throw new Exception("The departure Stop indicated does not match the first Stop in the list of Stops.");
        }
        departureTimesAtStartOfSubline.addAll(departureTimes);
    }

    @Override
    public String toString(){
        return "\nSubline : " + name  + " " + listOfStops.toString();
    }
}


