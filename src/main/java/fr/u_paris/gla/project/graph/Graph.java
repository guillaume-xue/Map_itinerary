package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;

import static fr.u_paris.gla.project.io.UpgradedNetworkFormat.WALK_AVG_SPEED;

public class Graph {

    private ArrayList<Stop> listOfStops = new ArrayList<>();
    private ArrayList<Line> listOfLines = new ArrayList<>();

    public Graph(ArrayList<Stop> listOfStops, ArrayList<Line> listOfLines){
        this.listOfLines = listOfLines;
        this.listOfStops = listOfStops;
    }

    public Stop getStop(double longitude, double latitude) throws Exception{
        for(Stop st : listOfStops){
            if(st.getLongitude() == longitude && st.getLatitude() == latitude){
                return st;
            }
        }
        throw new Exception(String.format("Stop was not found at coordinates x = %f, y = %f", longitude, latitude));
    }

    public Stop getClosestStop(double longitude, double latitude) throws Exception{

        if(listOfStops.isEmpty()){
            throw new Exception("The list of stops is empty");
        }

        listOfStops.sort((Stop a, Stop b) -> a.calculateDistance(longitude, latitude).compareTo(b.calculateDistance(longitude, latitude)));

        System.out.println(
            String.format(
                "Closest Stop found = %s ", 
                listOfStops.get(0).toString() 
            )
        );
        return listOfStops.get(0);
    }

    /*
     * Adds two new Stops, named StartStop and FinishStop, corresponding to their respective coordinates. 
     * They will be connected to all other Stops in a 300m radius by walking.
     * Previous StartStop and FinishStops will be removed from the listOfStops, but their connections will not be severed (seen comments in Graph.connectStopsByWalking() )
     * Return a MutablePair<Stop, Stop>, with MutablePair.left <- startStop, MutablePair.right <- finishStop
     */

    public MutablePair<Stop, Stop> addStartAndFinish(double longitudeS, double latitudeS, double longitudeF, double latitudeF) throws Exception{
        if(listOfStops.isEmpty()){
            throw new Exception("The graph is empty");
        }
        Stop startStop = new Stop(longitudeS, latitudeS, "StartPoint");
        Stop finishStop = new Stop(longitudeF, latitudeF, "FinishPoint");

        double distance;
        for(Stop e : listOfStops){
            //Remove previous startPoints that may have been generated.
            if(e.getNameOfAssociatedStation().equals("StartPoint") || e.getNameOfAssociatedStation().equals("FinishPoint")){
                listOfStops.remove(e);
            }
            //TODO : add global static variable that implements the max distance a user is ready to walk, in meters instead of 300m
            //TODO : refactor Duration.ofSeconds((long) Math.ceil( (distance / WALK_AVG_SPEED) * 3600)) into a separate method
            else {
                distance = startStop.calculateDistance(e);
                if (distance < 1) {
                    startStop.addAdjacentStop(e, calculateWalkingTime(distance), distance);
                }
                distance = finishStop.calculateDistance(e);
                if (distance < 1) {
                    e.addAdjacentStop(finishStop, calculateWalkingTime(distance), distance);
                }
            }   
        }
        listOfStops.add(startStop);
        listOfStops.add(finishStop);

        return new MutablePair<>(startStop, finishStop);
    }



    /*
     * TODO : this will NOT remove the connections between stops if the maximum distance changes. To do this we would need to be able
     * to identify what sort of transport is used when walking from a Stop to another 
     * change HashMap<Stop, MutablePair<Duration, Float>> timeDistancePerAdjacentStop --> HashMap<Stop, MutableTriple<Duration, Float, String>> timeDistancePerAdjacentStop
     * with String / an enum as the type of transport of the Subline that connects those two stops ? 
     */

    /*
     * This is atrociously slow, and gets exponentially slower when the maximum distance walkable between two Stops increases, and more connections are created.
     * Efforts should be made to speed this up, diminush the maximal acceptable walkable distance, or disable changing the distance and only run this once 
     * in a different thread while the app is loading to avoid a painful user experience.  
     */
    /*
     * Connects all Stops by creating walkable connections between Stops. New connections can be found in Stop.timeDistancePerAdjacentStop 
     * As per discussion with Marie, their are no departures for walkables connections, as one can consider that one can start walking from a Stop 
     * to another whenever they wish. 
     */
    public void connectStopsByWalking(){
        double distance; 
        int n = 0;
        int size = listOfStops.size();
        //TODO : find a more efficient way to do this or put a loading bar here 
        System.out.println("Connecting all Nodes by walking. This may take a while, please be patient.");
        for(Stop a : listOfStops){
            System.out.println("Connecting all Nodes by walking. This may take a while, please be patient. You are a step : " + n + "/" + size);
            n+=1;
            for(Stop e : listOfStops){
                if(!e.hasAdjacentStop(a)){
                    distance = a.calculateDistance(e);
                    //TODO : add global static variable that implements the max distance a user is ready to walk, in meters instead of 300m
                    if (distance < 0.3) {
                        a.addAdjacentStop(e, calculateWalkingTime(distance), distance);
                    }
                }
            }
        }
        System.out.println("Finished connecting all nodes by walking");
    }
    private Duration calculateWalkingTime(double distance){
        return Duration.ofSeconds((long) Math.ceil( (distance / WALK_AVG_SPEED) * 3600));
    }



    public Line getLine(String name) throws Exception{
        for(Line l : listOfLines){
            if(l.getName().equals(name)){
                return l;
            }
        }
        throw new Exception(String.format("Line was not found with name %s", name));
    }

    @Override
    public String toString(){
        return "--- List of Stops: ---\n" +
        listOfStops.toString() + 
        "\n--- List of Lines: ---\n" +
        listOfLines.toString();
    }
}

