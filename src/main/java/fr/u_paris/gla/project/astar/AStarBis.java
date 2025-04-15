package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import java.util.Set;
import java.util.HashSet;

public class AStarBis {
	 private final CostFunction costFunction;

	    public AStarBis(CostFunction costFunction) {
	        this.costFunction = costFunction;
	    }

	    public ArrayList<Stop> findShortestPath(Stop start, Stop goal) {
	        PriorityQueue<Stop> openSet = new PriorityQueue<>();
	        Set<Stop> closedSet = new HashSet<>();
	        Set<Stop> visitedStops = new HashSet<>(); // pour reset les f,g,h des noeuds

	        start.setG(0);
	        start.setH(start.calculateDistance(goal)); 
	        start.setF();
	        openSet.add(start);
	        visitedStops.add(start);

	        while (!openSet.isEmpty()) {
	            Stop current = openSet.poll();

	            if (current.equals(goal)) {
	            	ArrayList<Stop> path = reconstructPath(current);
	            	resetStopsForNextSearch(visitedStops);
	                return path;
	            }

	            closedSet.add(current);

	            for (Stop neighbor : current.getAdjacentStops()) {
	                if (closedSet.contains(neighbor)) continue;

	                double tentativeG = current.getG() + costFunction.costBetween(current, neighbor);

	                if (tentativeG < neighbor.getG()) {
	                    neighbor.setCameFrom(current);
	                    neighbor.setG(tentativeG);
	                    neighbor.setH(neighbor.calculateDistance(goal)); 
	                    neighbor.setF();

	                    if (!openSet.contains(neighbor)) {
	                        openSet.add(neighbor);
	                    }
	                    visitedStops.add(neighbor);
	                }
	            }
	        }
	        // si aucun chemin trouvé → on reset quand même les noeuds visités
	       	resetStopsForNextSearch(visitedStops);
	        return new ArrayList<>(); // Pas de chemin trouvé
	    }

	     
	    private void resetStopsForNextSearch(Set<Stop> visitedStops) {
	    	for (Stop stop : visitedStops) {
	            stop.setG(Double.POSITIVE_INFINITY);
	            stop.setH(Double.POSITIVE_INFINITY);
	            stop.setF();
	            stop.setCameFrom(null);
	        }
	    }
	    
	    private ArrayList<Stop> reconstructPath(Stop current) {
	        ArrayList<Stop> path = new ArrayList<>();
	        while (current != null) {
	            path.add(current);
	            current = current.getCameFrom();
	        }
	        Collections.reverse(path);
	        return path;
	    }
	
}
