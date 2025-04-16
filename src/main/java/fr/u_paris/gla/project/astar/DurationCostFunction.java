package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.graph.Stop;

public class DurationCostFunction implements CostFunction {
    @Override
    public double costBetween(Stop from, Stop to) {
        try {
            if (from == null || to == null) {
                throw new IllegalArgumentException("Stops cannot be null");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return Double.POSITIVE_INFINITY;
        }
        return from.getTimeTo(to).toSeconds(); 
    }
}
