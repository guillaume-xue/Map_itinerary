package fr.u_paris.gla.project.astar;


import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import java.time.LocalTime;

/**
 * Représente un nœud utilisé dans l'algorithme A* pour la recherche de chemin dans un réseau de transport.
 * Chaque nœud contient un arrêt de transport, des informations de coût (g, h, f),
 * des références vers le nœud précédent, les horaires d'arrivée/départ, et la ligne utilisée.
 * 
 * Cette classe permet de comparer les nœuds selon leur coût total estimé (f = g + h),
 * ce qui permet leur utilisation dans une PriorityQueue.
 */
public class TraversalNode implements Comparable<TraversalNode> {
    private Stop stop;
    private TraversalNode cameFrom = null;
    private double g;
    private double h;
    private double f;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Subline sublineUsed;

    public TraversalNode(Stop stop) {
        this.stop = stop;
        this.g = Double.POSITIVE_INFINITY;
        this.h = Double.POSITIVE_INFINITY;
        updateF();
    }

    public void setDepartureTime(LocalTime time) { 
    	this.departureTime = time; 
    }

    
    public LocalTime getDepartureTime() { 
    	return departureTime; 
    }
    
    public Subline getSublineUsed() { 
    	return sublineUsed; 
    	}
    
    public void setSublineUsed(Subline subline) { 
    	this.sublineUsed = subline; 
    }
    
    public void updateF() {
        this.f = g + h;
    }
    
    public double getG() {
    	return g;
    }
    
    public double getF() {
    	return f;
    }
    
    public double getH() {
    	return h;
    }
    
    public void setG(double g) {
    	this.g = g;
    }
    
    
    public void setH(double h) {
    	this.h = h;
    }
    
    public Stop getStop() {
    	return stop;
    }
    
    public void setArrivalTime(LocalTime hour) {
    	this.arrivalTime = hour;
    }
    
    public LocalTime getArrivalTime() {
    	return arrivalTime;
    }
    public TraversalNode getCameFrom() {
    	return cameFrom;
    }
    
    public void setCameFrom(TraversalNode prevNode) {
    	this.cameFrom = prevNode;
    }
    
    public int compareTo(TraversalNode other) {
        return Double.compare(this.f, other.f);
    }
    
    /**
     * Réinitialise l'état du nœud pour permettre sa réutilisation.
     * Réinitialise les valeurs de coût (g, h, f), les horaires, la ligne utilisée,
     * ainsi que le pointeur vers le nœud précédent.
     * 
     * Cette méthode est utile après la fin de la recherche A* afin d'éviter la conservation
     * d'états intermédiaires et ne pas fausser les résultats des prochains appels de A*.
     */
    public void reset() {
        this.g = Double.POSITIVE_INFINITY;
        this.h = 0;
        this.f = Double.POSITIVE_INFINITY;
        this.cameFrom = null;
        this.arrivalTime = null;
        this.departureTime = null;
        this.sublineUsed = null;
    }
}

