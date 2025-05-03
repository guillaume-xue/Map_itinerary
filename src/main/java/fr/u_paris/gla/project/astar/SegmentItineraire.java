package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.*;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Représente une portion de trajet effectuée sur une même sous-ligne,
 * entre plusieurs arrêts consécutifs.
 * <p>
 * Un segment contient la sous-ligne utilisée, la liste des arrêts desservis pendant ce segment,
 * ainsi que les heures de debut et de fin du parcours du segment.
 */
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
            stopsString.setLength(stopsString.length() - 4); 
        }

        String lineName = (subline != null && subline.getAssociatedLine() != null) 
            ? subline.getAssociatedLine().getName()
            : "Aucune ligne";

        return "Ligne: " + lineName + "\n" +
               "Arrêts: " + stopsString.toString() + "\n" +
               "Heure de départ: " + heureDepart + "\n" +
               "Heure d'arrivée: " + heureArrivee;
    }

    public LocalTime getHeureArrivee() {
      return heureArrivee;
    }

    public LocalTime getHeureDepart() {
      return heureDepart;
    }

    public ArrayList<Stop> getStops() {
      return stops;
    }
    
    public Subline getSubline() {
      return subline;
    }

}
