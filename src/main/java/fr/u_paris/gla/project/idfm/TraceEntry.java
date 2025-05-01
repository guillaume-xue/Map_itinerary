/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;



/**
 * Représente une ligne de transport intermédiaire utilisée pour la génération
 * des fichiers clients (CSV) contenant les informations d’horaires et d’arrêts.
 */
public final class TraceEntry {
    private final String lineName;
    private final String lineId;
    private final String typeLine;
    private final String colorLine;
    private List<StopEntry> lineStops = new ArrayList<>();
    
    /** 
     * Map associant un numéro de bifurcation (sous-ligne) à une séquence d’arrêts.
     * Chaque entrée correspond à un chemin alternatif emprunté par certains véhicules de la ligne.
     */
    private Map<String, List<StopEntry>> paths = new HashMap<>();
    
    /**
     * Map associant un nom d’arrêt terminus à une liste de paires (heure de départ, sous-ligne).
     * Les listes sont triées par heure de départ croissante à l’insertion.
     */
    private Map<String,List<Pair<String,String>>> departures = new HashMap<>();

    /**
     * Ensemble de paires d’arrêts consécutifs présents dans les chemins définis par {@code paths}.
     * Utilisé pour reconstituer la continuité des trajets.
     */
    private Set<Pair<StopEntry, StopEntry>> adjacentsStops = new HashSet<>();
    
    /**
     * Crée une nouvelle ligne de transport.
     *
     * @param lineName  le nom de la ligne
     * @param lineId    l'identifiant unique de la ligne
     * @param typeLine  le type de transport (métro, bus, etc.)
     * @param colorLine la couleur représentative de la ligne
     */
    public TraceEntry(String lineName, String lineId, String typeLine, String colorLine) {
        super();
        this.lineName = lineName;
        this.lineId = lineId;
        this.typeLine = typeLine;
        this.colorLine = colorLine;
    }

    public String getLineId() {
    	return lineId;
    }
    
    public String getLineName() {
    	return lineName;
    }
   
    public String getLineType() {
    	return typeLine;
    }
    
    public String getLineColor() {
    	return colorLine;
    }
    
    @Override
    public String toString() {
    	return MessageFormat.format("{0}, {1}, {2}, {3}", this.lineName, this.lineId, //$NON-NLS-1$
                this.typeLine, this.colorLine);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TraceEntry other = (TraceEntry) obj;
        return Objects.equals(lineId, other.lineId) && 
        		Objects.equals(lineName, other.lineName) && 
        		Objects.equals(typeLine, other.typeLine) && 
        		Objects.equals(colorLine, other.colorLine);
    }
    
    
    public List<StopEntry> getAllStops() {
    	return Collections.unmodifiableList(lineStops);
    }
    
    
    public void addStop(StopEntry stop) {
    	if (!lineStops.contains(stop)) {
    		lineStops.add(stop);
    	}
    }
    
    public void addAllDepartures(Map<String,List<Pair<String,String>>> departures) {
    	this.departures = departures;
    }
    
    public Map<String,List<Pair<String,String>>> getDepartures() {
    	return Collections.unmodifiableMap(departures);
    }
    
    public void addAllPaths(Map<String, List<StopEntry>> paths) {
    	this.paths = paths;
    	definePairsOfAdjacentStops();
    }
    
    public Map<String, List<StopEntry>> getPaths() {
    	return Collections.unmodifiableMap(paths);
    }
    
    /**
     * Calcule et stocke toutes les paires d’arrêts consécutifs à partir des chemins définis.
     */
    public void definePairsOfAdjacentStops() {
    	if (!this.paths.isEmpty()) {
    		Set<Pair<StopEntry, StopEntry>> adjacentsStops = new HashSet<>();

            for (List<StopEntry> stopList : this.paths.values()) {
                for (int i = 0; i < stopList.size() - 1; i++) {
                    StopEntry first = stopList.get(i);
                    StopEntry second = stopList.get(i + 1);
                    Pair<StopEntry, StopEntry> adjStops = Pair.of(first, second);
                    adjacentsStops.add(adjStops); // HashSet empêche les doublons
                }
            }
            this.adjacentsStops = adjacentsStops;
    	}
    }
    
    public Set<Pair<StopEntry, StopEntry>> getStopPairs() {
    	return Collections.unmodifiableSet(adjacentsStops);
    }
}
