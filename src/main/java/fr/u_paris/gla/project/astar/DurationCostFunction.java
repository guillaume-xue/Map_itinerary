package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.graph.Stop;

public class DurationCostFunction implements CostFunction {
    @Override
    public double costBetween(Stop from, Stop to) {
        return from.getTimeTo(to).toSeconds(); 
    }
}
