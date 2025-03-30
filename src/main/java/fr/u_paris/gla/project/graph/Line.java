package fr.u_paris.gla.project.graph;

import java.util.ArrayList;
import java.util.Objects;

public class Line implements Comparable<Line>{
    
    private String name;
    private String type;
    private ArrayList<Subline> listOfSublines = new ArrayList<>();

    
    public Line(String name){
        this.name = name;
    }

    public Line(String name, ArrayList<Subline> listOfSublines ){
        String[] tmp = name.split("_");
        this.name = tmp[0];
        this.type = tmp[1];
        this.listOfSublines = listOfSublines;
    }

    public void addSubline(Subline subline){
        listOfSublines.add(subline);
    }

    public String getName() {
        return name;
    }

    public String getType(){
        return type;
    }

    public ArrayList<Subline> getListOfSublines() {
        return listOfSublines;
    }

    @Override
    public int compareTo(Line o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Line [name=" + name + ", listOfSublines=" + listOfSublines + "]\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Line other = (Line) obj;
        return name.equals(other.name);
    }

}

