package fr.u_paris.gla.project.AStarTestStop;

import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Triple;


public class TestAStarStop {

    
    private ArrayList<LocalTime> helperCreateDepartures() {
    	return new ArrayList<>(List.of(
    		    LocalTime.of(0, 3),
    		    LocalTime.of(8, 30),
    		    LocalTime.of(12, 0),
    		    LocalTime.of(15, 45),
    		    LocalTime.of(18, 20),
    		    LocalTime.of(21, 0),
    		    LocalTime.of(23,56)
    		));

    }
    
    
    @Test
    public void veryNextDepartureTest() {
    	ArrayList<LocalTime> departures = helperCreateDepartures();
    	LocalTime res1 = Stop.veryNextDeparture(LocalTime.of(0, 0), departures);
    	LocalTime res2 = Stop.veryNextDeparture(LocalTime.of(23, 58), departures);
    	LocalTime res3 = Stop.veryNextDeparture(LocalTime.of(21, 3), departures);
    	LocalTime res4 = Stop.veryNextDeparture(LocalTime.of(15, 45), departures);
    	
    	assertEquals(res1, LocalTime.of(0, 3));
    	assertEquals(res2, LocalTime.of(0, 3));
    	assertEquals(res3, LocalTime.of(23, 56));
    	assertEquals(res4, LocalTime.of(15, 45));
    }

    
    @Test
    public void testDurationWithMidnightWrap() {
        assertEquals(Duration.ofHours(2), 
            Stop.durationWithMidnightWrap(LocalTime.of(10, 0), LocalTime.of(12, 0)));
        assertEquals(Duration.ZERO, 
            Stop.durationWithMidnightWrap(LocalTime.of(15, 30), LocalTime.of(15, 30)));
        assertEquals(Duration.ofHours(1), 
            Stop.durationWithMidnightWrap(LocalTime.of(23, 30), LocalTime.of(0, 30)));
        assertEquals(Duration.ofMinutes(10), 
            Stop.durationWithMidnightWrap(LocalTime.of(23, 55), LocalTime.of(0, 5)));
        assertEquals(Duration.ofMinutes(1), 
            Stop.durationWithMidnightWrap(LocalTime.of(23, 59), LocalTime.MIDNIGHT));
        assertEquals(Duration.ofMinutes(1439),
            Stop.durationWithMidnightWrap(LocalTime.MIDNIGHT, LocalTime.of(23, 59)));
        assertEquals(Duration.ofMinutes(1439),
            Stop.durationWithMidnightWrap(LocalTime.of(0, 2), LocalTime.of(0, 1)));
    }
    
    
    @Test
    public void testFindNextStopInSubline() {
        Stop stop1 = new Stop(0, 0, "Stop 1");
        Stop stop2 = new Stop(0, 1, "Stop 2");
        Stop stop3 = new Stop(0, 2, "Stop 3");
        Stop stop4 = new Stop(0, 3, "Stop 4");

        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(stop1);
        stops.add(stop2);
        stops.add(stop3);

        Subline subline = new Subline("Ligne A");
        subline.setListOfStops(stops);

        assertEquals(stop2, stop1.findNextStopInSubline(subline), "Le stop suivant de stop1 devrait être stop2.");
        assertEquals(stop3, stop2.findNextStopInSubline(subline), "Le stop suivant de stop2 devrait être stop3.");
        assertNull(stop3.findNextStopInSubline(subline), "Le stop3 est le dernier, donc aucun stop suivant.");
        assertNull(stop4.findNextStopInSubline(subline), "Stop4 n'est pas dans la subline, donc doit retourner null.");
    }
}
