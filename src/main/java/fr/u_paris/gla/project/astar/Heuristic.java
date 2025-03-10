package fr.u_paris.gla.project.astar;

import static java.lang.Math.abs;

public class Heuristic {
    private int x0;
    private int y0;
    private int x1;
    private int y1;
    public Heuristic(int x0, int y0, int x1, int y1){
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public int getX0() {
        return x0;
    }

    public int getY0() {
        return y0;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    // On prend la distance de Manhattan comme heuristique
    public int getHeuristic(){
        return abs(x1-x0) + abs(y1-y0);
    }

    @Override
    public String toString() {
        return "Heuristic{" +
                "x0=" + x0 +
                ", y0=" + y0 +
                ", x1=" + x1 +
                ", y1=" + y1 +
                '}';
    }
}
