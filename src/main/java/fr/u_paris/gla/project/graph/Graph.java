package fr.u_paris.gla.project.graph;

import java.util.ArrayList;

public class Graph {

    private ArrayList<Stop> listOfStops = new ArrayList<>();
    private ArrayList<Line> listOfLines = new ArrayList<>();

    public Graph(ArrayList<Stop> listOfStops, ArrayList<Line> listOfLines){
        this.listOfLines = listOfLines;
        this.listOfStops = listOfStops;
    }

    public Stop getStop(Float gpsCoordX, Float gpsCoordY) throws Exception{
        for(Stop st : listOfStops){
            if(st.getGpsCoordX() == gpsCoordX && st.getGpsCoordY() == gpsCoordY){
                return st;
            }
        }
        throw new Exception(String.format("Stop was not found at coordinates x = %f, y = %f", gpsCoordX, gpsCoordY));
    }
}

