package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestAStar {
    @Test
    public void AStarTest(){
        ArrayList<Subline> sublines = new ArrayList();
        ArrayList<Stop> listOfStops = new ArrayList();
        ArrayList<Line> listOfLines = new ArrayList();
        Graph graph = new Graph(listOfStops, listOfLines);

        Stop stop1 = new Stop(0F,0F, "Gare du Nord");
        Stop stop2 = new Stop(0F,0F, "Bourg-la-Reine");

        AStar aStar = new AStar(graph, stop1, stop2);

        System.out.println(aStar.getHeuristic(stop1, stop2));
        System.out.println(graph);

    }
}