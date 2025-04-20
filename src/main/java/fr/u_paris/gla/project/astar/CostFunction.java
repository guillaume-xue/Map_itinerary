package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Stop;
import java.time.LocalTime;

public interface CostFunction {
	/**
     * Calcule le coût entre deux arrêts à une heure donnée.
     * 
     * @param from l'arrêt de départ
     * @param to l'arrêt d'arrivée
     * @param departTime l'heure de départ estimée
     * @return le coût (en distance, en durée, ou autre selon l'implémentation)
     */
    double costBetween(Stop from, Stop to, LocalTime departTime);
}


