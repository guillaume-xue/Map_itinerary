package fr.u_paris.gla.project.graph;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;

public class TestLine {
    @Test
    public void addSublineTest(){
        ArrayList<Subline> listOfSublines = new ArrayList<>();
        ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();

        Line line = new Line("B");

        ArrayList<Subline> sublines = new ArrayList<>();

        for (Subline subline : sublines){
            System.out.println(subline);
        }

        Stop stop1 = new Stop(0F,0F,"Gare du Nord");
        Stop stop2 = new Stop(0F,0F,"Robinson");
        Stop stop3 = new Stop(0F,0F,"Aéroport Charles de Gaulle 2 - TGV");
        Stop stop4 = new Stop(0F,0F,"Saint-Rémy-lès-Chevreuse");
        Stop stop5 = new Stop(0F,0F,"Mitry-Claye");

        // Entre Gare du Nord et Bourg-la-Reine
        Subline subline1 = new Subline("B1");

        // Entre Bourg-la-Reine et Robinson
        Subline subline2 = new Subline("B2");

        // Entre Aéroport Charles de Gaulle 2 - TGV et Aulnay-sous-Bois
        Subline subline3 = new Subline("B3");

        // Entre Bourg-la-Reine et Saint-Rémy-lès-Chevreuse
        Subline subline4 = new Subline("B4");

        // Entre Aulnay-sous-Bois et Mitry-Claye
        Subline subline5 = new Subline("B5");


        LocalTime time1 = LocalTime.of(14,00);
        LocalTime time2 = LocalTime.of(14,30);
        LocalTime time3 = LocalTime.of(15,00);
        LocalTime time4 = LocalTime.of(15,30);
        LocalTime time5 = LocalTime.of(16,00);

        line.addSubline(subline1);
        line.addSubline(subline2);
        line.addSubline(subline3);
        line.addSubline(subline4);
        line.addSubline(subline5);

        listOfSublines.add(subline1);

        for(Subline subline : listOfSublines){
            System.out.println(subline);
        }
    }
}
