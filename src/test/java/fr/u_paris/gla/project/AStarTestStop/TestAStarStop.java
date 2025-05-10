package fr.u_paris.gla.project.AStarTestStop;

import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

public class TestAStarStop {

    @Test
    public void giveDepartureTimeFromUsedSublineTest(){
        // 3 stations du RER C
        Stop stop1 = new Stop(48.88100428912917, 2.355103854928953, "Gare du Nord");
        Stop stop2 = new Stop(48.7801486976132, 2.3125717998386075, "Bourg-la-Reine");
        Subline subline = new Subline(stop1, stop2);
        LocalTime departureTime = LocalTime.now();
        LocalTime departureTimeFromUsedSubline = stop1.giveDepartureTimeFromUsedSubline(departureTime, subline);

        System.out.println(departureTimeFromUsedSubline);
        System.out.println(departureTime);
    }

    @Test
    public void giveNextStopsArrivalTimeTest(){

    }

    @Test
    public void giveTimeToTest(){

    }

    @Test
    public void findNextStopInSublineTest(){

    }

    @Test
    public void veryNextDepartureTest(){

    }

    @Test
    public void durationWithMidnightWrapTest(){

    }

    @Test
    public void filterByEarliestArrivalTimePerStopTest(){

    }

    @Test
    public void giveNextStopsArrivalTime(){
        
    }


}
