package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.astar.CostFunction;
import fr.u_paris.gla.project.graph.Stop;
import java.time.LocalTime;

/**
 * Implémentation de l'interface {@link CostFunction} qui évalue le coût entre deux arrêts
 * en fonction de la durée du trajet (en secondes).
 * <p>
 * Cette classe est utilisée par l'algorithme A* pour calculer les chemins les plus courts
 * en minimisant la durée totale de trajet entre un point de départ et une destination.
 */
public class DurationCostFunction implements CostFunction {
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
        return from.getTimeTo(to, departTime).toSeconds();
    }
}
