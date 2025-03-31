package fr.u_paris.gla.project;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import fr.u_paris.gla.project.astar.AStar;
import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;

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

    @Test
    public void testAStarPathFinding(){
        // Création des stops
        Stop stopA = new Stop(0.0, 0.0, "A");
        Stop stopB = new Stop(1.0, 1.0, "B");
        Stop stopC = new Stop(2.0, 2.0, "C");
        Stop stopD = new Stop(3.0, 3.0, "D");
        Stop stopE = new Stop(4.0, 4.0, "E");

        // Création des connexions
        stopA.addAdjacentStop(stopB, null, 1.5f);
        stopB.addAdjacentStop(stopA, null, 1.5f);
        stopB.addAdjacentStop(stopC, null, 1.5f);
        stopC.addAdjacentStop(stopB, null, 1.5f);
        stopC.addAdjacentStop(stopD, null, 1.5f);
        stopD.addAdjacentStop(stopC, null, 1.5f);
        stopD.addAdjacentStop(stopE, null, 1.5f);
        stopE.addAdjacentStop(stopD, null, 1.5f);

        // Ajout d'un chemin plus court mais moins direct
        stopA.addAdjacentStop(stopE, null, 10.0f);
        stopE.addAdjacentStop(stopA, null, 10.0f);

        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(stopA);
        stops.add(stopB);
        stops.add(stopC);
        stops.add(stopD);
        stops.add(stopE);

        Graph graph = new Graph(stops, new ArrayList<>());
        AStar aStar = new AStar(graph, stopA, stopE);

        ArrayList<Stop> path = aStar.findPath();

        System.out.println(aStar.getHeuristic(stopA, stopE));

        // Vérification du chemin optimal
        /*assertEquals(5, path.size());
        assertEquals("A", path.get(0).getNameOfAssociatedStation());
        assertEquals("B", path.get(1).getNameOfAssociatedStation());
        assertEquals("C", path.get(2).getNameOfAssociatedStation());
        assertEquals("D", path.get(3).getNameOfAssociatedStation());
        assertEquals("E", path.get(4).getNameOfAssociatedStation());*/

        // Test avec un noeud isolé
        Stop isolatedStop = new Stop(10.0, 10.0, "Isolated");
        stops.add(isolatedStop);
        AStar aStar2 = new AStar(graph, stopA, isolatedStop);
        ArrayList<Stop> noPath = aStar2.findPath();
        assertTrue(noPath.isEmpty());
    }
}