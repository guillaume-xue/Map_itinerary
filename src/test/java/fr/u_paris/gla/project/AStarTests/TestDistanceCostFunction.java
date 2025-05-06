package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;

public class TestDistanceCostFunction {
    @Test
    public void testDistanceCostFunction(){

        ArrayList<Stop> listOfStops = new ArrayList<>();
        ArrayList<Line> listOfLines = new ArrayList<>();

        // Create a mock graph
        Graph graph = new Graph(listOfStops, listOfLines);

        // Create stops
        Stop stopA = new Stop(0, 0, "");
        Stop stopB = new Stop(1, 1, "");

        // Add stops to the graph
        graph.addStop(stopA);
        graph.addStop(stopB);

        // Create a distance cost function
        CostFunction distanceCostFunction = new DistanceCostFunction();

        // Calculate the cost between two stops
        double cost = distanceCostFunction.costBetween(stopA, stopB, LocalTime.ofSecondOfDay(120));

        // Print the result
        //System.out.println("Cost between stopA and stopB: " + cost);
    }
}
