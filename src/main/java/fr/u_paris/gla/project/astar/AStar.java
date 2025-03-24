package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.Graph;
import fr.u_paris.gla.project.graph.Line;
import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;

import java.util.*;


public class AStar {

    private HashMap<Stop, Double> gScore = new HashMap<>();
    private HashMap<Stop, Double> fScore = new HashMap<>();
    private Graph graph;
    public AStar(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Line> astar(Graph graph, Stop startStop, Stop finishStop) {
        HashMap<Stop, Stop> cameFrom = new HashMap<>();

        PriorityQueue<Stop> openSet = new PriorityQueue<>(Comparator.comparingDouble(s -> fScore.getOrDefault(s, Double.MAX_VALUE)));

        ArrayList<Subline> sublines = new ArrayList();
        ArrayList<Line> listOfLines = new ArrayList();
        ArrayList<Stop> listOfStops = new ArrayList();

        // Lignes de Train (RER et transilien) :

        listOfLines.add(new Line("RER A"));
        listOfLines.add(new Line("RER B"));
        listOfLines.add(new Line("RER C"));
        listOfLines.add(new Line("RER D"));
        listOfLines.add(new Line("RER E"));

        listOfLines.add(new Line("Ligne H"));
        listOfLines.add(new Line("Ligne J"));
        listOfLines.add(new Line("Ligne K"));
        listOfLines.add(new Line("Ligne L"));
        listOfLines.add(new Line("Ligne N"));
        listOfLines.add(new Line("Ligne P"));
        listOfLines.add(new Line("Ligne R"));
        listOfLines.add(new Line("Ligne U"));



        // Lignes de Métro :

        for (int i=1; i<=14; i++){
            if(i == 3 || i == 7){
                listOfLines.add(new Line("Métro " + String.valueOf(i)));
                listOfLines.add(new Line("Métro " + String.valueOf(i) + "bis"));
            }
            else{
                listOfLines.add(new Line("Métro " + String.valueOf(i)));
            }
        }

        // Lignes de Tramway :

        for (int i=1; i<=14; i++){
            if(i == 3){
                listOfLines.add(new Line("T" + String.valueOf(i) + "a"));
                listOfLines.add(new Line("T" + String.valueOf(i) + "b"));
            }
            else{
                listOfLines.add(new Line("T" + String.valueOf(i)));
            }
        }

        listOfStops.add(new Stop(0F,0F,"Gare du Nord"));

        gScore.put(startStop, 0.0);
        fScore.put(startStop, (double) startStop.distanceBetweenTwoStops(startStop, finishStop));
        openSet.add(startStop);

        while (!openSet.isEmpty()) {
            Stop current = openSet.poll();

            //if (current.equals(finishStop)) {
                //return reconstructPath(cameFrom, current);
            //}

            for (Line line : listOfLines) {
                for (Subline subline : sublines) {
                    if (startStop.equals(current)) {
                        Stop neighbor = finishStop;
                        double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + heuristic(startStop, finishStop);

                        if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                            cameFrom.put(neighbor, current);
                            gScore.put(neighbor, tentativeGScore);
                            fScore.put(neighbor, tentativeGScore + neighbor.distanceBetweenTwoStops(neighbor,finishStop));
                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }

        for (Line line: listOfLines){
            System.out.println(line.toString());
        }

        return listOfLines;
    }

    private boolean isValid(int[][] grid, int longitude, int latitude, HashSet<String> closedList) {
        return longitude >= 0 && longitude < grid.length && latitude >= 0 && latitude < grid[0].length
                && grid[longitude][latitude] == 0 && !closedList.contains(longitude + "," + latitude);
    }

    private ArrayList<Stop> reconstructPath(HashMap<Stop, Stop> cameFrom, Stop current) {
        ArrayList<Stop> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        path.add(current);
        Collections.reverse(path);
        return path;
    }

    public int heuristic(Stop startStop, Stop finishStop) {
        float x1 = startStop.getGpsCoordX();
        float y1 = startStop.getGpsCoordY();
        float x2 = finishStop.getGpsCoordX();
        float y2 = finishStop.getGpsCoordX();
        return (int) (Math.abs(x1 - x2) + Math.abs(y1 - y2)); // Distance de Manhattan
    }



    public static void main(String[] args){

        ArrayList<Subline> sublines = new ArrayList();
        ArrayList<Stop> listOfStops = new ArrayList();
        ArrayList<Line> listOfLines = new ArrayList();
        Graph graph = new Graph(listOfStops, listOfLines);

        AStar aStar = new AStar(graph);

        Stop stop1 = new Stop(0F,0F, "Gare du Nord");
        Stop stop2 = new Stop(0F,0F, "Bourg-la-Reine");

        aStar.astar(graph, stop1, stop2);


        /*int[][] grid = {
                {0, 1, 0, 0, 0},
                {0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0},
                {1, 1, 0, 0, 0},
                {0, 0, 0, 1, 0}
        };
        int startLongitude = 0, startLatitude = 0, goalLongitude = 4, goalLatitude = 4;

        List<Stop> path = aStar(grid, startLongitude, startLatitude, goalLongitude, goalLatitude);
        if (path.isEmpty()) {
            System.out.println("Aucun chemin trouvé.");
        } else {
            for (Stop node : path) {
                System.out.println("(" + node.longitude + ", " + node.latitude + ")");
            }
        }*/
    }

}
