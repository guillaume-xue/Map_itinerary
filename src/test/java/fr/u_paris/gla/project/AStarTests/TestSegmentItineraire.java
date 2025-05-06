package fr.u_paris.gla.project.AStarTests;

import fr.u_paris.gla.project.astar.SegmentItineraire;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import fr.u_paris.gla.project.idfm.CSVStreamProviderForMapData;
import fr.u_paris.gla.project.idfm.StopEntry;
import fr.u_paris.gla.project.utils.GPS;
import fr.u_paris.gla.project.utils.TransportTypes;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSegmentItineraire {
    @Test
    public void testToString() {
        // Create a SegmentItineraire object with sample data
        Subline subline = new Subline("B1");
        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(new Stop(0,0,"Gare du Nord"));
        stops.add(new Stop(0,0,"Chatelet"));
        LocalTime heureDepart = LocalTime.of(10, 0);
        LocalTime heureArrivee = LocalTime.of(10, 30);

        SegmentItineraire segment = new SegmentItineraire(subline, stops, heureDepart, heureArrivee);

        // Call the toString method
        String result = segment.toString();

        // Print the result
        //System.out.println(result);
    }

    @Test
    public void testSegmentItineraire() throws Exception {
        Subline subline = new Subline("B1");
        ArrayList<Stop> stops = new ArrayList<>();

        // Itinéraires avec Google Maps
        Stop stop1 = new Stop(48.85374687714646, 2.344673154515588, "Saint Michel Notre Dame");
        Stop stop2 = new Stop(48.82822980391691, 2.3785645488372444, "Bibliothèque François Mitterrand");
        Stop stop3 = new Stop(48.689207543366216, 2.383635586069716, "Gare de Juvisy");
        Stop stop4 = new Stop(48.29349975242116, 2.4013079931164656, "Gare de Malesherbes");

        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);
        stops.add(stop4);

        LocalTime heureDepart = LocalTime.of(10, 0);
        LocalTime heureArrivee = LocalTime.of(11, 0);
        SegmentItineraire segment = new SegmentItineraire(subline, stops, heureDepart, heureArrivee);


        // Itinéraires avec IDFM

        //System.out.println("SegmentItineraire: " + segment + "\n");
        assertEquals(subline, segment.getSubline());
        assertEquals(heureDepart, segment.getHeureDepart());
        assertEquals(heureArrivee, segment.getHeureArrivee());
        assertEquals(stops, segment.getStops());

        ArrayList<Line> lines = new ArrayList<>();
        Graph graph = new Graph(stops, lines);

        //System.out.println(stop1.getNameOfAssociatedStation()); // Saint Michel Notre Dame
        assertEquals(graph.getClosestStop(48.85, 2.34).getNameOfAssociatedStation(), "Saint Michel Notre Dame");

        //System.out.println(stop2.getNameOfAssociatedStation()); // Bibliothèque François Mitterrand
        assertEquals(graph.getClosestStop(48.82, 2.37).getNameOfAssociatedStation(), "Bibliothèque François Mitterrand");

        //System.out.println(stop3.getNameOfAssociatedStation()); // Gare de Juvisy
        assertEquals(graph.getClosestStop(48.68, 2.38).getNameOfAssociatedStation(), "Gare de Juvisy");

        //System.out.println(stop4.getNameOfAssociatedStation()); // Gare de Malesherbes
        assertEquals(graph.getClosestStop(48.29, 2.40).getNameOfAssociatedStation(), "Gare de Malesherbes");

    }
}


