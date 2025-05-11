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

        // Vérification que le temps de départ de la sous-ligne est égal au temps de départ donné
        assert(departureTimeFromUsedSubline.equals(departureTime));
        assert(usedSubline.getStartStop().equals(stop1));
        assert(usedSubline.getDestination().equals(stop2));

    }

}
