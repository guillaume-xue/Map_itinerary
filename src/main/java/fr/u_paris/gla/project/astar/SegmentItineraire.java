package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.*;
import java.time.LocalTime;
import java.util.ArrayList;

public class SegmentItineraire {
    private Subline subline;
    private ArrayList<Stop> stops;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;

    public SegmentItineraire(Subline subline, ArrayList<Stop> stops, LocalTime heureDepart, LocalTime heureArrivee) {
        this.subline = subline;
        this.stops = stops;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
    }
    
    @Override
    public String toString() {
        StringBuilder stopsString = new StringBuilder();
        for (Stop stop : stops) {
            stopsString.append(stop.getNameOfAssociatedStation()).append(" -> ");
        }
        if (stopsString.length() > 0) {
            stopsString.setLength(stopsString.length() - 4);  // Enlève le dernier " -> "
        }

        String sublineName = (subline != null && subline.getAssociatedLine() != null) 
            ? subline.getAssociatedLine().getName()
            : "Aucune sous-ligne";

        return "Sous-ligne: " + sublineName + "\n" +
               "Arrêts: " + stopsString.toString() + "\n" +
               "Heure de départ: " + heureDepart + "\n" +
               "Heure d'arrivée: " + heureArrivee;
    }


}
