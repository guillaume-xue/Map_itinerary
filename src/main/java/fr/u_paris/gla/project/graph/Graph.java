package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.commons.lang3.tuple.MutablePair;

import static fr.u_paris.gla.project.io.UpgradedNetworkFormat.WALK_AVG_SPEED;

public class Graph {
    //constante pour la ligne de marche
    public static double MAX_DISTANCE_WALKABLE = 0.1;
    public static double MAX_DISTANCE_WALKABLE_START_FINISH = 0.700;
    public static final Line WALK_LINE = new Line("IDWALK", "Marche a pied", "Walk", "808080");
    private ArrayList<Stop> listOfStops = new ArrayList<>();
    
    //on ajoute la Line WALK_LINE à notre graphe, au debut elle est vide puis elle est remplie par l'algo quand c'est nécéssaire
    private ArrayList<Line> listOfLines = new ArrayList<>(Collections.singletonList(WALK_LINE));

    public Graph(ArrayList<Stop> listOfStops, ArrayList<Line> listOfLines){
        this.listOfLines = listOfLines;
        this.listOfStops = listOfStops;
    }

    public Stop getStop(double latitude, double longitude) throws Exception{
        for(Stop st : listOfStops){
            if(st.getLongitude() == longitude && st.getLatitude() == latitude){
                return st;
            }
        }
        throw new Exception(String.format("Stop was not found at coordinates x = %f, y = %f", longitude, latitude));
    }

    public Stop getClosestStop(double latitude, double longitude) throws Exception{

        if(listOfStops.isEmpty()){
            throw new Exception("The list of stops is empty");
        }

        listOfStops.sort((Stop a, Stop b) -> a.calculateDistance(latitude, longitude).compareTo(b.calculateDistance(latitude, longitude)));

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
     * They will be connected to all other Stops in a MAX_DISTANCE_WALKABLE (meters) radius by walking.
     * Previous StartStop and FinishStops will be removed from the listOfStops, but their connections will not be severed (seen comments in Graph.connectStopsByWalking() )
     * Return a MutablePair<Stop, Stop>, with MutablePair.left <- startStop, MutablePair.right <- finishStop
     */
    public MutablePair<Stop, Stop> addStartAndFinish(double latitudeS, double longitudeS, double latitudeF, double longitudeF) throws Exception{
        if(listOfStops.isEmpty()){
            throw new Exception("The graph is empty");
        }
        Stop startStop = new Stop(latitudeS, longitudeS, "StartPoint");
        Stop finishStop = new Stop(latitudeF, longitudeF, "FinishPoint");

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
     * Currently useable with MAX_DISTANCE_WALKEABLE < 0.5km, and listOfStops = 35364.
     */
    /*
     * Connects all Stops by creating walkable connections between Stops. New connections can be found in Stop.timeDistancePerAdjacentStop 
     * As per discussion with Marie, their are no departures for walkables connections, as one can consider that one can start walking from a Stop 
     * to another whenever they wish. 
     * 
     * If there is already a connection between stops A and B (for instance already a metro connection), then we will not be able to walk from A to B
     */
    public void connectStopsByWalking(){
        double distance; 
        int size = listOfStops.size();
        Stop e;
        Stop a;
        //TODO : find a more efficient way to do this ?
        /*
        This will complete in n * (n + 1) / 2 iterations. 
        I believe this is the least we can do as it is the maximum numbers of edges possible for a graph of n vertices, 
        unless we decide to multithread this. However, due to concurrent acceses of shared variables (Stop.timeDistancePerAdjacentStop), I would be wary.
         */ 
        System.out.println("\nConnecting all Nodes by walking. This may take a while, please be patient.");
        System.out.println("Current maximum distance between 2 points is " + MAX_DISTANCE_WALKABLE + "km");
        if(MAX_DISTANCE_WALKABLE > 0.5){
            System.out.println("You have selected a maximum walkable distance between stations above 0.5km. " + 
            "The expected waiting time is very high.");
        }
        for(int x = 0; x < size; x ++){

            //get current Stop to compare
            a = listOfStops.get(x);

            for(int z = x; z < size; z ++){
                //get current Stop to be compared to
                e = (listOfStops.get(z));

                //check if there is already a connection
                if(!e.hasAdjacentStop(a)){
                    distance = a.calculateDistance(e);

                    //we assume that the time to go from one Stop to another is identical in both ways (a to e and e to a). 
                    //If this is not the case (for instance, if we take into account extra effort / time due to elevation changes), 
                    //one should run additional checks here.
                    if (distance < MAX_DISTANCE_WALKABLE * 1.33) {
                        a.addAdjacentStop(e, "Walk", calculateWalkingTime(distance), (float) distance);
                        e.addAdjacentStop(a, "Walk", calculateWalkingTime(distance), (float) distance);
                    }
                }
            }
        }
        System.out.println("\nFinished connecting all nodes by walking");
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

    public ArrayList<Line> getListOfLines(){
        return this.listOfLines;
    }

    public void addStop(Stop stopA) {
        if ( !listOfStops.contains(stopA) ) {
            listOfStops.add(stopA);
        } else {
            System.out.println("Stop already exists in the graph.");
        }
    }

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

