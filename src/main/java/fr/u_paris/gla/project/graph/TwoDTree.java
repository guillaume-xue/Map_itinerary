package fr.u_paris.gla.project.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A 2DTree implementation for a faster spatial search ( for connectStopsByWalking )
 * 
 * Class was written in the last week of development hence why it's only used for connecting stops
 * by walking, which isn't even an useful functionnality since itinerary searches are same or worse 
 * and the algorithm takes exponentially more time to find a result.
 *  
 */
public class TwoDTree {

    /**
     * Internal class, represents a node of the tree. 
     */
    public class TwoDNode{
        Stop stop;
        TwoDNode left, right;
        int axis; // 0 = latitude, 1 = longitude

        public TwoDNode(Stop stop, int axis){
            this.stop = stop;
            this.axis = axis;
        }
    }

    private TwoDNode root;

    /**
     * Constructs a 2DTree using the listOfStops of the graph.
     *
     * @param      stops  The list of stops.
     */
    public TwoDTree(List<Stop> stops) {
        this.root = build(stops, 0);
    }

    /**
     * Recursive builder for the 2DTtree.
     *
     * @param      stops  The stops
     * @param      depth  The depth
     *
     * @return     The root node of the instance of the tree
     */
    private TwoDNode build(List<Stop> stops, int depth) {
        if (stops.isEmpty()) return null;

        int axis = depth % 2;
        stops.sort(Comparator.comparingDouble(s -> axis == 0 ? s.getLatitude() : s.getLongitude()));
        int median = stops.size() / 2;

        TwoDNode node = new TwoDNode(stops.get(median), axis);
        node.left = build(stops.subList(0, median), depth + 1);
        node.right = build(stops.subList(median + 1, stops.size()), depth + 1);

        return node;
    }

    /**
     * Finds all the stops within the maxDistanceKm radius.
     *
     * @param      latitude       The latitude
     * @param      longitude      The longitude
     * @param      maxDistanceKm  The maximum distance kilometers
     *
     * @return     The list of stops in the radius.
     */
    public List<Stop> radiusSearch(double latitude, double longitude, double maxDistanceKm) {
        List<Stop> result = new ArrayList<>();
        searchRecursive(root, latitude, longitude, maxDistanceKm, result);
        return result;
    }

    /**
     * Recursive search used in radius search.
     *
     * @param      node           The root node
     * @param      latitude       The latitude
     * @param      longitude      The longitude
     * @param      maxDistanceKm  The maximum distance kilometers
     * @param      result         The result list to add the stops
     */
    private void searchRecursive(TwoDNode node, double latitude, double longitude, double maxDistanceKm, List<Stop> result) {
        if (node == null) return;

        double d = node.stop.calculateDistance(latitude, longitude);
        if (d <= maxDistanceKm) {
            result.add(node.stop);
        }

        int axis = node.axis;
        double pointCoord = axis == 0 ? latitude : longitude;
        double nodeCoord = axis == 0 ? node.stop.getLatitude() : node.stop.getLongitude();

        if (pointCoord - maxDistanceKm <= nodeCoord) {
            searchRecursive(node.left, latitude, longitude, maxDistanceKm, result);
        }
        if (pointCoord + maxDistanceKm >= nodeCoord) {
            searchRecursive(node.right, latitude, longitude, maxDistanceKm, result);
        }
    }
}