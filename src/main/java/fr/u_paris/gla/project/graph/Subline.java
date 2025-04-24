package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;
import fr.u_paris.gla.project.utils.TransportTypes;

public class Subline {

    private String name;

    private TransportTypes sublineType;
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
    
    public ArrayList<Stop> getListOfStops(){
        return this.listOfStops;
    }
    
    public TransportTypes getSublineType() {
    	return sublineType;
    }

    public void setTransportType(TransportTypes type){
        this.sublineType = type;
    }

    //Stop should be the departure Stop. 
    public void addDepartureTimes(Stop Stop, ArrayList<LocalTime> departureTimes) throws Exception{
        if(!Stop.equals(listOfStops.get(0))){
            throw new Exception("The departure Stop indicated does not match the first Stop in the list of Stops.");
        }
        departureTimesAtStartOfSubline.addAll(departureTimes);
    }

    public ArrayList<LocalTime> getDepartureTimes(){
        return this.departureTimesAtStartOfSubline;
    }

    @Override
    public String toString(){
        return "\nSubline : " + name  + " " + listOfStops.toString();
    }

    public String getName(){
        return this.name;
    }
}
