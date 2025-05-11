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

/**
 * Extracteur de données pour le réseau de transport Île-de-France Mobilités (IDFM).
 * 
 * Cette classe permet de télécharger, parser et transformer des données publiques
 * de transport fournies par IDFM. Elle génère des fichiers CSV utilisés par d'autres
 * parties du projet, à partir de plusieurs sources de données :
 * <ul>
 *   <li>Les tracés des lignes de transport</li>
 *   <li>Les arrêts associés à ces lignes</li>
 *   <li>Les horaires et trajets (au format GTFS dans un fichier .zip)</li>
 * </ul>
 * 
 * Le processus d'extraction comprend plusieurs étapes :
 * <ol>
 *   <li>Lecture et nettoyage des données de traces</li>
 *   <li>Construction des correspondances entre routes, trips et arrêts</li>
 *   <li>Création de fichiers CSV pour l'affichage sur carte, les horaires et les correspondances</li>
 * </ol>
 * 
 * Pour lancer l'extraction, utiliser la méthode {@link #parse(String[])} en fournissant trois
 * chemins de fichiers en argument (dans l'ordre : fichier pour la carte, fichier de correspondances,
 * dossier de sortie pour les horaires).
 *  
 */
public class IDFMNetworkExtractor {

    /** Le logger pour les informations sur l'exécution du programme*/
    private static final Logger LOGGER = Logger
            .getLogger(IDFMNetworkExtractor.class.getName());

    /** URLs des fichiers CSV fournis par l'API IDF Mobilités */
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
   
    /** Chemins des fichiers horaires fournis par IDF Mobilités */
    private static final String ZIP_PATH = "fr/u_paris/gla/project/idfm_data.zip";
    private static final String SCHEDULE_FILE_NAME = "idfm_data/stop_times.txt";
    private static final String TRIPS_FILE_NAME = "idfm_data/trips.txt";
    

    /** Index des colonnes du fichier CSV des traces */
    private static final int IDFM_TRACE_ID_INDEX    = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_TYPE_INDEX = 3;
    private static final int IDFM_TRACE_COLOR_INDEX = 4;

    /** Index des colonnes du fichier CSV des arrêts */
    private static final int IDFM_STOPS_RID_INDEX  = 0;
    private static final int IDFM_STOPS_ID_INDEX = 2;
    private static final int IDFM_STOPS_NAME_INDEX = 3;
    private static final int IDFM_STOPS_LON_INDEX  = 4;
    private static final int IDFM_STOPS_LAT_INDEX  = 5;

    /** Index utiles du fichier stop_times.txt */
    private static final int SCHEDULE_TRIP_ID = 0;
    private static final int SCHEDULE_DEPARTURES_TIME = 2;
    private static final int SCHEDULE_STOPS_ID = 5;
    
    /** Index utiles du fichier trips.txt */
    private static final int IDFM_ROUTE_ID = 0;
    private static final int IDFM_TRIP_ID = 2;

    
    /**
     * Point d'entrée du programme. Remplit les fichiers clients à partir des
     * données des trajets, horaires et arrêts.
     *
     * @param args les chemins des fichiers à générer : carte, jonctions, horaires
     */
    public static void parse(String[] args) {
        System.out.println("Création des fichiers en cours...\n");

        Map<String, TraceEntry> traces = new TreeMap<>();
        fillMapTraceEntries(traces);
        cleanTraces(traces);
        
        Map<String, List<String>> tripsByRoutes = createMapRoutesAndTripsData();
        Map<String, List<Pair<String, String>>> itinerariesByTrip = findItineraryForEachTrip();
        
        addPathsAndDeparturesToTraces(traces, tripsByRoutes, itinerariesByTrip);
        
        //System.out.println("Le nombre de lignes avant création des fichiers est " + traces.size() +"\n");
        //printNumberOfStops(traces);
        
        File directory = new File(args[2]);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        fillScheduleFiles(traces, directory);
        fillJunctionsFile(args[1], traces);
        fillMapDataFile(args[0], traces);
        System.out.println("Fichiers clients créés avec succès.");
    }
    
    /**
     * Affiche le nombre total de quais contenus dans toutes les lignes de transport.
     *
     * @param traces la map des lignes de transport
     */
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
    
    /**
     * Remplit le fichier des données de carte à partir de la map des traces.
     *
     * @param file   chemin du fichier à créer
     * @param traces map des lignes de transport
     */
    private static void fillMapDataFile(String file, Map<String, TraceEntry> traces) {
    	CSVStreamProviderForMapData provider = new CSVStreamProviderForMapData(traces);

    	try {
        	CSVTools.writeCSVToFile(file, Stream.generate(provider).takeWhile(Objects::nonNull));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", file));
        }
    }
    
    /**
     * Remplit la map des lignes de transport à partir des fichiers CSV en ligne.
     *
     * @param traces la map à remplir
     */
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
    
   

    /**
     * Ajoute aux objets TraceEntry les départs et les chemins associés.
     *
     * @param traces            map des lignes
     * @param tripsByRoutes     map route_id → liste de trip_id
     * @param itinerariesByTrip map trip_id → liste de (horaire, arrêt)
     */
    private static void addPathsAndDeparturesToTraces(Map<String, TraceEntry> traces, 
    		Map<String, List<String>> tripsByRoutes, 
    		Map<String, List<Pair<String, String>>> itinerariesByTrip) {
    	
    	for (Map.Entry<String, List<String>> routeEntry : tripsByRoutes.entrySet()) {
            String routeId = routeEntry.getKey();
            List<String> trips = routeEntry.getValue();
            
            //verifie qu'on a bien les infos dans la map traces
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
   
    
    /**
     * Complète les départs et chemins d’un objet TraceEntry donné.
     *
     * @param trace             ligne concernée
     * @param trips             trips associés à cette ligne
     * @param itinerariesByTrip map des trips vers leurs itinéraires
     * @param departures        map à remplir : terminus → (heure, sous-ligne)
     * @param paths             map à remplir : sous-ligne → liste d'arrêts
     */
    private static void fillDeparturesAndPathsForTrace(TraceEntry trace,
    		List<String> trips, 
    		Map<String, List<Pair<String, String>>> itinerariesByTrip,
    		Map<String,List<Pair<String,String>>> departures,
    		Map<String, List<StopEntry>> paths) {
    	
    	Map<List<String>, String> subLines = new HashMap<>(); 
    	for (String tripId : trips) {
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
    

    /**
     * Remplit le fichier des jonctions à partir des lignes de transport.
     *
     * @param junctionsFile chemin du fichier de jonctions à écrire
     * @param traces         map des lignes
     */
    private static void fillJunctionsFile(String junctionsFile, Map<String, TraceEntry> traces) {
        CSVStreamProviderForJunctions provider = new CSVStreamProviderForJunctions(traces);
        
        try {
        	CSVTools.writeCSVToFile(junctionsFile, Stream.generate(provider).takeWhile(Objects::nonNull));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Could not write in file {0}", junctionsFile));
        }
    }
    
    /**
     * Construit une map associant chaque route à tous ses trips en se 
     * basant sur le contenu du fichier fourni par IDFM.
     *
     * @return map de route_id → liste de trip_id
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
    
    /**
     * Construit une map associant chaque trip à son itinéraire détaillé.
     *
     * @return map de trip_id → liste de (horaire, stop_id)
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
   
    /**
     * Remplit les fichiers d’horaires pour chaque ligne.
     * Un fichier est généré par couple Idligne-terminus et contient les départs selon les quais terminus.
     *
     * @param traces    la map des lignes contenant toutes les informations à écrire
     * @param directory le répertoire dans lequel créer les fichiers
     */

    private static void fillScheduleFiles(Map<String, TraceEntry> traces, File directory) {
    	for (TraceEntry trace : traces.values()) {
    		String lineId = trace.getLineId();
    		String lineName = trace.getLineName();
    		for (Map.Entry<String,List<Pair<String,String>>> dep : trace.getDepartures().entrySet()) {
    			String stopName = dep.getKey();
    			List<Pair<String, String>> timesAndJunctions = dep.getValue();
    			String sanitizedStopName = stopName.replaceAll("[\\\\/:*?\"<>|]", ""); //pour eviter les erreurs liées aux / et \ dans les noms des stations
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
    
    	
    /**
     * Complète la map des trajets par ligne à partir d'une ligne du fichier trips.txt.
     *
     * @param line           la ligne extraite du fichier CSV
     * @param tripsByRoutes  la map des routes vers leurs trips à compléter
     */
    private static void fillTripsByRoutesMap(String[] line, Map<String, List<String>> tripsByRoutes) {
    	String routeId = line[IDFM_ROUTE_ID].replaceAll(":", "");
    	String tripId = line[IDFM_TRIP_ID];
        // Ajouter routeId en tant que clé si pas présente dans la map
        tripsByRoutes.putIfAbsent(routeId, new ArrayList<>());

        // Ajouter tripId dans la liste associée à cette clé
        tripsByRoutes.get(routeId).add(tripId);
    }
    
    /**
     * Trie et insère un horaire de départ dans la liste correspondante,
     * de manière à conserver l’ordre chronologique.
     *
     * @param itinerariesByTrip       la liste des horaires de départ
     * @param newEntry  le départ à insérer, sous forme de paire (heure, numéro de sous-ligne)
     */
    private static void insertSorted(List<Pair<String, String>> itinerariesByTrip, Pair<String, String> newEntry) {
        int index = 0;
        while (index < itinerariesByTrip.size() && itinerariesByTrip.get(index).getLeft().compareTo(newEntry.getLeft()) < 0) {
            index++;
        }
        itinerariesByTrip.add(index, newEntry);
    }
    
    /**
     * Ajoute à la map un itinéraire (séquence d’arrêts et d’horaires) pour un trip donné.
     *
     * @param line               la ligne du fichier stop_times.txt représentant un arrêt d’un trip
     * @param itinerariesByTrip  la map des trips vers leur liste (heure, id arrêt) à compléter
     */
    private static void addItineraryToATrip(String[] line, Map<String, List<Pair<String, String>>> itinerariesByTrip) {
    	String tripId = line[SCHEDULE_TRIP_ID];
    	String timeOfDeparture = line[SCHEDULE_DEPARTURES_TIME];
    	String stopId = line[SCHEDULE_STOPS_ID];
    	itinerariesByTrip.putIfAbsent(tripId, new ArrayList<>());
    	Pair<String, String> timeAndStop = Pair.of(timeOfDeparture, stopId);
    	insertSorted(itinerariesByTrip.get(tripId), timeAndStop);
    	
    }

    
    /**
     * Supprime toutes les lignes de transport vides.
     * 
     * @param traces la map des lignes à nettoyer
     */
    private static void cleanTraces(Map<String, TraceEntry> traces) {
    	Set<String> toRemove = new HashSet<>();
    	for (Entry<String, TraceEntry> traceEntry : traces.entrySet()) {
    		TraceEntry trace = traceEntry.getValue();
    		if (trace.getAllStops().isEmpty()) {
    			toRemove.add(traceEntry.getKey());
    		}
    	}
    	for (String string : toRemove) {
    		//System.out.println(string);
            traces.remove(string);
        }
    	//System.out.println("Nombre de lignes supprimées car ne possèdent aucun stops : " + toRemove.size() + "\n");
    }

    
    /**
     * Ajoute un arrêt (depuis le fichier des arrêts) à la ligne correspondante dans la map.
     * L’arrêt est associé à son identifiant de ligne, si existant dans la map.
     *
     * @param line   la ligne du fichier CSV contenant les infos de l’arrêt
     * @param traces la map des lignes à compléter
     */
    private static void addStop(String[] line, Map<String, TraceEntry> traces) {
    	String routeId = line[IDFM_STOPS_RID_INDEX].replaceAll(":", "");
    	
    	if (traces.containsKey(routeId)) {
    		StopEntry stop = new StopEntry(line[IDFM_STOPS_NAME_INDEX], line[IDFM_STOPS_ID_INDEX],
                Double.parseDouble(line[IDFM_STOPS_LON_INDEX]),
                Double.parseDouble(line[IDFM_STOPS_LAT_INDEX]));
    		traces.get(routeId).addStop(stop);
    	}
    }

    /**
     * Ajoute une ligne (depuis le fichier des tracés) à la map des lignes.
     * Crée une nouvelle entrée TraceEntry si l’identifiant de ligne n’est pas encore présent.
     *
     * @param line   la ligne du fichier CSV contenant les infos sur la ligne de transport
     * @param traces la map des lignes à compléter
     */
    private static void addLine(String[] line, Map<String, TraceEntry> traces) {
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
