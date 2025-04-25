package fr.u_paris.gla.project.graph;

import java.util.ArrayList;
import fr.u_paris.gla.project.utils.TransportTypes;
import java.util.Collections;

public class Graph {
    //constante pour la ligne de marche
    public static final Line WALK_LINE = new Line("IDWALK", "Marche a pied", "Walk", "808080");
    private ArrayList<Stop> listOfStops = new ArrayList<>();
    
    //on ajoute la Line WALK_LINE à notre graphe, au debut elle est vide puis elle est remplie par l'algo quand c'est nécéssaire
    private ArrayList<Line> listOfLines = new ArrayList<>(Collections.singletonList(WALK_LINE));

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

