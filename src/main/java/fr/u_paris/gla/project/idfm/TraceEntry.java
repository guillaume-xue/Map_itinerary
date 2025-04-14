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


//import org.apache.commons.lang3.tuple.Triple;

/** Representation of a transport line
 * 
 * @author Emmanuel Bigeon */
public final class TraceEntry {
    private final String lineName;
    private final String lineId;
    private final String typeLine;
    private final String colorLine;
    private List<StopEntry> lineStops = new ArrayList<>();
    
    // map qui lie un num de bifurcation à une sequence de stopEntry
    private Map<String, List<StopEntry>> paths = new HashMap<>();
    
    // exemple : [("05:55","2", "nomStationDep");("5:58","1", "autrenomStationDep");...] où on a des 
    //paires heureDepart-numSousLigne, et c'est trié à l'ajout par heure de départ
    //private List<Triple<String,String, String>> departures;
    
    // pour une clé nomStation on a une liste de tuple [("05:55","1");...] bien ordonnée en focntion des heures
    private Map<String,List<Pair<String,String>>> departures = new HashMap<>();

    //remplie quand paths est ajouté
    private Set<Pair<StopEntry, StopEntry>> adjacentsStops = new HashSet<>();
    
    /** Create a transport line.
     * 
     * @param lname the name of the line */
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
    
    //inmodifiable depuis l'extérieur, on en a besoin que pour avoir le nombre de quais ou bien le nom des stations
    public List<StopEntry> getAllStops() {
    	return Collections.unmodifiableList(lineStops);
    }
    
    //ajoute un quai que si il y est pas déjà
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
