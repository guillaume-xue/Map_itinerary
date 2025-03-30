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
import java.util.Set;
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

    private static final Logger LOGGER = Logger.getLogger(CSVExtractor.class.getName());

    /** Hidden constructor of tool class */
    private CSVExtractor() {
        // Parser class
    }

    /** Convert a degree angle value in a radian angle one.
     * 
     * @param args The list of files to parse
     */
    public static Graph makeOjectsFromCSV(String[] args){
        // --parse <stops_data.csv> <junctions_data.csv>

        if (args == null) {
            LOGGER.severe("Invalid command line for parsing CSV. Missing target path.");
            return null;
        }

        Map<String,ArrayList<Subline>> mapOfLines = new HashMap<>();
        Map<String,ArrayList<Stop>> mapOfStopEntry = new HashMap<>();
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops = new HashMap<>();

        // On ajoute les stops et les lignes ( sans les sous-lignes ) à leur map respectives.
        try{
            CSVTools.readCSVFromFile(args[STOPS_DATA_ID],(String[] line) -> 
                readStops(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Stops data file", e);
        }

        // On read le junctionsData pour ajouter les sous-lignes
        try{
            CSVTools.readCSVFromFile(args[JUNCTIONS_DATA_ID],(String[] line) -> 
                readJunctions(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Junctions data file", e);
        }

        ArrayList<Stop> listOfStops = new ArrayList<>(mapOfStops.values());

        ArrayList<Line> listOfLines = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Subline>> entry : mapOfLines.entrySet()) {
            listOfLines.add(new Line(entry.getKey(), entry.getValue()));
        }

        //Collections.sort(listOfStops);
        //Collections.sort(listOfLines);
        Graph graph = new Graph(listOfStops, listOfLines);

        System.out.println("Nombre d'arrêts (quais) uniques trouvés: " + listOfStops.size());
        System.out.println("Nombre de lignes trouvées: " + mapOfLines.size());
        System.out.println("Nombre de sous-lignes trouvées: " + mapOfLines.get("7_Subway").size());
        System.out.println("Nombre de sous-lignes n'ayant pas trouvé leur chemin associé: " + errorCpt);

        return graph;
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
        // La ligne sur laquelle on travaille, avec son index et son type concaténé
        String ligne = line[LINE_INDEX] + "_" + line[TYPE_INDEX];
        Subline variantSubline = new Subline(line[VARIANT_INDEX]); // La sous-ligne
        // La liste de string représentant les stations de la sous-ligne
        String[] stops = line[LIST_INDEX].replaceAll("[\\[\\]]", "").split(";");

        // Les stations potentielles recontrées précédemment pour la ligne
        ArrayList<Stop> listOfStopsEntry = mapOfStopEntry.get(ligne);
        if ( listOfStopsEntry == null ){
            LOGGER.warning("Liste des arrêts potentiels vide pour la ligne:" + ligne + ", passage à la prochaine");
            return;
        } 

        // La liste de stations finale à ajouter à la subline
        ArrayList<Stop> listOfStops = buildPotentialLine(stops, listOfStopsEntry, ligne, line[VARIANT_INDEX]);

        variantSubline.setListOfStops(listOfStops);
        mapOfLines.get(ligne).add(variantSubline);
    }

    public static ArrayList<Stop> buildPotentialLine(String[] stops, ArrayList<Stop> listOfStopsEntry, String ligne, String variant) {
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
        for (Stop departure : listOfPotentialDeparture) {
            if (dfsSearch(departure, stops, 1, listOfStopsEntry ,result)) {
                return result;
            }
        }

        errorCpt++;
        LOGGER.warning("Pas de chemin potentiel trouvé pour la sous-ligne " + ligne + ", variant " + variant + ": " + String.join(", ", stops) + "\n\n\n" );
        return new ArrayList<>();
    }

    // On parcours les chemins potentiels comme un arbre avec un parcours en profondeur
    private static boolean dfsSearch(
        Stop currentStop,
        String[] stops,
        int index,
        ArrayList<Stop> listOfStopsEntry,      
        ArrayList<Stop> potentialPath
    ){
        potentialPath.add(currentStop);

        if (index == stops.length) {
            return true;
        }

        // Set<Stop> potentialAdjacent = currentStop.getTimeDistancePerAdjacentStop().keySet();

        // for ( Stop st : listOfStopsEntry ){
        //     if (!potentialAdjacent.contains(st)){
        //         potentialAdjacent.remove(st);
        //     }
        // } 

        // currentStop.getTimeDistancePerAdjacentStop().keySet()
        for ( Stop adjacent : currentStop.getTimeDistancePerAdjacentStop().keySet() ){
            if ( adjacent.getNameOfAssociatedStation().equals(stops[index]) ){
                if ( dfsSearch(adjacent, stops, index + 1, listOfStopsEntry, potentialPath) ){
                    return true;
                }
            }
        }

        potentialPath.remove(potentialPath.size() - 1);
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