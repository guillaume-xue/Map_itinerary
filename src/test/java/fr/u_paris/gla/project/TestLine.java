package fr.u_paris.gla.project;

import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;

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

    @Test
    public void addLineTest(){
        ArrayList<Line> listOfLines = new ArrayList<>();
        Line line = new Line("B");
        Subline subline1 = new Subline("B1");
        Subline subline2 = new Subline("B2");
        Subline subline3 = new Subline("B3");
        Subline subline4 = new Subline("B4");
        Subline subline5 = new Subline("B5");

        line.addSubline(subline1);
        line.addSubline(subline2);
        line.addSubline(subline3);
        line.addSubline(subline4);
        line.addSubline(subline5);

        listOfLines.add(line);
        for (Line l : listOfLines){
            System.out.println(l);
        }
    }

    @Test
    public void addAllLinesTest(){
        ArrayList<Subline> sublines = new ArrayList();
        ArrayList<Line> listOfLines = new ArrayList();
        ArrayList<Stop> listOfStops = new ArrayList();

        // Lignes de Train (RER et transilien) :

        listOfLines.add(new Line("RER A"));
        listOfLines.add(new Line("RER B"));
        listOfLines.add(new Line("RER C"));
        listOfLines.add(new Line("RER D"));
        listOfLines.add(new Line("RER E"));

        listOfLines.add(new Line("Ligne H"));
        listOfLines.add(new Line("Ligne J"));
        listOfLines.add(new Line("Ligne K"));
        listOfLines.add(new Line("Ligne L"));
        listOfLines.add(new Line("Ligne N"));
        listOfLines.add(new Line("Ligne P"));
        listOfLines.add(new Line("Ligne R"));
        listOfLines.add(new Line("Ligne U"));



        // Lignes de Métro :

        for (int i=1; i<=14; i++){
            if(i == 3 || i == 7){
                listOfLines.add(new Line("Métro " + String.valueOf(i)));
                listOfLines.add(new Line("Métro " + String.valueOf(i) + "bis"));
            }
            else{
                listOfLines.add(new Line("Métro " + String.valueOf(i)));
            }
        }

        // Lignes de Tramway :

        for (int i=1; i<=14; i++){
            if(i == 3){
                listOfLines.add(new Line("T" + String.valueOf(i) + "a"));
                listOfLines.add(new Line("T" + String.valueOf(i) + "b"));
            }
            else{
                listOfLines.add(new Line("T" + String.valueOf(i)));
            }
        }

        for (Line line: listOfLines){
            System.out.println(line.toString());
        }
    }


}
