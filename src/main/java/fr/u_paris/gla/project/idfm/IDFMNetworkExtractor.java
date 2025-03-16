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
    private static final String SCHEDULE_FILE_NAME = "fr/u_paris/gla/project/stop_times.txt";
    
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
    private static final int SCHEDULE_STOPS_ID = 5;
    private static final int SCHEDULE_DEPARTURES_TIME = 2;
    
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
    	if (args.length != 2) {
            LOGGER.severe("Invalid command line. Needs target file and target repertory.");
            return;
        }
    	Map<String, TraceEntry> traces = parseMapData(new String[] {args[0]});
    	
    	
    	if (traces != null) {
    		File directory = new File(args[1]);
            parseScheduleData(directory, traces);
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
    
    
    /** parses schedule data from added URL (found in the idfm database, related to departures schedules) 
     * and makes csv files for each terminus of each line
     * puts those files in given directory
     * 
     * @param directory the destination directory
     * @param traces the map filled by other parsing function, contains all the information needed to know which stop is a terminus
     */
    private static void parseScheduleData(File directory, Map<String, TraceEntry> traces) {
    	
    	
        // Map où chaque (lname, StopEntry) est associé à une liste d'horaires
        Map<Pair<String, StopEntry>, NavigableSet<String>> scheduleByStop = new HashMap<>();
        findTerminusesByLine(scheduleByStop, traces);
       
        try {
            CSVTools.readCSVFromFile(SCHEDULE_FILE_NAME,
                    (String[] line) -> addDepart(line, scheduleByStop));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while reading the schedules", e);
        }
    	
        //ici scheduleByStop devrait être remplie de toutes les informations horaires dont on a besoin
        //remplir les csv cibles avec les données
    	
        //System.out.println("Nombre de stops dans scheduleByStop : " + scheduleByStop.size());
        fillAllScheduleFiles(scheduleByStop, directory);
    }
        
    private static void fillAllScheduleFiles(Map<Pair<String, StopEntry>, NavigableSet<String>> scheduleByStop, File directory) {
        for (Map.Entry<Pair<String, StopEntry>, NavigableSet<String>> entry : scheduleByStop.entrySet()) {
            Pair<String, StopEntry> key = entry.getKey();
            NavigableSet<String> times = entry.getValue();

            // Générer le nom du fichier
            String lineName = key.getLeft();
            String stopName = key.getRight().lname; 
            String sanitizedStopName = stopName.replaceAll("[\\\\/]", "_"); //pour eviter les erreurs lors de cré
            String fileName = directory + "/" + lineName + "_" + sanitizedStopName + ".csv";
            //System.out.println(fileName);
            File file = new File(fileName);
            
            //ici il faut clean la map pour supprimer toutes les clés dont la navigableSet est vide (oui il y en a)
            
            //remplir chaque fichier selon ScheduleFormat
            CSVStreamProviderForSchedules provider = new CSVStreamProviderForSchedules(times.iterator(), lineName, stopName);
            
            try {
            	CSVTools.writeCSVToFile(fileName, Stream.generate(provider).takeWhile(Objects::nonNull));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e,
                        () -> MessageFormat.format("Could not write in file {0}", fileName));
            }
        }
    }
    	
    	
    	
    /** fills the keys of a map containing every pair of line-terminus
     * the values of the map are lists of schedules for each pair
     * they will be added when stop_times file will be read
     * @param scheduleByStop
     * @param traces
     */
    private static void findTerminusesByLine(Map<Pair<String, StopEntry>, NavigableSet<String>> scheduleByStop, Map<String, TraceEntry> traces) {
    	for (TraceEntry trace : traces.values()) {
            for (List<StopEntry> path : trace.getPaths()) {
                if (!path.isEmpty()) {
                    StopEntry firstStop = path.get(0);
                    Pair<String, StopEntry> key = Pair.of(trace.lname, firstStop);
                    scheduleByStop.putIfAbsent(key, new TreeSet<>());
                }
            }
        }
    }

    //vérifier que l'heure est ajoutée est une vraie heure 
    //pour l'instant on a des 25h au lieu de 01h
    private static void addDepart(String[] line, Map<Pair<String, StopEntry>, NavigableSet<String>> scheduleByStop) {
    	String stopId = line[SCHEDULE_STOPS_ID];
    	Optional<Pair<String, StopEntry>> stopKey = findStopKey(stopId, scheduleByStop);
    	if (stopKey.isPresent()) {
    		String timeOfDeparture = line[SCHEDULE_DEPARTURES_TIME].substring(0,5);
    		//System.out.println(timeOfDeparture);
    		scheduleByStop.get(stopKey.get()).add(timeOfDeparture); //ajout trié
    	}
    	
    }
    
    private static Optional<Pair<String, StopEntry>> findStopKey(String stopId, Map<Pair<String, StopEntry>, NavigableSet<String>> scheduleByStop) {
        return scheduleByStop.keySet().stream()
            .filter(pair -> pair.getRight().getStopId().equals(stopId)).findFirst(); 
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
