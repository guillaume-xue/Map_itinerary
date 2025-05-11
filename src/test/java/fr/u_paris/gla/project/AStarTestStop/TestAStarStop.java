package fr.u_paris.gla.project.AStarTestStop;

import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

import static fr.u_paris.gla.project.graph.Stop.durationWithMidnightWrap;

public class TestAStarStop {

    @Test
    public void giveDepartureTimeFromUsedSublineTest(){
        Stop stop1 = new Stop(48.88100428912917, 2.355103854928953, "Gare du Nord");
        Stop stop2 = new Stop(48.7801486976132, 2.3125717998386075, "Bourg-la-Reine");
        Subline usedSubline = new Subline(stop1, stop2);
        LocalTime departureTime = LocalTime.now();
        LocalTime departureTimeFromUsedSubline = stop1.giveDepartureTimeFromUsedSubline(departureTime, usedSubline);

        System.out.println(departureTime);
        System.out.println(departureTimeFromUsedSubline);

        // Vérification que le temps de départ de la sous-ligne est égal au temps de départ donné
        assert(departureTimeFromUsedSubline.equals(departureTime));
        assert(usedSubline.getStartStop().equals(stop1));
        assert(usedSubline.getDestination().equals(stop2));

    }

    @Test
    public void giveNextStopsArrivalTimeTest(){
        // 1. Cas des trajets véhiculés (rail, tram, subway, bus)
        LocalTime localTime = LocalTime.of(17,0);
        Stop stop1 = new Stop(48.88100428912917, 2.355103854928953, "Gare du Nord");
        Stop stop2 = new Stop(48.7801486976132, 2.3125717998386075, "Bourg-la-Reine");

        ArrayList<Triple<Stop, Subline, LocalTime>> result = new ArrayList<>();
        result.add(Triple.of(stop1, new Subline(stop1, stop2), LocalTime.of(17, 0)));

        System.out.println(result);
        System.out.println(stop1.giveNextStopsArrivalTime(localTime));
        System.out.println(stop2.giveNextStopsArrivalTime(localTime));
    }

    @Test
    public void getTimeToTest(){
        Stop stop1 = new Stop(48.88100428912917, 2.355103854928953, "Gare du Nord");
        Stop stop2 = new Stop(48.7801486976132, 2.3125717998386075, "Bourg-la-Reine");
        LocalTime departTime = LocalTime.of(17,0);
        Duration minDuration = Duration.ofHours(9999);
        assert(stop1.getTimeTo(stop2, departTime).equals(minDuration));
        System.out.println(stop1.getTimeTo(stop2, departTime));
        System.out.println(minDuration);

    }

    @Test
    public void findNextStopInSublineTest(){

    }

    @Test
    public void veryNextDepartureTest(){

    }

    @Test
    public void durationWithMidnightWrapTest(){
        // On teste si la durée entre l'horaire actuelle et l'horaire dans 15 minutes est valide dans un cas normal
        LocalTime t1 = LocalTime.now();
        LocalTime t2 = t1.plusMinutes(15);
        Duration duration = durationWithMidnightWrap(t1, t2);
        System.out.println(t1);
        System.out.println(t2);
        System.out.println();
        assert(duration.equals(Duration.ofMinutes(15)));


        // On teste si la durée entre l'horaire actuelle et l'horaire dans 15 minutes est valide en tenant compte
        // du passage à minuit
        LocalTime t3 = LocalTime.of(23,59);
        LocalTime t4 = t3.plusMinutes(2);
        Duration duration2 = durationWithMidnightWrap(t3, t4);
        System.out.println(t3);
        System.out.println(t4);
        System.out.println();
        assert(duration2.equals(Duration.ofMinutes(2)));

        // On teste le cas où le temps t6 est avant t5
        LocalTime t5 = LocalTime.now().minusMinutes(15);
        LocalTime t6 = LocalTime.now();
        Duration duration3 = durationWithMidnightWrap(t5, t6);
        System.out.println(t5);
        System.out.println(t6);
        System.out.println();
        assert(duration3.equals(Duration.ofMinutes(15)));

    }

    @Test
    public void filterByEarliestArrivalTimePerStopTest(){

    }

    @Test
    public void giveNextStopsArrivalTime(){

    }


}
