package fr.u_paris.gla.project.graph;

import java.util.ArrayList;
import java.util.Objects;

public class Line implements Comparable<Line>{
    
    private String name;
    private String type;
    private String color;
    private ArrayList<Subline> listOfSublines = new ArrayList<>();

    
    public Line(String name, String type, String color){
        this.name = name;
        this.type = type;
        this.color = color;
    }

    /**
     * Constructs a line when only one string is provided, expects the string format to be:
     * "name_type_color"
     *
     * @param      input  The string.
     * @throws     IllegalArgumentException if the format is invalid.
     * 
     */
    public Line(String input){
        String[] tmp = input.split("_");
        if (tmp.length != 3) {
            throw new IllegalArgumentException(
                "Input string must be in the format 'name_type_color'. Got: " + input
            );
        }
        this.name = tmp[0];
        this.type = tmp[1];
        this.color = tmp[2];
    }

    public Line(String name, ArrayList<Subline> listOfSublines ){
        String[] tmp = name.split("_");
        this.name = tmp[0];
        this.type = tmp[1];
        this.color = tmp[2];
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

    public String getColor(){
        return color;
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
        return "Line [name=" + name + ", color=" + color + ", listOfSublines=" + listOfSublines + "]\n";
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
        return 
            Objects.equals(name, other.name) && 
            Objects.equals(type, other.type) &&
            Objects.equals(color, other.type);
    }

}

