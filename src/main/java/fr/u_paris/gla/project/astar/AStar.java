package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.*;
import fr.u_paris.gla.project.utils.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.time.LocalTime;
import org.apache.commons.lang3.tuple.Triple;
import fr.u_paris.gla.project.utils.TransportTypes;

public class AStar {
	//c'est dans costFunction qu'on décide de choisir un cout entre deux stop en distance ou en durée
	 private final CostFunction costFunction; 

	    public AStar(CostFunction costFunction) {
	        this.costFunction = costFunction;
	    }
	
	    private ArrayList<SegmentItineraire> reconstructPath(TraversalNode endNode) {
	        ArrayList<SegmentItineraire> result = new ArrayList<>();

	        LinkedList<Stop> currentStops = new LinkedList<>();
	        TraversalNode current = endNode;
	        Subline currentSubline = current.getSublineUsed();
	        LocalTime heureArrivee = current.getArrivalTime();
	        LocalTime heureDepart = current.getDepartureTime();

	        currentStops.addFirst(current.getStop());

	        TraversalNode previous = current.getCameFrom();
	        while (current.getCameFrom() != null) {
	            previous = current.getCameFrom();

	            // Si la subline change, on termine le segment
	            if (!currentSubline.equals(previous.getSublineUsed())) {
	                currentStops.addFirst(previous.getStop()); // on inclut aussi le stop précédent dans le segment
	                result.add(0, new SegmentItineraire(currentSubline, new ArrayList<>(currentStops), heureDepart, heureArrivee));

	                // Préparation du prochain segment
	                currentStops.clear();
	                currentStops.addFirst(previous.getStop());

	                currentSubline = previous.getSublineUsed();
	                heureArrivee = previous.getArrivalTime();
	                heureDepart = previous.getDepartureTime();
	            } else {
	                currentStops.addFirst(previous.getStop());
	                heureDepart = previous.getDepartureTime(); // mise à jour en continu
	            }

	            current = previous;
	        }

	        // Ajouter le dernier segment (du début de l'itinéraire)
	        //inutile si on dit qu'on part du stop le plus proche de l'endroit qu'on entre ds l'interface
	        //result.add(0, new SegmentItineraire(currentSubline, new ArrayList<>(currentStops), heureDepart, heureArrivee));

	        return result;
	    }


	    
	    public ArrayList<SegmentItineraire> findShortestPath(Stop start, Stop goal, LocalTime startTime) {
	        PriorityQueue<TraversalNode> openSet = new PriorityQueue<>();
	        Map<Stop, TraversalNode> nodeMap = new HashMap<>();
	        Set<Stop> closedSet = new HashSet<>();

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
	            	System.out.println("Un chemin trouvé ! reconstruction en cours...");
	                return reconstructPath(currentNode);
	            }

	            closedSet.add(currentStop);
	            for (Triple<Stop, Subline, LocalTime> next : currentStop.giveNextStopsArrivalTime(currentNode.getArrivalTime())) {
	                Stop neighborStop = next.getLeft();
	                
	                if (closedSet.contains(neighborStop)) continue;
	                
	                TransportTypes transportType = next.getMiddle().getSublineType();
	                LocalTime arrivalTimeAtNeighbor = next.getRight();
	                Subline usedSubline = next.getMiddle();
	                
	                LocalTime departureTime = currentNode.getArrivalTime();
	                TraversalNode neighborNode = nodeMap.computeIfAbsent(neighborStop, stop -> new TraversalNode(stop));

	                double tentativeG = currentNode.getG() +
	                    costFunction.costBetween(currentStop, neighborStop, currentNode.getArrivalTime());

	                if (tentativeG < neighborNode.getG()) {
	                    neighborNode.setCameFrom(currentNode);
	                    neighborNode.setG(tentativeG);
	                    neighborNode.setH(neighborStop.calculateDistance(goal));
	                    neighborNode.updateF();
	                    neighborNode.setArrivalTime(arrivalTimeAtNeighbor);
	                    neighborNode.setSublineUsed(usedSubline); 
	                    neighborNode.setDepartureTime(departureTime); 

	                    if (!openSet.contains(neighborNode)) {
	                        openSet.add(neighborNode);
	                    }
	                }
	            }
	        }
	        return new ArrayList<>();
	    }
	    
	   
	    
	   
}
