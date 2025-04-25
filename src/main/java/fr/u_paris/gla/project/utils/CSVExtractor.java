/**
 * 
 */
package fr.u_paris.gla.project.utils;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.io.File;
import java.util.stream.Collectors;

import fr.u_paris.gla.project.graph.*;
import fr.u_paris.gla.project.io.UpgradedNetworkFormat;
import fr.u_paris.gla.project.io.JunctionsFormat;
import fr.u_paris.gla.project.io.ScheduleFormat;
import java.time.Duration;
import java.time.LocalTime;
import fr.u_paris.gla.project.utils.TransportTypes;

/**
 * A tool class to extract and convert CSV data provided into our model objects.
 * This class expects to read the data with the Upgraded Network Format. 
 *
 */
public final class CSVExtractor {

    // Constantes:
    // Index pour les arguments
    private static final int STOPS_FILE_ID = 1;
    private static final int JUNCTIONS_FILE_ID = 2;
    private static final int SCHEDULE_DIR_ID = 3;

    // Distance maximale pour le linking des quais
    private static final int MAX_DISTANCE = 150;

    private static final Logger LOGGER = Logger.getLogger(CSVExtractor.class.getName());

    /** Hidden constructor of tool class */
    private CSVExtractor() {
        // Parser class
    }

    /** 
     * Main entry point of the parser. Launches the step, one by one, required to make 
     * the Graph object of our model.
     * 
     * @param args The String array of file paths. Expects:
     * index 1 being the file for stops data.
     * index 2 being the file for junctions data.
     * index 3 being the directory for schedules.
     *             
     * @return The generated Graph object representing the transport network,
     * null if parsing critically fails.
     * 
     */
    public static Graph makeObjectsFromCSV(String[] args){

        if ( !areArgumentsValid(args) ){
            LOGGER.severe("Error: Wrong arguments for objects parser. See Usage.");
            return null;
        }

        LOGGER.info("Objects parsing in progress: reading Stops data");

        // Une map qui associe l'ID d'une ligne à ses sous-lignes
        Map<String,ArrayList<Subline>> mapOfLines = new HashMap<>();

        // Une map qui asssocie l'ID d'une ligne à toutes les stations rencontrées dessus
        Map<String,ArrayList<Stop>> mapOfStopEntry = new HashMap<>();

        // Une map qui associe l'ID d'une ligne à son objet
        Map<String, Line> lineById = new HashMap<>();

        // Une map qui associe une paire de coordonnées à une station
        Map<Pair<Double,Double>,Stop> mapOfStops = new HashMap<>();

        // On ajoute les stations et les lignes (sans les sous-lignes) à leur map respective
        try{
            CSVTools.readCSVFromFile(args[STOPS_FILE_ID], (String[] line) -> {
                if ( isMapDataLineValid(line) ) {
                    readStops(line, mapOfLines, mapOfStopEntry, mapOfStops, lineById);
                } else {
                    LOGGER.warning("Invalid line skipped: " + line.toString());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Stops data file", e);
            return null;
        }

        LOGGER.info("Objects parsing in progress: reading Junctions data");

        // On associe les sous-lignes à leur ligne
        try{
            CSVTools.readCSVFromFile(args[JUNCTIONS_FILE_ID],(String[] line) -> 
                readJunctions(line, mapOfLines, mapOfStopEntry, mapOfStops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the Junctions data file", e);
        }

        // On read les horaires de départs pour les terminus
        LOGGER.info("Objects parsing in progress: reading Schedules data");
        File scheduleDir = new File(args[SCHEDULE_DIR_ID]);
        File[] scheduleFiles = scheduleDir.listFiles();
        // Pour chaque fichier du dossier
        if ( scheduleFiles != null ){
            for ( File file : scheduleFiles ){
                try{
                    CSVTools.readCSVFromFile(file.toPath().toString(),(String[] line) ->
                        readSchedules(line, file.getName(), mapOfLines, mapOfStopEntry));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error while reading the Schedules files", e);
                }
            }
        }

        // (Ticket #20) Une fois qu'on a rempli la mapOfStops on doit ajouter les quais de
        // chaque station entre elles pour permettre de changer de ligne, à pied, sur une même station.
        LOGGER.info("Objects parsing in progress: linking Stops");
        linkStops(mapOfStops, MAX_DISTANCE);

        // Maintenant que les map ont été remplies et toutes les informations ajoutées, on les transforme
        // en liste (requis par le model).
        ArrayList<Stop> listOfStops = new ArrayList<>(mapOfStops.values());

        ArrayList<Line> listOfLines = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Subline>> entry : mapOfLines.entrySet()) {
            Line tmp = lineById.get(entry.getKey());
            tmp.setListOfSublines(entry.getValue());
            tmp.setSublinesTransportType();
            listOfLines.add( tmp );
        }

        // Maintenant qu'on a ajouté les horaires de départs depuis les terminus, on ajoute les horaires
        // pour toutes les autres stations selon leurs adjacences.
        LOGGER.info("Objects parsing in progress: adding all other schedules");
        addMissingSchedules(listOfLines);
        
        //Collections.sort(listOfStops);
        //Collections.sort(listOfLines);
        
        Graph graph = new Graph(listOfStops, listOfLines);
        LOGGER.info("Objects parsing finished: Graph done");

        System.out.println(graph.statsToString());

        return graph;
    }

    /**
     * Determines whether the arguments of the main function are valid.
     * Checks that there are four arguments and that noone of them are empty.
     *
     * @param      args  The arguments
     *
     * @return     True if the arguments are valid, False otherwise.
     */
    public static boolean areArgumentsValid(String[] args) {
        if ( args.length != 4 ) return false;
        for ( String arg : args ){
            if ( arg.isEmpty() ){ return false; }
        }

        return true;
    }

    /**
     * Determines whether the specified text line is valid.
     * Checks that there are exactly NetworkFormat.NB_COLUMNS ( 9 ) columns, 
     * And that noone of them are empty.
     *
     * @param      line  The text line
     *
     * @return     True if the specified line is valid, False otherwise.
     */
    public static boolean isMapDataLineValid(String[] line) {
        if ( line.length != UpgradedNetworkFormat.NUMBER_COLUMNS ) return false;
        
        for ( String str : line ){
            if ( str.isEmpty() ) { return false; }
        }

        return true;
    }

    /**
     * Reads the info of the transport line from the tuple and calls the function to add the stops.
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
        Map<Pair<Double,Double>,Stop> mapOfStops,
        Map<String,Line> lineById
    ){
        // On ajoute la ligne de transport du tuple si elle n'a pas été rencontrée précedemment
        mapOfLines.putIfAbsent(line[UpgradedNetworkFormat.LINE_ID_INDEX], new ArrayList<>());
        mapOfStopEntry.putIfAbsent(line[UpgradedNetworkFormat.LINE_ID_INDEX], new ArrayList<>());
        lineById.putIfAbsent(line[UpgradedNetworkFormat.LINE_ID_INDEX], new Line(
            line[UpgradedNetworkFormat.LINE_ID_INDEX],
            line[UpgradedNetworkFormat.LINE_NAME_INDEX],
            line[UpgradedNetworkFormat.TYPE_INDEX],
            line[UpgradedNetworkFormat.COLOR_INDEX]
        ));

        // On ajoute les deux stations à la map
        addStops(line, mapOfStopEntry, mapOfStops);
    }

    /**
     * Adds the stops of the tuple being read. 
     *
     * @param      line            The text line
     * @param      mapOfStopEntry  The map of stop entry
     * @param      mapOfStops      The map of stops
     */
    public static void addStops(
        String[] line, 
        Map<String,ArrayList<Stop>> mapOfStopEntry, 
        Map<Pair<Double,Double>,Stop> mapOfStops
    ){

        // On lit les coordonnées du premiet arrêt
        String[] stopACoordString = line[UpgradedNetworkFormat.START_INDEX+1].split(",");
        double stopAlon = Double.parseDouble(stopACoordString[0]);
        double stopAlat = Double.parseDouble(stopACoordString[1]);
        // On en fait une paire qui sert de clef primaire
        Pair<Double,Double> stopACoord = new Pair<>(stopAlon,stopAlat);
        
        // On ajoute l'arrêt si il n'a pas déjà été rencontré
        mapOfStops.putIfAbsent(stopACoord, new Stop(stopAlon,stopAlat,line[UpgradedNetworkFormat.START_INDEX]));

        // Pareil avec le deuxième arrêt
        String[] stopBCoordString = line[UpgradedNetworkFormat.STOP_INDEX+1].split(",");
        double stopBlon = Double.parseDouble(stopBCoordString[0]);
        double stopBlat = Double.parseDouble(stopBCoordString[1]);
        Pair<Double,Double> stopBCoord = new Pair<>(stopBlon,stopBlat);

        mapOfStops.putIfAbsent(stopBCoord, new Stop(stopBlon,stopBlat,line[UpgradedNetworkFormat.STOP_INDEX]));

        // On recupère les objets Stop à partir de la map pour avoir la bonne référence
        Stop stopA = mapOfStops.get(stopACoord);
        Stop stopB = mapOfStops.get(stopBCoord);

        Duration timeToNextStation = UpgradedNetworkFormat.parseLargeDuration(line[UpgradedNetworkFormat.DURATION_INDEX]);
        Float distanceToNextStation = Float.parseFloat(line[UpgradedNetworkFormat.DISTANCE_INDEX]);

        // On ajoute l'arrêt B à la liste d'adjacence de l'arrêt A avec la 
        // durée/distance de transport
        stopA.addAdjacentStop(stopB, line[UpgradedNetworkFormat.TYPE_INDEX],timeToNextStation, distanceToNextStation);
        
        // On ajoute les deux arrêts à la map qui associe une ligne à tout ses 
        // arrêts rencontrés
        String ligne = line[UpgradedNetworkFormat.LINE_ID_INDEX];
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
        Map<Pair<Double,Double>,Stop> mapOfStops
    ){
        // La ligne de transport
        String ligne = line[UpgradedNetworkFormat.LINE_ID_INDEX];

        // La sous-ligne
        Subline variantSubline = new Subline(line[JunctionsFormat.VARIANT_INDEX]);
        
        // La liste de string représentant les stations de la sous-ligne
        String[] stops = line[JunctionsFormat.LIST_INDEX].replaceAll("[\\[\\]]", "").split(";");

        // Les stations potentielles recontrées précédemment pour la ligne
        ArrayList<Stop> listOfStopsEntry = mapOfStopEntry.get(ligne);
        if ( listOfStopsEntry == null ){
            LOGGER.warning("Liste des arrêts potentiels vide pour la ligne:" + ligne + ", passage à la prochaine");
            return;
        } 

        // La liste de stations finale à ajouter à la subline
        ArrayList<Stop> listOfStops = buildPotentialLine(stops, listOfStopsEntry, ligne, line[JunctionsFormat.VARIANT_INDEX]);
        if ( listOfStops.isEmpty() ){
            LOGGER.warning("Suppression de la sous-ligne");
            return;
        }

        variantSubline.setListOfStops(listOfStops);
        mapOfLines.get(ligne).add(variantSubline);
    }

    /**
     * Builds a potential line by calling a DFS-search.
     *
     * @param      stops             The textual stops
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

        LOGGER.warning("Pas de chemin potentiel trouvé pour la sous-ligne " + ligne + ", variant " + variant + "\n" );
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

        for ( Pair<Stop, TransportTypes> entry : currentStop.getTimeDistancePerAdjacentStop().keySet() ){
            Stop adjacent = entry.getKey();
            
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
     * Read a schedule file in the fileName format: "lineName_lineID_departureStop.csv"
     * Adds the corresponding departure time for the subline.
     *
     * @param      line        The text line
     * @param      fileName    The file name
     * @param      mapOfLines  The map of lines
     */
    public static void readSchedules(
        String[] line, 
        String fileName, 
        Map<String,ArrayList<Subline>>  mapOfLines,
        Map<String,ArrayList<Stop>> mapOfStopEntry
    )
    {

        // Si le fichier fourni n'est pas un csv, on le skip
        if (!fileName.endsWith(".csv")) return;

        // La ligne
        String ligne = line[ScheduleFormat.LINE_ID_INDEX];
        
        // On récupère la liste des sous-lignes
        ArrayList<Subline> allSublines = mapOfLines.get(ligne);
        if ( allSublines == null ) return;

        String variant = line[ScheduleFormat.TRIP_SEQUENCE_INDEX].substring(1, line[ScheduleFormat.TRIP_SEQUENCE_INDEX].length() - 1);
        String expectedTerminus = line[ScheduleFormat.TERMINUS_INDEX];    

        // FIXME: As variant is an index we could skip the for each subline, assuming the list of
        // sublines is sorted, with: allSublines.get(Integer.parseInt(variant))
        for (Subline subline : allSublines) {
            if ( !variant.equals(subline.getName()) ) continue;

            ArrayList<Stop> stops = subline.getListOfStops();
            if (stops == null || stops.isEmpty()) return;

            String actualTerminus = stops.get(0).getNameOfAssociatedStation();

            if (!actualTerminus.equals(expectedTerminus)) {
                LOGGER.log(Level.WARNING,"Mismatch found on subline: " + subline.getName() + " of line: " + ligne
                + "\nExpected: " + expectedTerminus + " | Found: " + actualTerminus );
                return;
            }


            try{
                Stop departureStop = stops.get(0);
                LocalTime scheduleToAdd = LocalTime.parse(line[ScheduleFormat.TIME_INDEX]);
                if ( subline.getDepartureTimes().contains(scheduleToAdd)) return;
                departureStop.addDeparture(subline, scheduleToAdd);
                subline.addDepartureTimes(departureStop, new ArrayList<>(List.of(scheduleToAdd)));
            } catch (Exception e){
                LOGGER.log(Level.WARNING, "Failed to add departure times: " + e.getMessage());
            }
        }
    }

    public static void addMissingSchedules(ArrayList<Line> listOfLines){
        for ( Line line : listOfLines ){
            for ( Subline subline : line.getListOfSublines() ){
                // La liste de Stop qui représente la sous-ligne, qu'on skip si vide
                ArrayList<Stop> stops = subline.getListOfStops();
                if (stops.isEmpty()) continue;

                // Le terminus de départ
                Stop departureStop = stops.get(0);

                // Les horaires de départ
                ArrayList<LocalTime> departureTimes = subline.getDepartureTimes();

                // On parcourt la liste de stops en récupérant la durée entre l'arrêt précédent 
                // et celui auquel on veut ajouter l'horaire
                for (LocalTime lt : departureTimes) {
                LocalTime currentTime = lt;

                for (int i = 1; i < stops.size(); i++) {
                    Stop previousStop = stops.get(i - 1);
                    Stop currentStop = stops.get(i);

                    Duration durationToNext = previousStop.getTimeDistancePerAdjacentStop()
                        .get(new Pair<>(currentStop, line.getType())).getKey();

                    currentTime = currentTime.plus(durationToNext);

                    currentStop.addDeparture(subline, currentTime);
                }
            }
            }
        }   
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
    public static void linkStops(Map<Pair<Double,Double>,Stop> mapOfStops, double maxDistance){
        
        // On regroupe les stations par nom
        Map<String, ArrayList<Stop>> stopsByName = new HashMap<>();
        for ( Stop stop : mapOfStops.values() ){
            stopsByName.putIfAbsent(stop.getNameOfAssociatedStation(), new ArrayList<>());
            stopsByName.get(stop.getNameOfAssociatedStation()).add(stop);
        }

        // On parcours les listes de quais de même nom et si leur distance est infieure à la 
        // valeur arbitraire choisie on les ajoute entre-eux dans leur liste d'adjacence.
        for ( ArrayList<Stop> stops : stopsByName.values() ){
            for ( int i = 0; i < stops.size(); i++){
                for ( int j = i + 1; j < stops.size(); j++ ){
                    Stop stopA = stops.get(i);
                    Stop stopB = stops.get(j);
                    
                    // En km
                    double distance = GPS.distance(stopA.getLatitude(), stopA.getLongitude(),
                                                   stopB.getLatitude(), stopB.getLongitude());

                    // En mètre
                    if ( distance * 1000 <= maxDistance ){
                        // Calcul issu de CSVStreamProviderForMapData -> get()
                        Duration duration = 
                        Duration.ofSeconds((long) Math.ceil( (distance / UpgradedNetworkFormat.WALK_AVG_SPEED) * 3600));
                        
                        stopA.addAdjacentStop(stopB, "Walk", duration, (float) distance);
                        stopB.addAdjacentStop(stopA, "Walk", duration, (float) distance);
                    }
                }
            }
        }

    }

}