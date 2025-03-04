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

    //ID;Short Name;Long Name;Route Type;Color;Route URL;Shape;id_ilico;OperatorName;NetworkName;URL;long_name_first;geo_point_2d

    // IDF mobilite csv formats
    private static final int IDFM_TRACE_ID_INDEX    = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_SHAPE_INDEX = 6;

    //route_id;route_long_name;stop_id;stop_name;stop_lon;stop_lat;OperatorName;shortName;mode;Pointgeo;Nom_commune;Code_insee

    private static final int IDFM_STOPS_RID_INDEX  = 0;
    private static final int IDFM_STOPS_NAME_INDEX = 3;
    private static final int IDFM_STOPS_LON_INDEX  = 4;
    private static final int IDFM_STOPS_LAT_INDEX  = 5;

    // Magically chosen values
    /** A number of stops on each line */
    private static final int GUESS_STOPS_BY_LINE = 5;

    // Well named constants
    private static final double QUARTER_KILOMETER = .25;

    /** Main entry point for the extractor of IDF mobilite data into a network as
     * defined by this application.
     * 
     * @param args the arguments (expected one for the destination file) */
    public static void parse(String[] args) {

        if (args.length != 1) {
            LOGGER.severe("Invalid command line. Missing target file.");
            return;
        }

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
        StopEntry entry = new StopEntry(line[IDFM_STOPS_NAME_INDEX],
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
