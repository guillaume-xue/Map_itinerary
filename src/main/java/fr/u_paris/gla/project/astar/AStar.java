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


/**
 * Cette classe implémente l'algorithme A* pour rechercher le chemin optimal entre deux arrêts (Stop)
 * dans un réseau de transport. Le coût utilisé pour évaluer les chemins peut être basé sur la distance
 * ou sur la durée, selon l'implémentation de {@link CostFunction}.
 * 
 * L'algorithme tient compte des changements de ligne (Subline) en ajoutant un coût supplémentaire
 * lors d'une correspondance. Il reconstruit le chemin final sous forme de segments (SegmentItineraire),
 * chacun correspondant à une portion d'itinéraire sur une même ligne.
 * 
 * 
 */
public class AStar {
	 /** Fonction de coût entre deux arrêts (distance ou durée) */
	 private final CostFunction costFunction; 

	 	
	    public AStar(CostFunction costFunction) {
	        this.costFunction = costFunction;
	    }
	
	    /**
	     * Reconstruit le chemin optimal en segments à partir du dernier nœud (goal).
	     * Chaque segment représente un parcours continu sur la même ligne de transport.
	     *
	     * @param endNode Le dernier nœud atteint par l'algorithme A* (objectif).
	     * @return Une liste ordonnée de {@link SegmentItineraire} représentant le chemin complet.
	     */
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
	        return result;
	    }


	    
	    /**
	     * Trouve le chemin le plus court entre deux arrêts à une heure donnée, en utilisant l'algorithme A*.
	     * Prend en compte les temps d'attente, les coûts de changement de ligne, et la fonction de coût choisie.
	     * 
	     * @param start      L'arrêt de départ.
	     * @param goal       L'arrêt d'arrivée.
	     * @param startTime  L'heure de départ.
	     * @return Une liste de {@link SegmentItineraire} représentant le chemin optimal trouvé, 
	     *         ou une liste vide si aucun chemin n'existe.
	     */
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
	            	ArrayList<SegmentItineraire> path = reconstructPath(currentNode);
	            	//nettoyage des nodes visités
	            	for (TraversalNode node : nodeMap.values()) {
	            	    node.reset();
	            	}
	                return path;
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

	                
	                //cout supplémentaire de 30sec pour chgmt de ligne
	                double coutChgmtLigne = (currentNode.getSublineUsed() == null || usedSubline == null)
	                	    ? 0.0
	                	    : (currentNode.getSublineUsed().equals(usedSubline) ? 0.0 : 30.0);
	                
	                double tentativeG = currentNode.getG() +
	                    costFunction.costBetween(currentStop, neighborStop, currentNode.getArrivalTime()) +
	                    coutChgmtLigne;

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
	      //nettoyage des nodes visités
	        for (TraversalNode node : nodeMap.values()) {
	            node.reset();
	        }
	        return new ArrayList<>();
	    }
	    
	    
	    
	   
}
