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
import java.util.TreeMap;
import java.util.Optional;
import java.time.LocalTime;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import java.util.Objects;
import java.util.Iterator;

/** Code of an extractor for the data from IDF mobilite.
 * 
 */
public class IDFMNetworkExtractor {

    /** The logger for information on the process */
    private static final Logger LOGGER = Logger
            .getLogger(IDFMNetworkExtractor.class.getName());

    // IDF mobilite API URLs
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
   
    //IDF mobilite provided schedule file
    private static final String ZIP_PATH = "fr/u_paris/gla/project/idfm_data.zip";
    private static final String SCHEDULE_FILE_NAME = "idfm_data/stop_times.txt";
    private static final String TRIPS_FILE_NAME = "idfm_data/trips.txt";
    
    //ID;Short Name;Long Name;Route Type;Color;Route URL;Shape;id_ilico;OperatorName;NetworkName;URL;long_name_first;geo_point_2d

    // IDF mobilite csv formats 
    private static final int IDFM_TRACE_ID_INDEX    = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_TYPE_INDEX = 3;
    private static final int IDFM_TRACE_COLOR_INDEX = 4;

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

    
    /** Main entry point for the extractor of IDF mobilite data into a network as
     * defined by this application.
     * 
     * @param destination The destination file */
    public static void parse(String[] args) {
    	// map <route_id, traceEntry>
        Map<String, TraceEntry> traces = new TreeMap<>();
        fillMapTraceEntries(traces);
        cleanTraces(traces);
        
        // map <route_id, list<trip_id>>
        Map<String, List<String>> tripsByRoutes = createMapRoutesAndTripsData();
        // map <trip_id, list<pair<horaire,stopId>>>
        Map<String, List<Pair<String, String>>> itinerariesByTrip = findItineraryForEachTrip();
        
        addPathsAndDeparturesToTraces(traces, tripsByRoutes, itinerariesByTrip);
        
        System.out.println("Le nombre de lignes avant création des fichiers est " + traces.size() +"\n");
        printNumberOfStops(traces);
        
        File directory = new File(args[2]);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        fillScheduleFiles(traces, directory);
        fillJunctionsFile(args[1], traces);
        fillMapDataFile(args[0], traces);
        
    }
    
    
    private static void printNumberOfStops(Map<String, TraceEntry> traces) {
    	int cptrStops = 0;
    	for (Map.Entry<String, TraceEntry> entry : traces.entrySet()) {
    		TraceEntry trace = entry.getValue();
    		for (StopEntry stop : trace.getAllStops()) {
    			cptrStops +=1;
    		}
    	}
    	System.out.println("Le nombre de quais avant création des fichiers est "+cptrStops +"\n");
    }
    
    private static void fillMapDataFile(String file, Map<String, TraceEntry> traces) {
    	CSVStreamProviderForMapData provider = new CSVStreamProviderForMapData(traces);

    	try {
        	CSVTools.writeCSVToFile(file, Stream.generate(provider).takeWhile(Objects::nonNull));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", file));
        }
    }
    
    //remplissage de la map traces à partir des URLs idfm
    private static void fillMapTraceEntries(Map<String, TraceEntry> traces) {
        try {
            CSVTools.readCSVFromURL(TRACE_FILE_URL,
                    (String[] line) -> addLine(line, traces));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the line paths", e);
        }

        try {
            CSVTools.readCSVFromURL(STOPS_FILE_URL,
                    (String[] line) -> addStop(line, traces));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the stops", e);
        }
    }
    
   
    //finit de compléter la map traces en ajoutant aux TraceEntry les liste departures et map paths
    private static void addPathsAndDeparturesToTraces(Map<String, TraceEntry> traces, 
    		Map<String, List<String>> tripsByRoutes, 
    		Map<String, List<Pair<String, String>>> itinerariesByTrip) {
    	
    	for (Map.Entry<String, List<String>> routeEntry : tripsByRoutes.entrySet()) {
            String routeId = routeEntry.getKey();
            List<String> trips = routeEntry.getValue();
            
            //verif qu'on a bien les infos dans la map traces
            TraceEntry trace = traces.get(routeId);
            if (trace == null) {
                continue;
            }
            
            Map<String,List<Pair<String,String>>> departures = new HashMap<>();
            Map<String, List<StopEntry>> paths = new HashMap<>();
            
            fillDeparturesAndPathsForTrace(trace, trips, itinerariesByTrip, departures, paths);
            
            trace.addAllDepartures(departures);
            trace.addAllPaths(paths);
    	}
    }
   
    
    private static void fillDeparturesAndPathsForTrace(TraceEntry trace,
    		List<String> trips, 
    		Map<String, List<Pair<String, String>>> itinerariesByTrip,
    		Map<String,List<Pair<String,String>>> departures,
    		Map<String, List<StopEntry>> paths) {
    	
    	Map<List<String>, String> subLines = new HashMap<>(); 
    	for (String tripId : trips) {
        	// [("05:55","IDFM:22241");...]
        	List<Pair<String, String>> itinerary = itinerariesByTrip.get(tripId);
        	if (itinerary == null || itinerary.isEmpty()) {
                continue;
            }
        	
        	//on transforme la liste de tuples en une liste avec que les stopId
        	List<String> stopsIdSequence = new ArrayList<>();
        	for (Pair<String, String> pair : itinerary) {
                String stopId = pair.getRight();
                stopsIdSequence.add(stopId);
        	}
        	
        	String numSubLine;
        	
        	//compare correctement des listes de string (de stopId)
        	if(subLines.containsKey(stopsIdSequence)) {
        		numSubLine = subLines.get(stopsIdSequence);
        	}
        	else {
        		numSubLine = String.valueOf(subLines.size());
        		subLines.put(stopsIdSequence, numSubLine);
        	}
        	
        
            // Transformer stopsIdSequence en List<StopEntry>
            List<StopEntry> stopEntries = new ArrayList<>();
            for (String stopId : stopsIdSequence) {
                for (StopEntry stopEntry : trace.getAllStops()) { 
                    if (stopEntry.getStopId().equals(stopId)) { 
                        stopEntries.add(stopEntry);
                        break; 
                    }
                }
            }
            if (stopEntries.isEmpty()) {
            	continue;
            }
            
            paths.put(numSubLine, stopEntries);
        	
        	String terminusName = stopEntries.get(0).getStopName();
        	String departureTime = itinerary.get(0).getLeft();
        	Pair<String, String> departure = Pair.of(departureTime, numSubLine);
        	if (departures.containsKey(terminusName)) {
        		insertSorted(departures.get(terminusName), departure);
        	}
        	else {
        		departures.put(terminusName, new ArrayList<>(List.of(departure)));
        	}
        }
    }
    
    private static void fillJunctionsFile(String junctionsFile, Map<String, TraceEntry> traces) {
        CSVStreamProviderForJunctions provider = new CSVStreamProviderForJunctions(traces);
        
        try {
        	CSVTools.writeCSVToFile(junctionsFile, Stream.generate(provider).takeWhile(Objects::nonNull));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", junctionsFile));
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
            CSVTools.readCSVFromZip(ZIP_PATH, TRIPS_FILE_NAME,
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
            CSVTools.readCSVFromZip(ZIP_PATH, SCHEDULE_FILE_NAME,
                    (String[] line) -> addItineraryToATrip(line, itinerariesByTrip));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the trips", e);
        }
        return itinerariesByTrip;
    }
   
    
    private static void fillScheduleFiles(Map<String, TraceEntry> traces, File directory) {
    	for (TraceEntry trace : traces.values()) {
    		String lineId = trace.getLineId();
    		String lineName = trace.getLineName();
    		for (Map.Entry<String,List<Pair<String,String>>> dep : trace.getDepartures().entrySet()) {
    			String stopName = dep.getKey();
    			List<Pair<String, String>> timesAndJunctions = dep.getValue();
    			String sanitizedStopName = stopName.replaceAll("[\\\\/:*?\"<>|]", ""); //pour eviter les erreurs liés aux / et \ dans les noms des stations
    			String sanitizedLineId = lineId.replaceAll("[\\\\/:*?\"<>|]", "");
    			String fileName = directory.toString() + "/" + lineName + "_" + sanitizedLineId + "_" + sanitizedStopName + ".csv";
    			//System.out.println("nom fichier :" + fileName);
                File file = new File(fileName);
                
                CSVStreamProviderForSchedules provider = new CSVStreamProviderForSchedules(timesAndJunctions, sanitizedLineId, stopName);
                
                try {
                	CSVTools.writeCSVToFile(fileName, Stream.generate(provider).takeWhile(Objects::nonNull));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e,
                            () -> MessageFormat.format("Could not write in file {0}", fileName));
                }
    		}
    	}
    }
    
    	
    
    private static void fillTripsByRoutesMap(String[] line, Map<String, List<String>> tripsByRoutes) {
    	String routeId = line[IDFM_ROUTE_ID].replaceAll(":", "");
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

    
    //supprime toutes les traces dont on a pas trouvé de stops leur appartenant
    private static void cleanTraces(Map<String, TraceEntry> traces) {
    	Set<String> toRemove = new HashSet<>();
    	for (Entry<String, TraceEntry> traceEntry : traces.entrySet()) {
    		TraceEntry trace = traceEntry.getValue();
    		if (trace.getAllStops().isEmpty()) {
    			toRemove.add(traceEntry.getKey());
    		}
    	}
    	for (String string : toRemove) {
    		System.out.println(string);
            traces.remove(string);
        }
    	System.out.println("Nombre de lignes supprimées car ne possèdent aucun stops : " + toRemove.size() + "\n");
    }

    
    //construit les listes désordonnées de quais appartenant à chacune des lignes de transport
    private static void addStop(String[] line, Map<String, TraceEntry> traces) {
    	String routeId = line[IDFM_STOPS_RID_INDEX].replaceAll(":", "");
    	
    	if (traces.containsKey(routeId)) {
    		StopEntry stop = new StopEntry(line[IDFM_STOPS_NAME_INDEX], line[IDFM_STOPS_ID_INDEX],
                Double.parseDouble(line[IDFM_STOPS_LON_INDEX]),
                Double.parseDouble(line[IDFM_STOPS_LAT_INDEX]));
    		traces.get(routeId).addStop(stop);
    	}
    }

    //remettre en private
    public static void addLine(String[] line, Map<String, TraceEntry> traces) {
    	String id = line[IDFM_TRACE_ID_INDEX];
    	String sanitizedLineId = id.replaceAll(":", "");
        TraceEntry newEntry = new TraceEntry(line[IDFM_TRACE_SNAME_INDEX], sanitizedLineId, line[IDFM_TRACE_TYPE_INDEX], line[IDFM_TRACE_COLOR_INDEX]);
        
        if (traces.containsKey(id)) {
            System.out.println("Attention : Un doublon a été détecté pour l'ID " + sanitizedLineId);
            System.out.println("Ancienne entrée : " + traces.get(sanitizedLineId));
            System.out.println("Nouvelle entrée : " + newEntry);
        }
        
        traces.put(sanitizedLineId, newEntry);
    }

}
