package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestAStar {
    @Test
    public void AStarTest(){
        /*ArrayList<Subline> sublines = new ArrayList();
        ArrayList<Stop> listOfStops = new ArrayList();
        ArrayList<Line> listOfLines = new ArrayList();
        Graph graph = new Graph(listOfStops, listOfLines);

        Stop stop1 = new Stop(0F,0F, "Gare du Nord");
        Stop stop2 = new Stop(1F,1F, "Bourg-la-Reine");

        AStar2 aStar2 = new AStar2(graph, stop1, stop2);

        System.out.println(aStar2.getHeuristic(stop1, stop2));*/

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