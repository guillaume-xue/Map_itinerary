package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestAStar {


    @Test
    public void testAStar() throws Exception {
        // Create a list of stops
        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(new Stop(1.0, 2.0, "Stop A"));
        stops.add(new Stop(3.0, 4.0, "Stop B"));
        stops.add(new Stop(5.0, 6.0, "Stop C"));

        // Create a list of lines
        ArrayList<Line> lines = new ArrayList<>();
        lines.add(new Line("1", "Line 1", "Metro", "Red"));
        lines.add(new Line("2", "Line 2", "Bus", "Blue"));

        // Create a graph with stops and lines
        Graph graph = new Graph(stops, lines);

        // Create an AStar instance
        AStar aStar = new AStar(graph);

        // Define start and end stops
        //Stop start = graph.getStop(1.0, 2.0);
        //Stop end = graph.getStop(3.0, 4.0);

        // Perform A* search
        ArrayList<Stop> path = aStar.findPath();

        // Print the path
        System.out.println("Path found: " + path);
    }
}
