package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.graph.Stop;
import java.time.LocalTime;

/**
 * Implémentation de {@link CostFunction} qui calcule le coût entre deux arrêts en fonction de la distance.
 * <p>
 * Cette fonction de coût est utilisée dans l'algorithme A* lorsqu'on souhaite privilégier un trajet plus court
 * en termes de distance parcourue, indépendamment de la durée de déplacement.
 */
public class DistanceCostFunction implements CostFunction {
    @Override
    public double costBetween(Stop from, Stop to, LocalTime departTime) {
        try {
            if (from == null || to == null) {
                throw new IllegalArgumentException("Stops cannot be null");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return Double.POSITIVE_INFINITY;
        }
        return from.getDistanceTo(to);
    }
}
