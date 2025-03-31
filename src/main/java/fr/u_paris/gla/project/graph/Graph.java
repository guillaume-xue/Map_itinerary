package fr.u_paris.gla.project.graph;

import java.util.ArrayList;

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
        return /*"--- List of Stops: ---\n" +
        listOfStops.toString() + */
        "\n--- List of Lines: ---\n" +
        listOfLines.toString();
    }
}

