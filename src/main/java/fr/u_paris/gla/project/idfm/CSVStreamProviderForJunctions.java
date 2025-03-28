package fr.u_paris.gla.project.idfm;

import java.util.*;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;

public class CSVStreamProviderForJunctions implements Supplier<String[]> {

    private final Iterator<Map.Entry<Pair<String, String>, List<String>>> iterator;
    private String[] line = new String[3]; // 3 colonnes : nomLigne, numBifurcation, stops

    public CSVStreamProviderForJunctions(Map<Pair<String, String>, List<String>> bifurcationStops) {
        this.iterator = bifurcationStops.entrySet().iterator();
    }

    @Override
    public String[] get() {
        if (iterator.hasNext()) {
            Map.Entry<Pair<String, String>, List<String>> entry = iterator.next();
            Pair<String, String> key = entry.getKey();
            List<String> stops = entry.getValue();

            String lineName = key.getLeft();       // Nom de la ligne
            String bifurcationNumber = key.getRight(); // Numéro de bifurcation sous forme de String
            String stopsString = String.join(",", stops); // Transformation de la liste en une chaîne de stops séparés par ";"

            line[0] = lineName;
            line[1] = bifurcationNumber;
            line[2] = "[" + stopsString + "]";

            return line;
        }
        return null;
    }
}
