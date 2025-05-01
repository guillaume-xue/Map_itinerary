package fr.u_paris.gla.project.idfm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Supplier;

import fr.u_paris.gla.project.io.JunctionsFormat;

/**
 * Fournisseur de lignes CSV représentant les bifurcations d'une ligne de transport.
 * <p>
 * Cette classe permet d'itérer sur une structure contenant les différentes variantes (bifurcations)
 * d'une ligne, et de générer une ligne formatée à chaque appel, selon le format requis pour les fichiers CSV.
 * </p>
 */
public class CSVStreamProviderForJunctions implements Supplier<String[]> {
    private final Iterator<Map.Entry<String, TraceEntry>> tracesIterator; // Premier itérateur
    private Iterator<Map.Entry<String, List<StopEntry>>> pathsIterator = Collections.emptyIterator(); // Deuxième itérateur

    private String currentLineID;

    private final String[] line = new String[JunctionsFormat.NUMBER_COLUMNS]; // 3 colonnes : idLigne, numBifurcation, stops


    /**
     * Construit un fournisseur à partir d'un ensemble de traces,
     * chaque trace contenant une ligne et ses bifurcations associées.
     *
     * @param traces une map dont la clé est un idLine
     *               et la valeur une {@code TraceEntry} représentant une ligne de transport
     */
    public CSVStreamProviderForJunctions(Map<String, TraceEntry> traces) {
        this.tracesIterator = traces.entrySet().iterator();
        advanceToNextValidTrace(); // Initialise le premier TraceEntry et son itérateur de paths
    }

    /**
     * Renvoie une ligne formatée représentant une bifurcation d'une ligne, ou {@code null} s'il n'y a plus rien à traiter.
     * <p>
     * Chaque ligne contient : identifiant de ligne, numéro de bifurcation, et liste ordonnée des noms d’arrêts (séparés par {@code ;}).
     * </p>
     *
     * @return un tableau de chaînes représentant une ligne du fichier CSV, ou {@code null} si tout a été parcouru
     */
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
     * Avance à la prochaine ligne ({@code TraceEntry}) contenant au moins une bifurcation.
     *
     * @return {@code true} si un nouveau {@code TraceEntry} a été trouvé, {@code false} si tous ont été parcourus
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


