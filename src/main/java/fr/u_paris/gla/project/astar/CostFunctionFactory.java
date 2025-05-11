package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;  
import fr.u_paris.gla.project.astar.DistanceCostFunction;  
import fr.u_paris.gla.project.astar.DurationCostFunction;

/**
 * Fabrique de fonctions de coût pour l'algorithme A*.
 * <p>
 * Cette classe permet d'obtenir une implémentation de {@link CostFunction} en fonction du mode choisi :
 * soit basé sur la distance, soit sur la durée.
 */
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

