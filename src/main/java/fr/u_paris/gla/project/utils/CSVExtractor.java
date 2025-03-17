/**
 * 
 */
package fr.u_paris.gla.project.utils;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;

import fr.u_paris.gla.project.graph.*;
import fr.u_paris.gla.project.io.NetworkFormat;
import java.time.Duration;
import java.time.LocalTime;

/**
 * A CSV Extractor class in order to generate the objects from the CSV to our Model. 
 */
public final class CSVExtractor {

    private static final Logger LOGGER = Logger
            .getLogger(CSVExtractor.class.getName());

    /** Hidden constructor of tool class */
    private CSVExtractor() {
        // Parser class
    }

    public static void makeOjectsFromCSV(String path){
            if (path == null) {
                LOGGER.severe("Invalid command line for parsing CSV. Missing target path.");
            return;
        }

        Map<String,ArrayList<Subline>> mapOfLines = new HashMap<>();
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops = new HashMap<>();

        try{
            CSVTools.readCSVFromFile(path,(String[] line) -> 
                readLine(line, mapOfLines, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the file", e);
        }
    

        ArrayList<Stop> listOfStops = new ArrayList<>(mapOfStops.values());
        //Collections.sort(listOfStops);

        ArrayList<Line> listOfLines = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Subline>> entry : mapOfLines.entrySet()) {
            listOfLines.add(new Line(entry.getKey(), entry.getValue()));
        }
        //Collections.sort(listOfLines);

        Graph graph = new Graph(listOfStops, listOfLines);

        System.out.println(graph.toString());
    }

    // Format d'une ligne:
    //"54B";"0";"Rue de la Commanderie";"49.08530899148388, 3.0767128685174954";
    //          "Cimetière";"49.0878251824414, 3.078824791457119";"00:04";"0.166"
    // LineName;SublineID;StopA;StopACoord;StopB;StopBCoord;DistanceMin;DistanceKM
    //    0       1       2        3       4       5           6          7

    public static void readLine(
        String[] line, 
        Map<String,ArrayList<Subline>> mapOfLines,  
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        mapOfLines.putIfAbsent(line[0], new ArrayList<>());

        int sublineId = Integer.parseInt(line[1]);
        if ( mapOfLines.get(line[0]).size() < sublineId+1 ){
            mapOfLines.get(line[0]).add(new Subline(line[1]));
        }

        addStops(line, mapOfLines.get(line[0]).get(sublineId), mapOfStops);
    }

    public static void addStops(
        String[] line, 
        Subline subline, 
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        String[] stopACoordString = line[3].split(",");
        double stopAlon = Double.parseDouble(stopACoordString[0]);
        double stopAlat = Double.parseDouble(stopACoordString[1]);
        ImmutablePair<Double,Double> stopACoord = new ImmutablePair<>(stopAlon,stopAlat);

        mapOfStops.putIfAbsent(stopACoord, new Stop(stopAlon,stopAlat,line[2]));

        String[] stopBCoordString = line[5].split(",");
        double stopBlon = Double.parseDouble(stopBCoordString[0]);
        double stopBlat = Double.parseDouble(stopBCoordString[1]);
        ImmutablePair<Double,Double> stopBCoord = new ImmutablePair<>(stopBlon,stopBlat);

        mapOfStops.putIfAbsent(stopBCoord, new Stop(stopBlon,stopBlat,line[4]));

        Stop stopA = mapOfStops.get(stopACoord);
        Stop stopB = mapOfStops.get(stopBCoord);

        Duration timeToNextStation = NetworkFormat.parseLargeDuration(line[6]);

        Float distanceToNextStation = Float.parseFloat(line[7]);

        stopA.addAdjacentStop(stopB, timeToNextStation, distanceToNextStation);
        subline.addNextStop(stopA);
        subline.addNextStop(stopB);

    }
}