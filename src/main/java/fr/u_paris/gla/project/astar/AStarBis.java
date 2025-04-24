package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.utils.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.time.LocalTime;
import org.apache.commons.lang3.tuple.Triple;
import fr.u_paris.gla.project.utils.TransportTypes;

public class AStarBis {
	//c'est dans costFunction qu'on décide de choisir un cout entre deux stop en distance ou en durée
	 private final CostFunction costFunction;

	    public AStarBis(CostFunction costFunction) {
	        this.costFunction = costFunction;
	    }
	
	    /* si tt continue à bien fonctionner on pourra jeter ça
	     * private ArrayList<Stop> reconstructPath1(TraversalNode node) {
	        ArrayList<Stop> path = new ArrayList<>();
	        while (node != null) {
	            path.add(node.getStop());
	            node = node.getCameFrom();
	        }
	        Collections.reverse(path);
	        return path;
	    }
	    
	    //v1 sans les horaires -> on va finir par pouvoir la jeter
	    public ArrayList<Stop> findShortestPath(Stop start, Stop goal) {
	        PriorityQueue<TraversalNode> openSet = new PriorityQueue<>();
	        Set<Stop> closedSet = new HashSet<>();
	        //les traversalNode seront instanciés que si on a besoin d'eux et 
	        //au moment du return ils sont nettoyés par le garbage collector (apres la reconstruction du chemin)
	        Map<Stop, TraversalNode> nodeMap = new HashMap<>();

	        // Création du node de départ
	        TraversalNode startNode = new TraversalNode(start);
	        startNode.setG(0);
	        startNode.setH(start.calculateDistance(goal));
	        startNode.updateF();
	        nodeMap.put(start, startNode);
	        openSet.add(startNode);

	        while (!openSet.isEmpty()) {
	            TraversalNode currentNode = openSet.poll();
	            Stop currentStop = currentNode.getStop();

	            if (currentStop.equals(goal)) {
	                return reconstructPath1(currentNode);
	            }

	            closedSet.add(currentStop);

	            for (Stop neighborStop : currentStop.getAdjacentStops()) {
	                if (closedSet.contains(neighborStop)) continue;

	                // Récupère ou crée le node du voisin
	                TraversalNode neighborNode = nodeMap.computeIfAbsent(neighborStop, stop -> new TraversalNode(stop));

	                double tentativeG = currentNode.getG() + costFunction.costBetween(currentStop, neighborStop);

	                if (tentativeG < neighborNode.getG()) {
	                    neighborNode.setCameFrom(currentNode);
	                    neighborNode.setG(tentativeG);
	                    neighborNode.setH(neighborStop.calculateDistance(goal));
	                    neighborNode.updateF();

	                    if (!openSet.contains(neighborNode)) {
	                        openSet.add(neighborNode);
	                    }
	                }
	            }
	        }

	        return new ArrayList<>(); // Pas de chemin trouvé
	    }*/

	    private ArrayList<Pair<Stop, LocalTime>> reconstructPath2(TraversalNode node) {
	        ArrayList<Pair<Stop, LocalTime>> path = new ArrayList<>();
	        while (node != null) {
	            path.add(Pair.of(node.getStop(), node.getArrivalTime()));
	            node = node.getCameFrom();
	        }
	        Collections.reverse(path);
	        return path;
	    }
	    
	    //donne les horaires en plus de la liste de stops
	    public ArrayList<Pair<Stop, LocalTime>> findShortestPath(Stop start, Stop goal, LocalTime startTime) {
	        PriorityQueue<TraversalNode> openSet = new PriorityQueue<>();
	        Set<Stop> closedSet = new HashSet<>();
	        Map<Stop, TraversalNode> nodeMap = new HashMap<>();
	        Map<Pair<Stop, TransportTypes>, LocalTime> stopsByTransportType = new HashMap<>();

	        TraversalNode startNode = new TraversalNode(start);
	        startNode.setG(0);
	        startNode.setH(start.calculateDistance(goal));
	        startNode.updateF();
	        startNode.setArrivalTime(startTime);
	        nodeMap.put(start, startNode);
	        openSet.add(startNode);

	        while (!openSet.isEmpty()) {
	            TraversalNode currentNode = openSet.poll();
	            Stop currentStop = currentNode.getStop();

	            if (currentStop.equals(goal)) {
	                return reconstructPath2(currentNode);
	            }

	            closedSet.add(currentStop);

	            for (Triple<Stop, TransportTypes, LocalTime> next : currentStop.giveNextStopsArrivalTime(currentNode.getArrivalTime())) {
	                Stop neighborStop = next.getLeft();
	                TransportTypes stopType = next.getMiddle();
	                LocalTime arrivalTimeAtNeighbor = next.getRight();

	                if (closedSet.contains(neighborStop) && stopsByTransportType.containsKey(new Pair(neighborStop, stopType))) continue;

	                stopsByTransportType.put(new Pair(neighborStop, stopType), arrivalTimeAtNeighbor);
	                TraversalNode neighborNode = nodeMap.computeIfAbsent(neighborStop, stop -> new TraversalNode(stop));

	                //a voir si il faut modif getTimeTo pour qu'il prenne en compte dans quel type de transport on est
	                double tentativeG = currentNode.getG() +
	                	    costFunction.costBetween(currentStop, neighborStop, currentNode.getArrivalTime());

	                if (tentativeG < neighborNode.getG()) {
	                    neighborNode.setCameFrom(currentNode);
	                    neighborNode.setG(tentativeG);
	                    neighborNode.setH(neighborStop.calculateDistance(goal));
	                    neighborNode.updateF();
	                    neighborNode.setArrivalTime(arrivalTimeAtNeighbor);

	                    if (!openSet.contains(neighborNode)) {
	                        openSet.add(neighborNode);
	                    }
	                }
	            }
	        }

	        return new ArrayList<>();
	    }

	   
}
