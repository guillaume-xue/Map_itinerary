package fr.u_paris.gla.project.graph;

import java.time.LocalTime;
import java.util.ArrayList;
import fr.u_paris.gla.project.utils.TransportTypes;
import java.util.Objects;

public class Subline {

    private String name;

    private TransportTypes sublineType;
    //needed for printing the itinerary
    private Line associatedLine;
    
    //The first Stop should ALWAYS be the departure Stop.
    private ArrayList<Stop> listOfStops = new ArrayList<>();
    private ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();
    
    public Subline(String name){
        this.name = name;
    }

    //à n'utiliser que pour la création des sous-lignes pr la ligne WALK_LINE
    public Subline(Stop departure, Stop arrival) {
    	this.name = "walking_subline_" + departure.hashCode() + "_" + arrival.hashCode();
    	this.sublineType = TransportTypes.Walk;
    	this.associatedLine = Graph.WALK_LINE;
    	this.addNextStop(departure);
    	this.addNextStop(arrival);
    }
    
    public Stop getStartStop() {
        if (listOfStops != null && !listOfStops.isEmpty()) {
            return listOfStops.get(0); 
        }
        return null;  
    }

    public Stop getDestination() {
        if (listOfStops != null && !listOfStops.isEmpty()) {
            return listOfStops.get(listOfStops.size() - 1); 
        }
        return null;  
    }
    
    public void addNextStop(Stop Stop){
        listOfStops.add(Stop);
    }

    public Line getAssociatedLine() {
    	return this.associatedLine;
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
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subline subline = (Subline) o;
        // Comparaison des éléments pertinents pour l'égalité
        return Objects.equals(name, subline.name) &&
               sublineType == subline.sublineType &&
               Objects.equals(associatedLine, subline.associatedLine) &&
               Objects.equals(listOfStops, subline.listOfStops); // Comparer les stops aussi si nécessaire
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sublineType, associatedLine, listOfStops);
    }
}
