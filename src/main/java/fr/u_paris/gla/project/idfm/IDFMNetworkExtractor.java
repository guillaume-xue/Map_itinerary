/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.io.IOException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.u_paris.gla.project.utils.CSVTools;
import fr.u_paris.gla.project.utils.GPS;

import java.io.File;
import java.io.FileWriter;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.Optional;
import java.time.LocalTime;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Objects;
import java.util.Iterator;

/** Code of an extractor for the data from IDF mobilite.
 * 
 * @author Emmanuel Bigeon */
public class IDFMNetworkExtractor {

    /** The logger for information on the process */
    private static final Logger LOGGER = Logger
            .getLogger(IDFMNetworkExtractor.class.getName());

    // IDF mobilite API URLs
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
   
    //IDF mobilite provided schedule file
    private static final String SCHEDULE_FILE_NAME = "fr/u_paris/gla/project/stop_times_sample.txt";
    private static final String TRIPS_FILE_NAME = "fr/u_paris/gla/project/trips_sample.txt";
    
    //ID;Short Name;Long Name;Route Type;Color;Route URL;Shape;id_ilico;OperatorName;NetworkName;URL;long_name_first;geo_point_2d

    // IDF mobilite csv formats
    private static final int IDFM_TRACE_ID_INDEX    = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_SHAPE_INDEX = 6;

    //route_id;route_long_name;stop_id;stop_name;stop_lon;stop_lat;OperatorName;shortName;mode;Pointgeo;Nom_commune;Code_insee

    private static final int IDFM_STOPS_RID_INDEX  = 0;
    private static final int IDFM_STOPS_ID_INDEX = 2;
    private static final int IDFM_STOPS_NAME_INDEX = 3;
    private static final int IDFM_STOPS_LON_INDEX  = 4;
    private static final int IDFM_STOPS_LAT_INDEX  = 5;

    //the useful indexes for stop_times.txt file
    private static final int SCHEDULE_TRIP_ID = 0;
    private static final int SCHEDULE_DEPARTURES_TIME = 2;
    private static final int SCHEDULE_STOPS_ID = 5;
    
    //the useful indexes for trips.txt file
    private static final int IDFM_ROUTE_ID = 0;
    private static final int IDFM_TRIP_ID = 2;
    
    // Magically chosen values
    /** A number of stops on each line */
    private static final int GUESS_STOPS_BY_LINE = 5;

    // Well named constants
    private static final double QUARTER_KILOMETER = .25;

    /**
     * 
     * @param args one is the output file and the other is the target directory 
     * for csv files containing schedules data
     */
    public static void parse(String[] args) {
    	if (args.length != 3) {
            LOGGER.severe("Invalid command line. Needs two target files and a target repertory.");
            return;
        }
    	Map<String, TraceEntry> traces = parseMapData(new String[] {args[0]});
    	
    	
    	if (traces != null) {
    		File directory = new File(args[2]);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        	parseScheduleDataWithBifurcations(directory, args[1], traces);
        }
    }
    
    /** parses the map data from given URL and makes the csv containing 
     * map data required by the client
     * 
     * @param args the arguments (expected one for the destination file) */
    private static Map<String, TraceEntry> parseMapData(String[] args) {

        Map<String, TraceEntry> traces = new HashMap<>();
        try {
            CSVTools.readCSVFromURL(TRACE_FILE_URL,
                    (String[] line) -> addLine(line, traces));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the line paths", e);
        }

        List<StopEntry> stops = new ArrayList<>(traces.size() * GUESS_STOPS_BY_LINE);
        try {
            CSVTools.readCSVFromURL(STOPS_FILE_URL,
                    (String[] line) -> addStop(line, traces, stops));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the stops", e);
        }

        cleanTraces(traces);

        CSVStreamProvider provider = new CSVStreamProvider(traces.values().iterator());

        try {
            CSVTools.writeCSVToFile(args[0], Stream.iterate(provider.next(),
                    t -> provider.hasNext(), t -> provider.next()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", args[0]));
        }
        return traces;
    }
       
    
    private static void parseScheduleDataWithBifurcations(File directory, String junctionsFile, Map<String, TraceEntry> traces) {
        Map<String, List<String>> tripsByRoutes = createMapRoutesAndTripsData();
        Map<String, List<Pair<String, String>>> itinerariesByTrip = findItineraryForEachTrip();
        
        //ici porte de paris apparait dans le trip qui part de carrefour pleyel
        //printItineraryForTrip("IDFM:RATP:178016-C01383-COU_RATP_5120308_2467828_5", itinerariesByTrip);

        Map<Pair<String, String>, List<Pair<String, String>>> bifurcations = new HashMap<>();
        Map<Pair<String, String>, List<String>> bifurcationStops = new HashMap<>();

        fillMapsForScheduleAndJunctionsFiles(tripsByRoutes, traces, itinerariesByTrip, bifurcations, bifurcationStops);

        // Affichage des résultats
        //System.out.println("Bifurcations: " + bifurcations);
        //System.out.println("Bifurcation Stops: " + bifurcationStops);
        
        fillAllScheduleFiles(bifurcations, directory);
        fillJunctionsFile(junctionsFile, bifurcationStops);
    }

    private static void fillMapsForScheduleAndJunctionsFiles(
    		Map<String, List<String>> tripsByRoutes, Map<String, TraceEntry> traces, 
    		Map<String, List<Pair<String, String>>> itinerariesByTrip,
    		Map<Pair<String, String>, List<Pair<String, String>>> bifurcations,
    		Map<Pair<String, String>, List<String>> bifurcationStops) {
    	
        for (Map.Entry<String, List<String>> routeEntry : tripsByRoutes.entrySet()) {
            String routeId = routeEntry.getKey();
            TraceEntry trace = traces.get(routeId);
            if (trace == null) {
                continue;
            }
            String routeLname = trace.lname; // Nom de la ligne

            Map<List<String>, String> sequenceIndexMap = new HashMap<>();
            
            for (String tripId : routeEntry.getValue()) {
                List<Pair<String, String>> itinerary = itinerariesByTrip.get(tripId);
                if (itinerary == null || itinerary.isEmpty()) {
                    continue;
                }

                // Remplacement des stop_id par leurs lname
                List<String> stopLnameSequence = new ArrayList<>();
                for (Pair<String, String> pair : itinerary) {
                    String stopId = pair.getRight();
                    String stopLname = getStopLnameFromTraces(trace, stopId);
                    if (stopLname == null) {
                        stopLnameSequence.add("UNKNOWN_STATION");
                    }
                    else {
                    	stopLnameSequence.add(stopLname);
                    }
                }

                if (stopLnameSequence.isEmpty()) {
                    continue;
                }

                // Récupérer l'heure et le premier stop lname
                String heureDepart = itinerary.get(0).getLeft();
                String nomStationDepart = stopLnameSequence.get(0);

                // Vérifier si cette séquence est déjà enregistrée
                String sequenceIndex = sequenceIndexMap.computeIfAbsent(stopLnameSequence, k -> String.valueOf(sequenceIndexMap.size()));

                // Ajouter la séquence d'arrêts associée
                Pair<String, String> routeBifurcationKey = Pair.of(routeLname, sequenceIndex);
                bifurcationStops.putIfAbsent(routeBifurcationKey, stopLnameSequence);

                // Ajouter l'heure et la bifurcation associée à la nouvelle structure
                Pair<String, String> ligneStationKey = Pair.of(routeLname, nomStationDepart);
                bifurcations.putIfAbsent(ligneStationKey, new ArrayList<>());
                insertSorted(bifurcations.get(ligneStationKey), Pair.of(heureDepart, sequenceIndex));
            }
        }
    }
    
    private static void fillJunctionsFile(String junctionsFile, Map<Pair<String, String>, List<String>> bifurcationStops) {
    	
    	
        CSVStreamProviderForJunctions provider = new CSVStreamProviderForJunctions(bifurcationStops);
        
        try {
        	CSVTools.writeCSVToFile(junctionsFile, Stream.generate(provider).takeWhile(Objects::nonNull));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", junctionsFile));
        }
    }
    
    /**
     * Recherche le lname d'un stop_id donné en parcourant les paths du TraceEntry.
     */
    private static String getStopLnameFromTraces(TraceEntry trace, String stopId) {
        for (List<StopEntry> path : trace.getPaths()) {
            for (StopEntry stop : path) {
                if (stop.getStopId().equals(stopId)) {
                    return stop.lname;
                }
            }
        }
        //System.out.println("JE NE TROUVE PAS CET ARRET "+ stopId);
        return null; // Si aucun match n'est trouvé
    }

    
    //pour verifier à la main
    public static void printItineraryForTrip(String tripId, Map<String, List<Pair<String, String>>> itinerariesByTrip) {
    	// Vérifier si la clé existe dans la map
    	if (!itinerariesByTrip.containsKey(tripId)) {
    		System.out.println("Aucun itinéraire trouvé pour le tripId : " + tripId);
    		return;
    	}

    	// Récupérer la liste des horaires et arrêts associés
    	List<Pair<String, String>> itinerary = itinerariesByTrip.get(tripId);

    	// Afficher chaque paire (heurePassage, stopId)
    	System.out.println("Itinéraire pour le tripId " + tripId + " :");
    	for (Pair<String, String> entry : itinerary) {
    		System.out.println("Heure de passage : " + entry.getLeft() + ", Arrêt : " + entry.getRight());
    	}
    }
    
    /**associe une ligne de transport avec tous les trajets fait dans la journée 
     * (donc peut y avoir des doublons d'itinéraires car un même itinéraire à 
     * 8:00 et à 12:00 ne représente pas un même trip)
     * 
     * @return
     */
    private static Map<String, List<String>> createMapRoutesAndTripsData() {
    	Map<String, List<String>> tripsByRoutes = new HashMap<>();
        try {
            CSVTools.readCSVFromFile(TRIPS_FILE_NAME,
                    (String[] line) -> fillTripsByRoutesMap(line, tripsByRoutes));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the routes", e);
        }
        return tripsByRoutes;
    }
    
    /** lie un trip avec l'ensemble de ses stations parcourues dans le bon ordre
     * pour l'instant dans cette map on peut avoir deux trip_id différents mais 
     * les ordres des stations peuvent etre identiques
     * @return
     */
    private static Map<String, List<Pair<String, String>>> findItineraryForEachTrip() {
    	Map<String, List<Pair<String, String>>> itinerariesByTrip = new HashMap<>();
        try {
            CSVTools.readCSVFromFile(SCHEDULE_FILE_NAME,
                    (String[] line) -> addItineraryToATrip(line, itinerariesByTrip));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the trips", e);
        }
        return itinerariesByTrip;
    }
   
    	
    private static void fillAllScheduleFiles(Map<Pair<String, String>, List<Pair<String, String>>> bifurcations, File directory) {
    	for (Map.Entry<Pair<String, String>, List<Pair<String, String>>> entry : bifurcations.entrySet()) {
    		Pair<String, String> key = entry.getKey();
    		List<Pair<String, String>> timesAndJunctions = entry.getValue();
    		
    		String lineName = key.getLeft();
            String stopName = key.getRight();
            String sanitizedStopName = stopName.replaceAll("[\\\\/]", "_"); //pour eviter les erreurs liés aux / et \ dans les noms des stations
            String fileName = directory + "/" + lineName + "_" + sanitizedStopName + ".csv";
            
            File file = new File(fileName);
            
            
            CSVStreamProviderForSchedules provider = new CSVStreamProviderForSchedules(timesAndJunctions, lineName, stopName);
            
            try {
            	CSVTools.writeCSVToFile(fileName, Stream.generate(provider).takeWhile(Objects::nonNull));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e,
                        () -> MessageFormat.format("Could not write in file {0}", fileName));
            }
        }
    }
    
    	
    
    private static void fillTripsByRoutesMap(String[] line, Map<String, List<String>> tripsByRoutes) {
    	String routeId = line[IDFM_ROUTE_ID];
    	String tripId = line[IDFM_TRIP_ID];
        // Ajouter routeId en tant que clé si pas présente dans la map
        tripsByRoutes.putIfAbsent(routeId, new ArrayList<>());

        // Ajouter tripId dans la liste associée à cette clé
        tripsByRoutes.get(routeId).add(tripId);
    }
    
    /**permet de trier les couples (depart, stop_depart) par heure de depart croissantes
     * pas de problème avec minuit car dans le fichier stop_times c'est écrit "24:00:00"
     * @param itinerariesByTrip
     * @param newEntry
     */
    private static void insertSorted(List<Pair<String, String>> itinerariesByTrip, Pair<String, String> newEntry) {
        int index = 0;
        while (index < itinerariesByTrip.size() && itinerariesByTrip.get(index).getLeft().compareTo(newEntry.getLeft()) < 0) {
            index++;
        }
        itinerariesByTrip.add(index, newEntry);
    }
    
    private static void addItineraryToATrip(String[] line, Map<String, List<Pair<String, String>>> itinerariesByTrip) {
    	String tripId = line[SCHEDULE_TRIP_ID];
    	String timeOfDeparture = line[SCHEDULE_DEPARTURES_TIME];
    	String stopId = line[SCHEDULE_STOPS_ID];
    	itinerariesByTrip.putIfAbsent(tripId, new ArrayList<>());
    	Pair<String, String> timeAndStop = Pair.of(timeOfDeparture, stopId);
    	insertSorted(itinerariesByTrip.get(tripId), timeAndStop);
    	
    }

    
    private static void cleanTraces(Map<String, TraceEntry> traces) {
        Set<String> toRemove = new HashSet<>();
        for (Entry<String, TraceEntry> traceEntry : traces.entrySet()) {
            TraceEntry trace = traceEntry.getValue();
            if (!cleanLine(trace.getPaths())) {
                LOGGER.severe(() -> MessageFormat.format(
                        "Missing stop for line {0}. Line will be removed", trace.lname));
                toRemove.add(traceEntry.getKey());
            }
        }

        for (String string : toRemove) {
            traces.remove(string);
        }
    }

    /** @param path */
    private static boolean cleanLine(List<List<StopEntry>> stops) {
        for (List<StopEntry> path : stops) {
            for (int i = 0; i < path.size(); i++) {
                StopEntry stop = path.get(i);
                if (!(stop instanceof UnidentifiedStopEntry)) {
                    continue;
                }
                UnidentifiedStopEntry unidentified = (UnidentifiedStopEntry) stop;
                StopEntry stopResolution = unidentified.resolve();
                if (stopResolution == null) {
                    return false;
                }
                path.set(i, stopResolution);
            }
        }
        return true;
    }

    /**crée un stopEntry basé sur des vraies infos sur les stations
     * va être appelé sur chaque ligne du csv donc pour chaque StopEntry existant ça va regarder si il est un candidat possible 
	 * pour des stations unidentifiedStopEntry de la TraceEntry concernée
     * @param line
     * @param traces
     * @param stops
     */
    private static void addStop(String[] line, Map<String, TraceEntry> traces,
            List<StopEntry> stops) {
        StopEntry entry = new StopEntry(line[IDFM_STOPS_NAME_INDEX], line[IDFM_STOPS_ID_INDEX],
                Double.parseDouble(line[IDFM_STOPS_LON_INDEX]),
                Double.parseDouble(line[IDFM_STOPS_LAT_INDEX]));
        String rid = line[IDFM_STOPS_RID_INDEX];
        traces.computeIfPresent(rid,
                (String k, TraceEntry trace) -> addCandidate(trace, entry));
        stops.add(entry);
    }

    /**ajoute tous les unidentifiedStopEntry à la traceEntry en se basant sur le jsonpath de la ligne ds le csv
     * 
     * @param line
     * @param traces
     */
    private static void addLine(String[] line, Map<String, TraceEntry> traces) {
        TraceEntry entry = new TraceEntry(line[IDFM_TRACE_SNAME_INDEX]);
        List<List<StopEntry>> buildPaths = buildPaths(line[IDFM_TRACE_SHAPE_INDEX]);
        entry.getPaths().addAll(buildPaths);
        if (buildPaths.isEmpty()) {
            LOGGER.severe(() -> MessageFormat.format(
                    "Line {0} has no provided itinerary and was ignored", entry.lname));
        } else {
            traces.put(line[IDFM_TRACE_ID_INDEX], entry);
        }
    }

    private static TraceEntry addCandidate(TraceEntry trace, StopEntry entry) {
        for (List<StopEntry> path : trace.getPaths()) {
            for (StopEntry stopEntry : path) {
                if (stopEntry instanceof UnidentifiedStopEntry unidentified
                        && GPS.distance(entry.latitude, entry.longitude,
                                stopEntry.latitude,
                                stopEntry.longitude) < QUARTER_KILOMETER) {
                    unidentified.addCandidate(entry);
                }
            }
        }
        return trace;
    }

    private static List<List<StopEntry>> buildPaths(String pathsJSON) {
        List<List<StopEntry>> all = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(pathsJSON);
            JSONArray paths = json.getJSONArray("coordinates");
            for (int i = 0; i < paths.length(); i++) {
                JSONArray path = paths.getJSONArray(i);
                List<StopEntry> stopsPath = new ArrayList<>();
                for (int j = 0; j < path.length(); j++) {
                    JSONArray coordinates = path.getJSONArray(j);

                    StopEntry entry = new UnidentifiedStopEntry(coordinates.getDouble(0),
                            coordinates.getDouble(1));

                    stopsPath.add(entry);
                }

                all.add(stopsPath);
            }
        } catch (JSONException e) {
            // Ignoring invalid element!
            LOGGER.log(Level.FINE, e,
                    () -> MessageFormat.format("Invalid json element {0}", pathsJSON)); //$NON-NLS-1$
        }
        return all;
    }
}
