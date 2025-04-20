package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;  
import fr.u_paris.gla.project.astar.DistanceCostFunction;  
import fr.u_paris.gla.project.astar.DurationCostFunction;

public class CostFunctionFactory {
    public enum Mode {
        DISTANCE,
        DURATION
    }

    public static CostFunction getCostFunction(Mode mode) {
        return switch (mode) {
            case DURATION -> new DurationCostFunction();
            case DISTANCE -> new DistanceCostFunction();
        };
    }
}

