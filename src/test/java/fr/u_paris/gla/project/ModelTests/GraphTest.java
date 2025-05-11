package fr.u_paris.gla.project.ModelTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Line;

class GraphTest {

	private Graph helperCreateGraph() {
		ArrayList<Stop> stops = new ArrayList<>();
	    stops.add(new Stop(48.85374687714646, 2.344673154515588, "Saint Michel Notre Dame"));
	    stops.add(new Stop(48.82822980391691, 2.3785645488372444, "Bibliothèque François Mitterrand"));
	    stops.add(new Stop(48.689207543366216, 2.383635586069716, "Gare de Juvisy"));
	    stops.add(new Stop(48.29349975242116, 2.4013079931164656, "Gare de Malesherbes"));

	    return new Graph(stops, new ArrayList<>());
	}
	
	@Test
	public void getClosestStopTest() {
	    Graph graph = helperCreateGraph();

	    try {
	        assertEquals("Saint Michel Notre Dame", graph.getClosestStop(48.8537468771, 2.34467315451).getNameOfAssociatedStation());
	        assertEquals("Bibliothèque François Mitterrand", graph.getClosestStop(48.8282298039, 2.378564548837).getNameOfAssociatedStation());
	        assertEquals("Gare de Juvisy", graph.getClosestStop(48.68920754336, 2.38363558606).getNameOfAssociatedStation());
	        assertEquals("Gare de Malesherbes", graph.getClosestStop(48.2934997524, 2.401307993116).getNameOfAssociatedStation());
	    } catch (Exception e) {
	        fail("getClosestStop a levé une exception : " + e.getMessage());
	    }
	}


}
