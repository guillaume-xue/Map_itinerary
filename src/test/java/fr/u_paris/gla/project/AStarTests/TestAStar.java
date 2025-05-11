package fr.u_paris.gla.project;

import fr.u_paris.gla.project.graph.*;
import fr.u_paris.gla.project.astar.*;
import fr.u_paris.gla.project.utils.*;

import org.apache.commons.lang3.tuple.MutablePair;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;

public class TestAStar {

    /**
     * A (so-called) test ( written poorly ) that aims at testing the efficiency of the algorithm. It is basically a script 
     * that generates the results of n itinerary searches without the need of doing it by hand in GUI.
     * 
     * The test runs the n itinerary and outputs the results in txt files. Enabling us to compare the results 
     * rapidly and tweak the algorithm constants / data, to find the optimal configuration for the algorithm.
     * 
     * Test needs the runEfficiencTest option to run by doing -> mvn test -D runEfficiencyTest at launch.
     */
    @Test
    @EnabledIfSystemProperty(named = "runEfficiencyTest", matches = "true")
    public void efficiencyTest() {
        try {
            // Config -> adapt everything before launching
            Path outputPath = Paths.get("results/effi5.txt"); 
            boolean connectStops = true;
            String jarVersion = "1.2.0";
            String maxWalkingDistance = "100";
            String maxWalkingDistanceStartAndFinish = "700";
            String note = "5th try - close to dev version + connects stop with 100 meters + openSet change";
            String test1 = "Test1: 24 Rue Paul Vaillant Couturier, Alfortville -> Place Aurélie Nemours, Paris, 75013";
            String test2 = "Test2: 103 Rue Damrémont, Paris, 75018 -> 62 Avenue Gabriel Péri, Montreuil, 93100";
            // List des paires de coordonnées à tester
            List<Pair<Pair<Double, Double>, Pair<Double, Double>>> testPairs = List.of(
                Pair.of(Pair.of(48.81401374746832, 2.4154794216156006), Pair.of(48.827124666357115, 2.380342483520508)),
                // 24 Rue Paul Vaillant Couturier, Alfortville -> Place Aurélie Nemours, Paris, 75013
                Pair.of(Pair.of(48.896182868598444, 2.33809232711792), Pair.of(48.85627286259125, 2.445230484008789))
                // 103 Rue Damrémont, Paris, 75018 -> 62 Avenue Gabriel Péri, Montreuil, 93100

            );

            // Setup
            long setupStart = System.nanoTime();
            String[] args = new String[] { "stopsData.csv", "junctionsData.csv", "Schedules/" };
            Graph graph = CSVExtractor.makeObjectsFromCSV(args);;

            long walkConnectTime = 0;
            if (connectStops) {
                long walkStart = System.nanoTime();
                graph.connectStopsByWalkingV2();
                walkConnectTime = System.nanoTime() - walkStart;
            }

            CostFunction costFunctionDistance = CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DISTANCE);
            CostFunction costFunctionDuration = CostFunctionFactory.getCostFunction(CostFunctionFactory.Mode.DURATION);
            
            AStar astarDistance = new AStar(costFunctionDistance);
            AStar astarDuration = new AStar(costFunctionDuration);
            long setupEnd = System.nanoTime();

            LocalTime heureDepart = LocalTime.of(12,0); // 12h

            List<String> resultLines = new ArrayList<>();
            resultLines.add("=== Efficiency Test Configuration ===");
            resultLines.add("ConnectStopsByWalking: " + connectStops);
            resultLines.add("MaxWalkingDistance (m): " + maxWalkingDistance);
            resultLines.add("maxWalkingDistanceStartAndFinish (m): " + maxWalkingDistanceStartAndFinish);            
            resultLines.add("CostFunction: both");
            resultLines.add("Number of Tests: " + testPairs.size());
            resultLines.add("Graph Setup Time (s): " + ((setupEnd - setupStart) / 1_000_000_000.0));
            resultLines.add("Walking Connect Time (s): " + (walkConnectTime / 1_000_000_000.0));
            resultLines.add("Heure depart: " + heureDepart);
            resultLines.add("Jar Version: " + jarVersion);
            resultLines.add("Note: " + note );
            resultLines.add(test1);
            resultLines.add(test2);
            resultLines.add("\n=== Path Results ===");

            for (int i = 0; i < testPairs.size(); i++) {

                Pair<Double, Double> p1 = testPairs.get(i).getKey();
                Pair<Double, Double> p2 = testPairs.get(i).getValue();

                MutablePair<Stop, Stop> startFinish = graph.addStartAndFinish("Start", "Finish", p1.getKey(), p1.getValue(),
                p2.getKey(), p2.getValue());

                long pathStart = System.nanoTime();
                ArrayList<SegmentItineraire> itineraryDistance = astarDistance.findShortestPath(
                    startFinish.getLeft(), startFinish.getRight(), heureDepart);
                long pathEnd = System.nanoTime();

                resultLines.add(String.format("\n--- Test %d ---", i + 1));
                resultLines.add("AStar Distance - heure de départ: " + heureDepart);
                resultLines.add("Execution Time (s): " + ((pathEnd - pathStart) / 1_000_000_000));
                resultLines.add("Path Found: " + (!itineraryDistance.isEmpty()));
                for (SegmentItineraire segment : itineraryDistance) {
                    resultLines.add(segment.toString());
                }

                pathStart = System.nanoTime();
                ArrayList<SegmentItineraire> itineraryDuration = astarDuration.findShortestPath(
                    startFinish.getLeft(), startFinish.getRight(), heureDepart);
                pathEnd = System.nanoTime();

                resultLines.add("\nAStar Duration - heure de départ: " + heureDepart);
                resultLines.add("Execution Time (s): " + ((pathEnd - pathStart) / 1_000_000_000));
                resultLines.add("Path Found: " + (!itineraryDuration.isEmpty()));
                for (SegmentItineraire segment : itineraryDuration) {
                    resultLines.add(segment.toString());
                }

            }

            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, resultLines);

            //System.out.println("Efficiency test completed. Results written to " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
