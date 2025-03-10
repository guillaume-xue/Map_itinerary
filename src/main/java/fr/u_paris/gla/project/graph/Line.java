package fr.u_paris.gla.project.graph;

import java.util.ArrayList;

public class Line {
    
    private String name;
    private ArrayList<Subline> listOfSublines = new ArrayList<>();
    
    public Line(String name){
        this.name = name;
    }

    public void addSubline(Subline subline){
        listOfSublines.add(subline);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Subline> getListOfSublines() {
        return listOfSublines;
    }

    @Override
    public String toString() {
        return "Line [name=" + name + ", listOfSublines=" + listOfSublines + "]";
    }


}

