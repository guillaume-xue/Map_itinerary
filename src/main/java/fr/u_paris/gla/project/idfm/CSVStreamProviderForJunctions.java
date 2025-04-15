package fr.u_paris.gla.project.idfm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Supplier;

import fr.u_paris.gla.project.io.JunctionsFormat;


public class CSVStreamProviderForJunctions implements Supplier<String[]> {
    private final Iterator<Map.Entry<String, TraceEntry>> tracesIterator; // Premier itérateur
    private Iterator<Map.Entry<String, List<StopEntry>>> pathsIterator = Collections.emptyIterator(); // Deuxième itérateur

    private String currentLineID;
    private String currentLineType;

    private final String[] line = new String[JunctionsFormat.NUMBER_COLUMNS]; // 4 colonnes : nomLigne, typeLigne, numBifurcation, stops

    public CSVStreamProviderForJunctions(Map<String, TraceEntry> traces) {
        this.tracesIterator = traces.entrySet().iterator();
        advanceToNextValidTrace(); // Initialise le premier TraceEntry et son itérateur de paths
    }

    @Override
    public String[] get() {
        while (true) {
            if (pathsIterator.hasNext()) {
                // On récupère une bifurcation (numéro et liste de stops)
                Map.Entry<String, List<StopEntry>> entry = pathsIterator.next();
                String bifurcationNumber = entry.getKey();
                List<StopEntry> stops = entry.getValue();

                // Transforme stops en une liste de stopName
                String stopsString = stops.stream()
                        .map(StopEntry::getStopName)
                        .collect(Collectors.joining(";"));

                line[JunctionsFormat.LINE_ID_INDEX] = currentLineID;
                line[JunctionsFormat.VARIANT_INDEX] = bifurcationNumber;
                line[JunctionsFormat.LIST_INDEX] = "[" + stopsString + "]";

                return line;
            } else if (!advanceToNextValidTrace()) {
                return null; // Plus d'éléments à traiter
            }
        }
    }

    /**
     * Passe au prochain `TraceEntry` et initialise son itérateur `pathsIterator`.
     * @return `true` si un nouveau `TraceEntry` a été trouvé, `false` si tous sont épuisés.
     */
    private boolean advanceToNextValidTrace() {
        while (tracesIterator.hasNext()) {
            Map.Entry<String, TraceEntry> traceEntry = tracesIterator.next();
            currentLineID = traceEntry.getValue().getLineId();
            pathsIterator = traceEntry.getValue().getPaths().entrySet().iterator(); // Initialise le deuxième itérateur

            if (pathsIterator.hasNext()) {
                return true; 
            }
        }
        return false; 
    }
}


