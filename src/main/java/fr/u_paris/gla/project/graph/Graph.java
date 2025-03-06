package fr.u_paris.gla.project.graph;

import java.util.ArrayList;

public class Graph {

    private ArrayList<Station> listOfStations = new ArrayList<>();
    private ArrayList<Line> listOfLines = new ArrayList<>();

    public Graph(ArrayList<Station> listOfStations, ArrayList<Line> listOfLines){
        this.listOfLines = listOfLines;
        this.listOfStations = listOfStations;
    }

    public Station getStation(Float gpsCoordX, Float gpsCoordY) throws Exception{
        for(Station st : listOfStations){
            if(st.getGpsCoordX() == gpsCoordX && st.getGpsCoordY() == gpsCoordY){
                return st;
            }
        }
        throw new Exception(String.format("Station was not found at coordinates x = %f, y = %f", gpsCoordX, gpsCoordY));
    }
}

