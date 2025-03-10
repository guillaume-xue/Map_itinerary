package fr.u_paris.gla.project.astar;

import java.util.ArrayList;

public class Graph {
    ArrayList<Noeud> noeuds;

    public Graph(ArrayList<Noeud> noeuds){
        this.noeuds = new ArrayList<>();
    }

    public ArrayList<Noeud> getNoeuds(){
        return noeuds;
    }

    /*public ArrayList<Noeud> parcourtGraphe(){
        Iterator<Noeud> it = noeuds.iterator();
        while (it.hasNext()){
            String element = String.valueOf(it.next());
            System.out.println(element);
        }

        return noeuds;
    }*/

    @Override
    public String toString() {
        return "Graph{" +
                "noeuds=" + noeuds +
                '}';
    }
}
