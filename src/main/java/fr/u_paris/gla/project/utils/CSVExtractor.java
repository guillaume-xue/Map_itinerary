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
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;

import fr.u_paris.gla.project.graph.*;
import static fr.u_paris.gla.project.io.UpgradedNetworkFormat.*;
import static fr.u_paris.gla.project.io.junctionFormat.*;
import java.time.Duration;
import java.time.LocalTime;

/**
 * A CSV Extractor class in order to generate the objects from the CSV to our Model. 
 */
public final class CSVExtractor {

    private static int errorCpt = 0;

    // Index pour les arguments
    private static final int STOPS_DATA_ID = 1;
    private static final int JUNCTIONS_DATA_ID = 2;

    private static final Logger LOGGER = Logger
            .getLogger(CSVExtractor.class.getName());

    /** Hidden constructor of tool class */
    private CSVExtractor() {
        // Parser class
    }

    // 
    public static void makeOjectsFromCSV(String[] args){
        // --parse <stops_data.csv> <junctions_data.csv>

        if (args == null) {
            LOGGER.severe("Invalid command line for parsing CSV. Missing target path.");
            return;
        }

        Map<String,ArrayList<Subline>> mapOfLines = new HashMap<>();
        Map<String,ArrayList<Stop>> mapOfStopEntry = new HashMap<>();
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops = new HashMap<>();

        // On ajoute les stops et les lignes ( sans les sous-lignes ) à leur map respectives.
        try{
            CSVTools.readCSVFromFile(args[STOPS_DATA_ID],(String[] line) -> 
                readStops(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the file", e);
        }

        // On read le junctionsData pour faire la liste de lignes et leurs sous-lignes
        try{
            CSVTools.readCSVFromFile(args[JUNCTIONS_DATA_ID],(String[] line) -> 
                readJunctions(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the file", e);
        }

        ArrayList<Stop> listOfStops = new ArrayList<>(mapOfStops.values());
        //Collections.sort(listOfStops);
        System.out.println("Nombre d'arrêts (quais) uniques trouvés: " + mapOfStops.size());
        System.out.println("Nombre d'arrêts (quais) uniques trouvés: " + listOfStops.size());
        System.out.println("Nombre de lignes trouvées: " + mapOfLines.size());

        ArrayList<Line> listOfLines = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Subline>> entry : mapOfLines.entrySet()) {
            listOfLines.add(new Line(entry.getKey(), entry.getValue()));
        }
        //Collections.sort(listOfLines);

        Graph graph = new Graph(listOfStops, listOfLines);

        System.out.println("Nombre de sous-lignes n'ayant pas trouvé leur chemin associé:" + errorCpt);
        //System.out.println(graph.toString());
    }

    public static void readStops(
        String[] line, 
        Map<String,ArrayList<Subline>> mapOfLines,
        Map<String,ArrayList<Stop>> mapOfStopEntry,
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        // On ajoute la ligne du tuple si elle est nouvelle
        mapOfLines.putIfAbsent(line[LINE_INDEX] + "_" + line[TYPE_INDEX], new ArrayList<>());
        mapOfStopEntry.putIfAbsent(line[LINE_INDEX] + "_" + line[TYPE_INDEX], new ArrayList<>());
        // On ajoute les deux stations à la map
        addStops(line, mapOfStopEntry, mapOfStops);
    }

    public static void readJunctions(
        String[] line,
        Map<String,ArrayList<Subline>> mapOfLines,
        Map<String,ArrayList<Stop>> mapOfStopEntry, 
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        String ligne = line[LINE_INDEX] + "_" + line[TYPE_INDEX];
        Subline variantSubline = new Subline(line[VARIANT_INDEX]);
        // La liste de string représentant les stations issu des jonctions
        String[] stops = line[LIST_INDEX].replaceAll("[\\[\\]]", "").split(";");

        // Les stations potentiels issu de la mapOfStopsEntry
        ArrayList<Stop> listOfStopsEntry = mapOfStopEntry.get(ligne);
        if ( listOfStopsEntry == null ){
            LOGGER.warning("Liste des arrêts potentiels vide pour la ligne:" + ligne + ", passage à la prochaine");
            return;
        } 

        // La liste de station finale à ajouter à la subline
        ArrayList<Stop> listOfStops = buildPotentialLine(stops, listOfStopsEntry);

        variantSubline.setListOfStops(listOfStops);
        mapOfLines.get(ligne).add(variantSubline);
    }

    public static ArrayList<Stop> buildPotentialLine(String[] stops, ArrayList<Stop> listOfStopsEntry) {
        ArrayList<Stop> result = new ArrayList<>();
        if (stops.length == 0 || listOfStopsEntry.isEmpty()) {
            LOGGER.warning("Liste des arrêts vide, impossible de reconstruire la ligne");
            return result;
        }

        // On build la liste de départs potentiels
        ArrayList<Stop> listOfPotentialDeparture = new ArrayList<>();
        for (Stop stop : listOfStopsEntry) {
            if (stop.getNameOfAssociatedStation().equals(stops[0])) {
                listOfPotentialDeparture.add(stop);
            }
        }

        if (listOfPotentialDeparture.isEmpty()) {
            LOGGER.warning("Pas de terminus de départ trouvé correspondant à: " + stops[0]);
            return result;
        }

        // On lance un parcours en profondeur sur chaque départ potentiel
        for (Stop start : listOfPotentialDeparture) {
            HashSet<Stop> visited = new HashSet<>();
            if (dfsSearch(start, stops, 1, /*visited,*/ result)) {
                return result;
            }
        }

        errorCpt++;
        LOGGER.warning("Pas de chemin potentiel trouvé pour la sous-ligne: " + String.join(", ", stops) + "\n\n\n" );
        return new ArrayList<>();
    }

    // On parcours les chemins potentiels comme un arbre avec un parcours en profondeur
    private static boolean dfsSearch(
        Stop currentStop, 
        String[] stops, 
        int index, 
        //HashSet<Stop> visited, 
        ArrayList<Stop> path) 
    {
        path.add(currentStop);
        //visited.add(currentStop);

        if (index == stops.length) {
            return true;
        }

        for (Stop adjacent : currentStop.getTimeDistancePerAdjacentStop().keySet()) {
            if (/*!visited.contains(adjacent) &&*/ adjacent.getNameOfAssociatedStation().equals(stops[index])) {
                if (dfsSearch(adjacent, stops, index + 1, /*visited,*/ path)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        //visited.remove(currentStop);
        return false;
    }

    public static void addStops(
        String[] line, 
        Map<String,ArrayList<Stop>> mapOfStopEntry, 
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        String[] stopACoordString = line[START_INDEX+1].split(",");
        double stopAlon = Double.parseDouble(stopACoordString[0]);
        double stopAlat = Double.parseDouble(stopACoordString[1]);
        ImmutablePair<Double,Double> stopACoord = new ImmutablePair<>(stopAlon,stopAlat);

        mapOfStops.putIfAbsent(stopACoord, new Stop(stopAlon,stopAlat,line[START_INDEX]));

        String[] stopBCoordString = line[STOP_INDEX+1].split(",");
        double stopBlon = Double.parseDouble(stopBCoordString[0]);
        double stopBlat = Double.parseDouble(stopBCoordString[1]);
        ImmutablePair<Double,Double> stopBCoord = new ImmutablePair<>(stopBlon,stopBlat);

        mapOfStops.putIfAbsent(stopBCoord, new Stop(stopBlon,stopBlat,line[STOP_INDEX]));

        Stop stopA = mapOfStops.get(stopACoord);
        Stop stopB = mapOfStops.get(stopBCoord);

        Duration timeToNextStation = parseLargeDuration(line[DURATION_INDEX]);

        Float distanceToNextStation = Float.parseFloat(line[DISTANCE_INDEX]);

        stopA.addAdjacentStop(stopB, timeToNextStation, distanceToNextStation);
        String ligne = line[LINE_INDEX] + "_" + line[TYPE_INDEX];
        mapOfStopEntry.get(ligne).add(stopA);
        mapOfStopEntry.get(ligne).add(stopB);
    }
}