package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Stop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class AStar {
    private Graph graph;
    private Stop departStop;
    private Stop finishStop;

    public AStar(Graph graph, Stop departStop, Stop finishStop){
        this.graph = graph;
        this.departStop = departStop;
        this.finishStop = finishStop;
    }

    public AStar(Graph graph){
        this.graph = graph;
    }

    public void setDepartStop(Stop departStop){
        this.departStop = departStop;
    }

    public void setFinishStop(Stop finishStop){
        this.finishStop = finishStop;
    }

    public ArrayList<Stop> findPath(){
        PriorityQueue<Stop> openSet = new PriorityQueue<>();
        ArrayList<Stop> closedSet = new ArrayList<>();

        departStop.setG(0);
        departStop.setH(getHeuristic(departStop, finishStop));
        departStop.setF();
        openSet.add(departStop);

        while(!openSet.isEmpty()){
            Stop currentStop = openSet.poll();

            if(currentStop.equals(finishStop)){
                return reconstructPath(currentStop);
            }

            closedSet.add(currentStop);

            for(Stop neighbor : currentStop.getAdjacentStops()){
            	//si noeud déjà visité on le passe
                if(closedSet.contains(neighbor)){
                    continue;
                }
                //calcul du cout réel total pr arriver au noeud voisin de currentStop depuis departStop
                double tentativeGScore = currentStop.getG() + currentStop.distanceBetweenAdjacentStop(neighbor);

                if(!openSet.contains(neighbor)){
                    openSet.add(neighbor);
                } else if(tentativeGScore >= neighbor.getG()){ //si le cout réel est plus élevé que celui depuis le voisin
                    continue;
                }
                neighbor.setCameFrom(currentStop);
                neighbor.setG(tentativeGScore);
                neighbor.setH(getHeuristic(neighbor, finishStop));
                neighbor.setF();
            }
        }
        return new ArrayList<>(); // No path found

    }

    private ArrayList<Stop> reconstructPath(Stop currentStop) {
        ArrayList<Stop> path = new ArrayList<>();
        while(currentStop.getCameFrom() != null){
            path.add(currentStop);
            currentStop = currentStop.getCameFrom();
        }
        path.add(currentStop);
        Collections.reverse(path);
        return path;
    }

    public double getHeuristic(Stop startStop, Stop finishStop) {
        // Distance euclidienne pour avoir une meilleure estimation
        double dx = startStop.getLongitude() - finishStop.getLongitude();
        double dy = startStop.getLatitude() - finishStop.getLatitude();
        return Math.sqrt(dx*dx + dy*dy);
    }
}