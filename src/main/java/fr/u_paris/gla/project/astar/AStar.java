package fr.u_paris.gla.project.astar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Math.min;

public class AStar {

    private Graph graph;
    private Noeud noeudDepart;
    private Noeud noeudArrivee;

    public AStar(Graph graph, Noeud noeudDepart, Noeud noeudArrivee){
        this.graph = graph;
        this.noeudDepart = noeudDepart;
        this.noeudArrivee = noeudArrivee;
    }

    public int smallestHeuristic(Noeud n1, Noeud n2){
        return min(n1.getHeuristique(), n2.getHeuristique());
    }

    public Queue makeAStar() {
        Queue<Noeud> nonExplores = new LinkedList<>();
        ArrayList<Noeud> dejaExplores = new ArrayList<>();
        nonExplores.add(noeudDepart);

        Iterator<Noeud> it = nonExplores.iterator();
        while (it.hasNext()){
            String element = String.valueOf(it.next());
            //dejaExplores = smallestHeuristic();
            System.out.println(element);
        }
        return nonExplores;
    }
}
