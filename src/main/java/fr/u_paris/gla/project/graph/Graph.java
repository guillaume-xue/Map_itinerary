package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import static fr.u_paris.gla.project.io.UpgradedNetworkFormat.WALK_AVG_SPEED;

/**
 * Java object representation of the transport Network, holds the lines, sublines, stops etc...
 */
public class Graph {

    public static double MAX_DISTANCE_WALKABLE = 0.1; // Max distance between two stops when linking them for walking
    public static double MAX_DISTANCE_WALKABLE_START_FINISH = 0.700; // Max distance between the start/finish and the other stops when linking them
    
    public static final Line WALK_LINE = new Line("IDWALK", "Marche a pied", "Walk", "808080"); // Arbitrary line for walking
    
    //on ajoute la Line WALK_LINE à notre graphe, au debut elle est vide puis elle est remplie par l'algo quand c'est nécéssaire
    private ArrayList<Line> listOfLines = new ArrayList<>(Collections.singletonList(WALK_LINE));
    private ArrayList<Stop> listOfStops = new ArrayList<>();

    /**
     * Constructs a new Graph instance.
     *
     * @param      listOfStops  The list of stops
     * @param      listOfLines  The list of lines
     */
    public Graph(ArrayList<Stop> listOfStops, ArrayList<Line> listOfLines){
        this.listOfLines = listOfLines;
        this.listOfStops = listOfStops;
    }

    /**
     * Gets the stop at the given coordinates, if found.
     *
     * @param      latitude   The latitude
     * @param      longitude  The longitude
     *
     * @return     The found stop.
     *
     * @throws     Exception  Throws an exception is stop is not found.
     */
    public Stop getStop(double latitude, double longitude) throws Exception{
        for(Stop st : listOfStops){
            if(st.getLongitude() == longitude && st.getLatitude() == latitude){
                return st;
            }
        }
        throw new Exception(String.format("Stop was not found at coordinates x = %f, y = %f", longitude, latitude));
    }

    /**
     * Gets the closest stop to the given coordinates.
     *
     * @param      latitude   The latitude
     * @param      longitude  The longitude
     *
     * @return     The closest stop found.
     *
     * @throws     Exception  Throws an exception if the current list of stops of the graph is empty.
     */
    public Stop getClosestStop(double latitude, double longitude) throws Exception{

        if(listOfStops.isEmpty()){
            throw new Exception("The list of stops is empty");
        }

        listOfStops.sort((Stop a, Stop b) -> a.calculateDistance(latitude, longitude).compareTo(b.calculateDistance(latitude, longitude)));

        // Debug print
        //System.out.println( String.format( "Closest Stop found = %s ", listOfStops.get(0).toString() ) );
        
        return listOfStops.get(0);
    }

    /*
     * Adds two new Stops, named StartStop and FinishStop, corresponding to their respective coordinates. 
     * They will be connected to all other Stops in a MAX_DISTANCE_WALKABLE (meters) radius by walking.
     * Previous StartStop and FinishStops will be removed from the listOfStops, but their connections will not be severed (seen comments in Graph.connectStopsByWalking() )
     * Return a MutablePair<Stop, Stop>, with MutablePair.left <- startStop, MutablePair.right <- finishStop
     */
    public MutablePair<Stop, Stop> addStartAndFinish(String nameOfStart, String nameOfFinish, double latitudeS, double longitudeS, double latitudeF, double longitudeF) throws Exception{
        if(listOfStops.isEmpty()){
            throw new Exception("The graph is empty");
        }
        Stop startStop = new Stop(latitudeS, longitudeS, nameOfStart);
        Stop finishStop = new Stop(latitudeF, longitudeF, nameOfFinish);

        double distance;

        listOfStops.removeIf( s -> (s.getNameOfAssociatedStation().equals("StartPoint") || s.getNameOfAssociatedStation().equals("FinishPoint")));

        for(Stop e : listOfStops){
            //Remove previous startPoints that may have been generated.
            {
                distance = startStop.calculateDistance(e);
                if (distance < MAX_DISTANCE_WALKABLE_START_FINISH) {
                    startStop.addAdjacentStop(e, "Walk", calculateWalkingTime(distance), (float) distance);
                }
                distance = finishStop.calculateDistance(e);
                if (distance < MAX_DISTANCE_WALKABLE_START_FINISH) {
                    e.addAdjacentStop(finishStop, "Walk", calculateWalkingTime(distance), (float) distance);
                }
            }   
        }
        //Check if we can walk directly from the starting point directly to the other
        distance = startStop.calculateDistance(finishStop);
        if(distance < MAX_DISTANCE_WALKABLE * 1.33){
            startStop.addAdjacentStop(finishStop, "Walk", calculateWalkingTime(distance), (float) distance);
        }
        listOfStops.add(startStop);
        listOfStops.add(finishStop);

        return new MutablePair<>(startStop, finishStop);
    }


    
    /**
     * nlog(n) version of connectStopsByWalking ( uses a 2DTree )
     */
    public void connectStopsByWalkingV2(){
        System.out.println("Connecting all nodes, max distance: " + MAX_DISTANCE_WALKABLE + "km");

        TwoDTree tree = new TwoDTree(this.listOfStops);

        for (Stop source : listOfStops){
            // Instead of parsing every stop with every other stop we search the radius in the 2Dtree
            List<Stop> nearbyStops = tree.radiusSearch(source.getLatitude(), source.getLongitude(), MAX_DISTANCE_WALKABLE);

            for ( Stop target : nearbyStops ){
                if ( !source.equals(target) && !source.hasAdjacentStop(target) ){
                    double distance = source.calculateDistance(target);
                    Duration duration = calculateWalkingTime(distance);
                    source.addAdjacentStop(target, "Walk", duration, (float) distance);
                    target.addAdjacentStop(source, "Walk", duration, (float) distance);
                }
            }
        }

        System.out.println("Finished connecting.");
    }

    /**
     * Helper function to calculate the walking time for a given distance.
     *
     * @param      distance  The distance
     *
     * @return     The walking time.
     */
    private Duration calculateWalkingTime(double distance){
        return Duration.ofSeconds((long) Math.ceil( (distance / WALK_AVG_SPEED) * 3600));
    }

    /**
     * Gets the line associated to the given name
     *
     * @param      name       The name
     *
     * @return     The line.
     *
     * @throws     Exception  Throws an exception if no line is found with the given name.
     */
    public Line getLine(String name) throws Exception{
        for(Line l : listOfLines){
            if(l.getName().equals(name)){
                return l;
            }
        }
        throw new Exception(String.format("Line was not found with name %s", name));
    }

    /**
     * Gets the list of lines.
     *
     * @return     The list of lines.
     */
    public ArrayList<Line> getListOfLines(){
        return this.listOfLines;
    }

    /**
     * Adds a stop.
     *
     * @param      stop  The stop a
     */
    public void addStop(Stop stop) {
        if ( !listOfStops.contains(stop) ) {
            listOfStops.add(stop);
        } else {
            System.out.println("Stop already exists in the graph.");
        }
    }

    /**
     * Gets the list of stops.
     *
     * @return     The list of stops.
     */
    public ArrayList<Stop> getListOfStops(){
        return this.listOfStops;
    }

    
    @Override
    public String toString(){
        return "--- List of Stops: ---\n" +
        listOfStops.toString() + 
        "\n--- List of Lines: ---\n" +
        listOfLines.toString();
    }

    /**
     * Gets the string representation of the stats ( sizes of the lists ) of the graph
     *
     * @return     The string representation
     */
    public String statsToString(){
        int sublinesCpt = 0;
        int emptySublinesCpt = 0;
        int scheduleCpt = 0;

        for ( Line line  : listOfLines ) {
            sublinesCpt += line.getListOfSublines().size();
            for ( Subline subline : line.getListOfSublines() ){
                if ( subline.getListOfStops().size() == 0 ) emptySublinesCpt++;

                if ( subline.getDepartureTimes().size() != 0 ) scheduleCpt++;
            }
        }

        return "\n--- Graph stats ---\n" +
        "\nNombre de quais uniques: " + 
        listOfStops.size() +
        "\nNombre de lignes: " + 
        listOfLines.size() +
        "\nNombre de sous-lignes: " + 
        sublinesCpt +
        "\nNombre de sous-lignes vides: " +
        emptySublinesCpt +
        "\nNombre de sous-lignes ayant au moins une horaire de départ recensée: " +
        scheduleCpt +
        "\n\n-------------------\n";
    }


}

