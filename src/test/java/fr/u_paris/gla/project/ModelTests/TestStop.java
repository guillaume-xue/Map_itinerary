package fr.u_paris.gla.project;

import java.time.LocalTime;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;

import fr.u_paris.gla.project.graph.Stop;


public class TestStop {

    @Test
    public void addAdjacentStopTest(){
        // 3 stations du RER C
        Stop stop1 = new Stop(48.828382841376715F, 2.379067231453914F, "Bibliothèque François Mitterand");
        Stop stop2 = new Stop(48.84252292928216F, 2.3672210648002685F, "Paris Austerlitz");
        Stop stop3 = new Stop(48.853767644075354F, 2.344640478142948F, "St-Michel Notre Dame");

        assert(stop1.getNameOfAssociatedStation() == "Bibliothèque François Mitterand");
        assert(stop2.getNameOfAssociatedStation() == "Paris Austerlitz");
        assert(stop3.getNameOfAssociatedStation() == "St-Michel Notre Dame");


        LocalTime now = LocalTime.now();
        LocalTime timeToNextStation = LocalTime.now().plusMinutes(3); // 3 minutes entre Bibliothèque François Mitterand et Paris Austerlitz
        Float distanceToNextStation = 1900F; // 1900 mètres entre Bibliothèque François Mitterand et Paris Austerlitz
        HashMap<Stop, MutablePair<LocalTime, Float>> timeDistancePerAdjacentStop = new HashMap<>();

        // timeDistancePerAdjacentStop.put(adjacentStop, new MutablePair<>(timeToNextStation, distanceToNextStation));

    }

    @Test
    public void addDepartureTest(){

        Stop stop1 = new Stop(0F,0F,"Gare du Nord");
        Stop stop2 = new Stop(0F,0F,"Robinson");
        Stop stop3 = new Stop(0F,0F,"Aéroport Charles de Gaulle 2 - TGV");
        Stop stop4 = new Stop(0F,0F,"Saint-Rémy-lès-Chevreuse");
        Stop stop5 = new Stop(0F,0F,"Mitry-Claye");


        /*// Entre Gare du Nord et Bourg-la-Reine
        Subline subline1 = new Subline("B1", stop1, departureTimesAtStartOfSubline);

        // Entre Bourg-la-Reine et Robinson
        Subline subline2 = new Subline("B2", stop2, departureTimesAtStartOfSubline);

        // Entre Aéroport Charles de Gaulle 2 - TGV et Aulnay-sous-Bois
        Subline subline3 = new Subline("B3", stop3, departureTimesAtStartOfSubline);

        // Entre Bourg-la-Reine et Saint-Rémy-lès-Chevreuse
        Subline subline4 = new Subline("B4", stop4, departureTimesAtStartOfSubline);

        // Entre Aulnay-sous-Bois et Mitry-Claye
        Subline subline5 = new Subline("B5", stop5, departureTimesAtStartOfSubline);*/


        LocalTime time1 = LocalTime.of(14,00);
        LocalTime time2 = LocalTime.of(14,30);
        LocalTime time3 = LocalTime.of(15,00);
        LocalTime time4 = LocalTime.of(15,30);
        LocalTime time5 = LocalTime.of(16,00);



    }

    public static void main(String[] args) {
        System.out.println("test");
    }
}

