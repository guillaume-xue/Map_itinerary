package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Stop;

public interface CostFunction {
	double costBetween(Stop from, Stop to);
}


