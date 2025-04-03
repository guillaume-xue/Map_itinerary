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
 * A tool class to extract and convert CSV data provided into our model objects.
 * This class expects to read the data with the Upgraded Network Format. 
 *
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

    /** 
     * Main entry point of the parser. 
     * 
     * @param args The String array of file paths. Expects:
     * index 1 being the file for stops data and
     * index 2 being the file for junctions data.
     *             
     * @return The generated Graph object representing the transport network, 
     * null if parsing fails.
     * 
     */
    public static Graph makeOjectsFromCSV(String[] args){

        if (args == null) {
            LOGGER.severe("Error: Objects parsing failed. Missing targets paths in the arguments");
            return null;
        }

        // Associe le nom de la ligne à ses sous-lignes
        Map<String,ArrayList<Subline>> mapOfLines = new HashMap<>();
        // Asssocie le nom de la ligne à toutes les stations rencontrées dessus
        Map<String,ArrayList<Stop>> mapOfStopEntry = new HashMap<>();
        // Associe des coordonnées à une station
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops = new HashMap<>();

        // On ajoute les stations et les lignes ( sans les sous-lignes ) à leur map respectives.
        try{
            CSVTools.readCSVFromFile(args[STOPS_DATA_ID],(String[] line) -> 
                readStops(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Stops data file", e);
        }

        // On read le junctionsData pour associer les sous-lignes à leur ligne
        try{
            CSVTools.readCSVFromFile(args[JUNCTIONS_DATA_ID],(String[] line) -> 
                readJunctions(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Junctions data file", e);
        }

        // (Ticket #20) Une fois qu'on a rempli la mapOfStops on doit ajouter les quais de
        // chaque station entre elles pour permettre de changer de ligne, à pied,
        // sur une même station.
        linkStops(mapOfStops, 150);

        // Une fois qu'on a trouvé toutes les stations uniques on fait de la mapOfStops
        // une liste pour l'objet Graph
        ArrayList<Stop> listOfStops = new ArrayList<>(mapOfStops.values());

        // Pareil pour les lignes
        ArrayList<Line> listOfLines = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Subline>> entry : mapOfLines.entrySet()) {
            listOfLines.add(new Line(entry.getKey(), entry.getValue()));
        }

        //Collections.sort(listOfStops);
        //System.out.println(listOfStops.toString());

        //Collections.sort(listOfLines);
        Graph graph = new Graph(listOfStops, listOfLines);

        System.out.println("Nombre d'arrêts (quais) uniques trouvés: " + listOfStops.size());
        System.out.println("Nombre de lignes trouvées: " + mapOfLines.size());
        System.out.println("Nombre de sous-lignes n'ayant pas trouvé leur chemin associé: " + errorCpt);

        return graph;
    }

    /**
     * Adds the transport line from the tuple using data from the provided stops CSV file.
     * Then calls the function to add the stops.
     *
     * @param      line            The text line from csv being read
     * @param      mapOfLines      The map of lines
     * @param      mapOfStopEntry  The map of stop entry
     * @param      mapOfStops      The map of stops
     */
    public static void readStops(
        String[] line, 
        Map<String,ArrayList<Subline>> mapOfLines,
        Map<String,ArrayList<Stop>> mapOfStopEntry,
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        // On ajoute la ligne de transport du tuple si elle est nouvelle
        mapOfLines.putIfAbsent(line[LINE_INDEX] + "_" + line[TYPE_INDEX], new ArrayList<>());
        mapOfStopEntry.putIfAbsent(line[LINE_INDEX] + "_" + line[TYPE_INDEX], new ArrayList<>());
        // On ajoute les deux stations à la map
        addStops(line, mapOfStopEntry, mapOfStops);
    }

    /**
     * Adds the stops of the tuple being read. 
     *
     * @param      line            The text line from csv being read
     * @param      mapOfStopEntry  The map of stop entry
     * @param      mapOfStops      The map of stops
     */
    public static void addStops(
        String[] line, 
        Map<String,ArrayList<Stop>> mapOfStopEntry, 
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){

        // On lit les coordonnées du premiet arrêt
        String[] stopACoordString = line[START_INDEX+1].split(",");
        double stopAlon = Double.parseDouble(stopACoordString[0]);
        double stopAlat = Double.parseDouble(stopACoordString[1]);
        // On en fait une paire qui sert de clef primaoire
        ImmutablePair<Double,Double> stopACoord = new ImmutablePair<>(stopAlon,stopAlat);

        // On ajoute l'arrêt si il n'a pas déjà été rencontré
        mapOfStops.putIfAbsent(stopACoord, new Stop(stopAlon,stopAlat,line[START_INDEX]));

        // Pareil avec le deuxième arrêt
        String[] stopBCoordString = line[STOP_INDEX+1].split(",");
        double stopBlon = Double.parseDouble(stopBCoordString[0]);
        double stopBlat = Double.parseDouble(stopBCoordString[1]);
        ImmutablePair<Double,Double> stopBCoord = new ImmutablePair<>(stopBlon,stopBlat);

        mapOfStops.putIfAbsent(stopBCoord, new Stop(stopBlon,stopBlat,line[STOP_INDEX]));

        // On recupère les objets Stop à partir de la map pour avoir la bonne référence
        Stop stopA = mapOfStops.get(stopACoord);
        Stop stopB = mapOfStops.get(stopBCoord);

        Duration timeToNextStation = parseLargeDuration(line[DURATION_INDEX]);
        Float distanceToNextStation = Float.parseFloat(line[DISTANCE_INDEX]);

        // On ajoute l'arrêt B à la liste d'adjacence de l'arrêt A avec la 
        // durée/distance de transport
        stopA.addAdjacentStop(stopB, timeToNextStation, distanceToNextStation);
        
        // On ajoute les deux arrêts à la map qui associe une ligne à tout ses 
        // arrêts rencontrés
        String ligne = line[LINE_INDEX] + "_" + line[TYPE_INDEX];
        if (!mapOfStopEntry.get(ligne).contains(stopA)) {
            mapOfStopEntry.get(ligne).add(stopA);
        }
        if (!mapOfStopEntry.get(ligne).contains(stopB)) {
            mapOfStopEntry.get(ligne).add(stopB);
        }
    }

    /**
     * Reads a line from the junctions data CSV and adds the potential path to the
     * transport line.
     * 
     * @param      line            The text line from csv being read
     * @param      mapOfLines      The map of lines
     * @param      mapOfStopEntry  The map of stop entry
     * @param      mapOfStops      The map of stops
     */
    public static void readJunctions(
        String[] line,
        Map<String,ArrayList<Subline>> mapOfLines,
        Map<String,ArrayList<Stop>> mapOfStopEntry, 
        Map<ImmutablePair<Double,Double>,Stop> mapOfStops
    ){
        // La ligne de transport sur laquelle on travaille, avec son index et son type concaténé
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

    /**
     * Builds a potential line.
     *
     * @param      stops             The stops
     * @param      listOfStopsEntry  The list of stops entry
     * @param      ligne             The transport line
     * @param      variant           The variant
     *
     * @return     The list of stops representing the Subline.
     */
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

        // Si on n'a pas trouvé de départs potentiels
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

    /**
     * Depth-first search to find a potential path for the subline. 
     *
     * @param      currentStop       The current stop
     * @param      stops             The stops
     * @param      index             The index
     * @param      listOfStopsEntry  The list of stops entry
     * @param      potentialPath     The potential path
     *
     * @return     True if a path has been found, false otherwise.
     */
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
    
    /**
     * Links all platforms of a station to allow passengers to change lines on foot.  
     * 'maxDistance' is an arbitrary value, in meters, defining the radius within a platform  
     * is considered part of the same station.
     * 
     * FIXME: probably needs a better solution than an arbitrary value ( 150 meters right now )
     *
     * @param      mapOfStops   The map of stops
     * @param      maxDistance  The maximum distance in meters
     */
    public static void linkStops(Map<ImmutablePair<Double,Double>,Stop> mapOfStops, double maxDistance){
        // On regroupe les stations par nom
        Map<String, ArrayList<Stop>> stopsByName = new HashMap<>();
        for (Stop stop : mapOfStops.values()) {
            stopsByName.putIfAbsent(stop.getNameOfAssociatedStation(), new ArrayList<>());
            stopsByName.get(stop.getNameOfAssociatedStation()).add(stop);
        }

        // On parcours les listes de quais de même nom et si leur distance est infieure à la 
        // valeur arbitraire choisie on les ajoute entre-eux dans leur liste d'adjacence.
        for ( ArrayList<Stop> stops : stopsByName.values() ){
            for ( int i = 0; i < stops.size(); i++) {
                for ( int j = i + 1; j < stops.size(); j++ ){
                    Stop stopA = stops.get(i);
                    Stop stopB = stops.get(j);
                    
                    // En km
                    double distance = GPS.distance(stopA.getLatitude(), stopA.getLongitude(),
                                                   stopB.getLatitude(), stopB.getLongitude());

                    // En mètre
                    if ( distance * 1000 <= maxDistance ){
                        // Calcul issu de CSVStreamProviderForMapData -> get()
                        Duration duration = Duration.ofSeconds((long) Math.ceil( (distance / WALK_AVG_SPEED) * 3600));
                        
                        stopA.addAdjacentStop(stopB, duration, (float) distance);
                        stopB.addAdjacentStop(stopA, duration, (float) distance);
                    }
                }
            }
        }

    }

}