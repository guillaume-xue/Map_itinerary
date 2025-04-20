package fr.u_paris.gla.project.graph;

import java.util.ArrayList;
import java.util.Objects;

public class Line implements Comparable<Line>{
    
    private String id; // clef primaire
    private String name;
    private String type;
    private String color;
    private ArrayList<Subline> listOfSublines = new ArrayList<>();

    
    public Line(String id, String name, String type, String color){
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public void addSubline(Subline subline){
        listOfSublines.add(subline);
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public String getColor(){
        return color;
    }

    public ArrayList<Subline> getListOfSublines(){
        return listOfSublines;
    }

    public void setListOfSublines(ArrayList<Subline> newSublines){
        this.listOfSublines.clear();
        this.listOfSublines.addAll(newSublines);
    }

    @Override
    public int compareTo(Line other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return String.format("Line [id=%s, name=%s, type=%s, color=%s, sublines=%s]%n",
            id, name, type, color, listOfSublines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Line other = (Line) o;
        return 
            Objects.equals(id, other.id);
    }

}

