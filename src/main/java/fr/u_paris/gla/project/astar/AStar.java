package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class AStar {
    private Graph graph;
    Stop departStop;
    Stop finishStop;

    public AStar(Graph graph, Stop departStop, Stop finishStop){
        this.graph = graph;
        this.departStop = departStop;
        this.finishStop = finishStop;
    }

    public ArrayList<Stop> findPath(Graph graph, Stop departStop, Stop finishStop){
        PriorityQueue<Stop> openSet = new PriorityQueue<>();
        ArrayList<Stop> closedSet = new ArrayList<>();
        openSet.add(departStop);

        while(!openSet.isEmpty()){
            Stop currentStop = openSet.poll();
            closedSet.add(currentStop);
            if(currentStop.equals(finishStop)){
                return reconstructPath(currentStop);
            }
            for(Stop adjacentStop : currentStop.getAdjacentStops()){
                if(closedSet.contains(adjacentStop)){
                    continue;
                }
                double tentativeGScore = currentStop.getG() + currentStop.distanceBetweenAdjacentStop(adjacentStop);
                if(!openSet.contains(adjacentStop) || tentativeGScore < adjacentStop.getG()){
                    adjacentStop.setCameFrom(currentStop);
                    adjacentStop.setG(tentativeGScore);
                    adjacentStop.setH(getHeuristic(adjacentStop, finishStop));
                    if(!openSet.contains(adjacentStop)){
                        openSet.add(adjacentStop);
                    }
                }
            }
        }
        return closedSet;

    }

    private ArrayList<Stop> reconstructPath(Stop currentStop) {
        ArrayList<Stop> totalPath = new ArrayList<>();
        totalPath.add(currentStop);
        while(currentStop.getCameFrom() != null){
            currentStop = currentStop.getCameFrom();
            totalPath.add(currentStop);
        }
        return totalPath;
    }

    public double getHeuristic(Stop startStop, Stop finishStop) {
        return startStop.distanceBetweenAdjacentStop(finishStop); // Distance de Manhattan
    }
}
