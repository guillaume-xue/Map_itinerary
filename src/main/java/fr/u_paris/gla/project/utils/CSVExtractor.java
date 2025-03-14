/**
 * 
 */
package fr.u_paris.gla.project.utils;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import fr.u_paris.gla.project.graph.*;

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

    // Format d'une ligne:
    //"54B";"0";"Rue de la Commanderie";"49.08530899148388, 3.0767128685174954";
    //          "Cimetière";"49.0878251824414, 3.078824791457119";"00:04";"0.166"
    // LineID;SublineID;StopA;StopACoord;StopB;StopBCoord;DistanceMin;DistanceKM
    //    0       1       2        3       4       5           6          7

    public static void makeOjectsFromCSV(String path){
            if (path == null) {
                LOGGER.severe("Invalid command line for parsing CSV. Missing target path.");
            return;
        }

        ArrayList<Stop> listOfStops = new ArrayList<>();
        ArrayList<Line> listOfLines = new ArrayList<>();
        // TODO faire des Arraylist des HashSet pour un soucis de rapidité

        try{
            CSVTools.readCSVFromFile(path,(String[] line) -> 
                readLine(line, listOfStops, listOfLines));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the file", e);
        }
    
        Graph graph = new Graph(listOfStops, listOfLines);

        Collections.sort(listOfStops);
        Collections.sort(listOfLines);

        System.out.println(listOfStops.toString());
        System.out.println(listOfLines.toString());
    }

    public static void readLine(String[] line, ArrayList<Stop> stops, ArrayList<Line> lines ){
        addStops(line, stops);
        addLine(line, lines);
    }

    public static void addStops(String[] line, ArrayList<Stop> stops){
        String[] stopACoord = line[3].split(",");
        String[] stopBCoord = line[5].split(",");

        float stopAlon = Float.parseFloat(stopACoord[0]);
        float stopAlat = Float.parseFloat(stopACoord[1]);
        float stopBlon = Float.parseFloat(stopBCoord[0]);
        float stopBlat = Float.parseFloat(stopBCoord[1]);

        Stop stopA = new Stop(stopAlon,stopAlat,line[2]);
        Stop stopB = new Stop(stopBlon,stopBlat,line[4]);

        if (!stops.contains(stopA)) {
            stops.add(stopA);
        }
    
        if (!stops.contains(stopB)) {
            stops.add(stopB);
        }
    }

    public static void addLine(String[] line, ArrayList<Line> lines){
        Line newLine = new Line(line[0]);
        if (!lines.contains(newLine)){
            lines.add(newLine);
        }
    }

}