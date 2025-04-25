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

public class AStarBis {
	//c'est dans costFunction qu'on décide de choisir un cout entre deux stop en distance ou en durée
	 private final CostFunction costFunction; 

	    public AStarBis(CostFunction costFunction) {
	        this.costFunction = costFunction;
	    }
	
	   // -----------VERSIONS A JETER QD GUI A JOUR AVEC NVELLE VERSION
	    
	    //fait des bails bizarres
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
	            	//System.out.println("tentative reconstruction chemin");
	                return reconstructPath2(currentNode);
	            }

	            closedSet.add(currentStop);

	            for (Triple<Stop, Subline, LocalTime> next : currentStop.giveNextStopsArrivalTime(currentNode.getArrivalTime())) {
	                Stop neighborStop = next.getLeft();
	                LocalTime arrivalTimeAtNeighbor = next.getRight();
	                if (closedSet.contains(neighborStop)) {
	                	continue;
	                }
	                TraversalNode neighborNode = nodeMap.computeIfAbsent(neighborStop, stop -> new TraversalNode(stop));
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
	    
	 
	 // ---------NOUVELLES VERSIONS A UTILISER -----------------
	    private ArrayList<SegmentItineraire> reconstructPath3(TraversalNode endNode) {
	        ArrayList<SegmentItineraire> result = new ArrayList<>();

	        LinkedList<Stop> currentStops = new LinkedList<>();
	        TraversalNode current = endNode;
	        Subline currentSubline = current.getSublineUsed();
	        LocalTime heureArrivee = current.getArrivalTime();
	        LocalTime heureDepart = current.getDepartureTime();

	        currentStops.addFirst(current.getStop());

	        while (current.getCameFrom() != null) {
	            TraversalNode previous = current.getCameFrom();

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
	        result.add(0, new SegmentItineraire(currentSubline, new ArrayList<>(currentStops), heureDepart, heureArrivee));

	        return result;
	    }


	    //pê nettoyer les departureTime en utilisant les ArrivalTime des stopPrecedents
	    public ArrayList<SegmentItineraire> findShortestPath2(Stop start, Stop goal, LocalTime startTime) {
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
	            	System.out.println("tentative reconstruction chemin");
	                return reconstructPath3(currentNode);
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
