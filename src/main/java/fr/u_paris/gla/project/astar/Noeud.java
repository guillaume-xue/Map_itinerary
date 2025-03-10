package fr.u_paris.gla.project.astar;

public class Noeud {
    private int x;
    private int y;
    private int cout;
    private int heuristique;

    public Noeud(int x, int y, int cout, int heuristique){
        this.x = x;
        this.y = y;
        this.cout = cout;
        this.heuristique = heuristique;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCout() {
        return cout;
    }

    public int getHeuristique() {
        return heuristique;
    }

    @Override
    public String toString() {
        return "Noeud{" +
                "x=" + x +
                ", y=" + y +
                ", cout=" + cout +
                ", heuristique=" + heuristique +
                '}';
    }
}
