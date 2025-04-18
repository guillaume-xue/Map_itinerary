package fr.u_paris.gla.project.astar;


import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;
import java.time.LocalTime;


public class TraversalNode implements Comparable<TraversalNode> {
    private Stop stop;
    private TraversalNode cameFrom = null;
    private double g;
    private double h;
    private double f;
    private LocalTime arrivalTime;
    private Subline currentSubline;

    public TraversalNode(Stop stop) {
        this.stop = stop;
        this.g = Double.POSITIVE_INFINITY;
        this.h = Double.POSITIVE_INFINITY;
        updateF();
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
}

