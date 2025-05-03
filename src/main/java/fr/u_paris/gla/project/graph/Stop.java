package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Collections;

import fr.u_paris.gla.project.utils.Pair;
import fr.u_paris.gla.project.utils.GPS;
import fr.u_paris.gla.project.io.UpgradedNetworkFormat;
import fr.u_paris.gla.project.utils.TransportTypes;
import java.util.PriorityQueue;
import org.apache.commons.lang3.tuple.Triple;


public class Stop {

    private double longitude;
    private double latitude;
    private String nameOfAssociatedStation;

    //A list of all adjacent stations, with the associated time and distance to get from current station to adjacent station. 
    //private HashMap<Stop, Pair<Duration, Float>> timeDistancePerAdjacentStop = new HashMap<>();
    private HashMap<Pair<Stop, TransportTypes >, Pair<Duration, Float>> timeDistancePerAdjacentStop = new HashMap<>();

    //For each subline that passes through this station, it should have an entry here
    /*if this station is not a departure station, 
    we will need to calculate at which time the trains would arrive into the station recursively from the departure station
    */
    private HashMap<Subline, ArrayList<LocalTime>> departures = new HashMap<>();

    public Stop(double latitude, double longitude, String nameOfAssociatedStation){
        this.latitude = latitude;
        this.longitude = longitude;

        this.nameOfAssociatedStation = nameOfAssociatedStation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNameOfAssociatedStation() {
        return nameOfAssociatedStation;
    }

    public HashMap<Subline, ArrayList<LocalTime>> getDepartures() {
        return departures;
    }

    public void addAdjacentStop(Stop adjacentStop, String mode, Duration timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(new Pair(adjacentStop, TransportTypes.valueOf(mode)), new Pair(timeToNextStation, distanceToNextStation));
    }
    
    public void addDeparture(Subline subline, ArrayList<LocalTime> times) {
        departures.put(subline, new ArrayList<>(times)); // defensive copy
    }

    public void addDeparture(Subline subline, LocalTime time) {
        departures.computeIfAbsent(subline, k -> new ArrayList<>()).add(time);
    }
    
    //à voir si cette fonction est encore utilisée qqpart
    public ArrayList<Stop> getAdjacentStops() {
        ArrayList<Stop> stops = new ArrayList<>();
        for (Pair<Stop, TransportTypes> key : timeDistancePerAdjacentStop.keySet()) {
            Stop stop = key.getKey();
            if (!stops.contains(stop)) { // Évite les doublons si un stop est accessible par plusieurs modes
                stops.add(stop);
            }
        }
        return stops;
    }

    public boolean hasAdjacentStop(Stop s){
        return timeDistancePerAdjacentStop.containsKey(s);
    }

    public HashMap<Pair<Stop, TransportTypes >, Pair<Duration, Float>> getTimeDistancePerAdjacentStop(){
        return timeDistancePerAdjacentStop;
    }
    
    
    //retourne la distance avec le prochain stop en sachant que peut importe le type de transport la distance est calculée pareil
    public double getDistanceTo(Stop otherStop) {
        for (Map.Entry<Pair<Stop, TransportTypes>, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            if (entry.getKey().getKey().equals(otherStop)) {
                return entry.getValue().getValue(); 
            }
        }
        return Double.POSITIVE_INFINITY;
    }


    public Duration getTimeTo(Stop to, LocalTime departTime) {
        ArrayList<Triple<Stop, Subline, LocalTime>> nextStops = this.giveNextStopsArrivalTime(departTime);
        Duration minDuration = Duration.ofHours(9999); 
        for (Triple<Stop, Subline, LocalTime> triple : nextStops) {
            if (triple.getLeft().equals(to)) {
                Duration duration = Duration.between(departTime, triple.getRight());
                if (duration.compareTo(minDuration) < 0) {
                    minDuration = duration;
                }
            }
        }
        return minDuration;
    }
    
    public void showTimeDistancePerAdjacentStop() {
        System.out.println(this.nameOfAssociatedStation);
        for (Map.Entry<Pair<Stop, TransportTypes>, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            Stop adjacentStop = entry.getKey().getKey();
            TransportTypes mode = entry.getKey().getValue();
            Pair<Duration, Float> data = entry.getValue();

            Duration duration = data.getKey();
            Float distance = data.getValue();

            System.out.println(adjacentStop.getNameOfAssociatedStation() + " (" + mode + ") - Durée: " + 
                UpgradedNetworkFormat.formatDuration(duration) + " min - Distance: " + distance + " m");
        }
    }
    
    public Stop findNextStopInSubline(Subline subline) {
    	ArrayList<Stop> stops = subline.getListOfStops();
    	for(int i = 0; i<stops.size() -1; i++) {
    		if (stops.get(i).equals(this)) {
    			return stops.get(i + 1);
    		}
    	}
    	return null;
    }
    
    /*en disant qu'on est au stop this au moment departTime alors ça nous dis à quelle 
    heure on pourra et devra être au stop suivant
    cad que si c'est un trajet à pied on aura la plus petite heure du depart du prochain 
    train telle que departTime + tempsTransfert < heureRenvoyee
    et si c'est un trajet en train alors ça donne l'horaire auquel le prochain train qui 
    va à la station suivante arrivera là-bas
    */
    public ArrayList<Triple<Stop, Subline, LocalTime>> giveNextStopsArrivalTime(LocalTime departTime) {
        ArrayList<Triple<Stop, Subline, LocalTime>> result = new ArrayList<>();
        Set<Pair<Stop, Subline>> alreadyProcessed = new HashSet<>();

        // 1. Cas des trajets véhiculés (rail, tram, subway, bus)
        for (Map.Entry<Subline, ArrayList<LocalTime>> entry : departures.entrySet()) {
            Subline subline = entry.getKey();
            ArrayList<LocalTime> departureTimes = entry.getValue();
            TransportTypes mode = subline.getSublineType(); 
            Stop nextStop = findNextStopInSubline(subline);
            if (nextStop == null) continue;
            Pair<Stop, TransportTypes> key = new Pair(nextStop, mode);
            Pair<Duration, Float> timeDist = timeDistancePerAdjacentStop.get(key);
            if (timeDist != null) {
            	Duration travelTime = timeDist.getKey();
                for (LocalTime departure : departureTimes) {
             		if (!departure.isBefore(departTime.plus(travelTime))) {
               			result.add(Triple.of(nextStop, subline, departure));
                        alreadyProcessed.add(new Pair(nextStop, subline));
                        break;
                    }
             	}
            }
        }

        // 2. Cas des trajets à pied
        for (Map.Entry<Pair<Stop, TransportTypes>, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            Pair<Stop, TransportTypes> key = entry.getKey();
            Stop nextStop = key.getKey();
            TransportTypes mode = key.getValue();
            if (mode == TransportTypes.Walk) {
                Duration travelTime = entry.getValue().getKey();
                LocalTime arrivalTime = departTime.plus(travelTime);
                Subline subline = this.getOrCreateWalkingSubline(nextStop);
                result.add(Triple.of(nextStop, subline, arrivalTime));
            }
        }

        return filterByEarliestArrivalTimePerStop(result);
       
    }

    
    //on ne garde que les triples tels que la subline est celle qui permet d'arriver le plus tôt au nextStop si pour nextStop on pouvait y arriver de différentes manières
    public static ArrayList<Triple<Stop, Subline, LocalTime>> filterByEarliestArrivalTimePerStop(
            ArrayList<Triple<Stop, Subline, LocalTime>> originalList) {

        Map<Stop, Triple<Stop, Subline, LocalTime>> bestByStop = new HashMap<>();

        for (Triple<Stop, Subline, LocalTime> triple : originalList) {
            Stop stop = triple.getLeft();
            LocalTime time = triple.getRight();

            if (!bestByStop.containsKey(stop) || time.isBefore(bestByStop.get(stop).getRight())) {
                bestByStop.put(stop, triple);
            }
        }

        return new ArrayList<>(bestByStop.values());
    }


    private Subline getOrCreateWalkingSubline(Stop neighborStop) {
        // Cherche si une sous-ligne existe déjà pour le trajet à pied
        for (Subline subline : Graph.WALK_LINE.getListOfSublines()) {
            if (subline.getStartStop().equals(this) && subline.getDestination().equals(neighborStop)) {
                return subline;
            }
        }
        // sinon on la crée avec constructeur spécial WALK_LINE
        Subline newWalkSubline = new Subline(this, neighborStop);
        Graph.WALK_LINE.addSubline(newWalkSubline);
        return newWalkSubline; 
    }

    public void showNextStopsArrivalTime(LocalTime depart) {
        ArrayList<Triple<Stop, Subline, LocalTime>> result = giveNextStopsArrivalTime(depart);
        for (Triple<Stop, Subline, LocalTime> t : result) {
            System.out.println("Prochain stop : " + t.getLeft().getNameOfAssociatedStation()
                + " à " + t.getRight() + " via " + t.getMiddle().getName());
        }
    }

    
    @Override
    public String toString() {
        return String.format(
            "Stop [" +
            "name: '%s'," +
            " latitude: %.6f," +
            " longitude: %.6f" +
            " connecting stops : %s" +
            "]\n",
            nameOfAssociatedStation, latitude, longitude, getAllConnections()
        );    
    }

    public String getAllConnections(){
        ArrayList<String> temp = new ArrayList<>();
        timeDistancePerAdjacentStop.forEach((k,v) -> temp.add(k.getKey().getNameOfAssociatedStation()));
        return String.join(", ", temp);
    }

    

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, nameOfAssociatedStation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Stop other = (Stop) o;
        return 
            Double.doubleToLongBits(latitude) == Double.doubleToLongBits(other.latitude) && 
            Objects.equals(nameOfAssociatedStation, other.nameOfAssociatedStation) && 
            Double.doubleToLongBits(longitude) == Double.doubleToLongBits(other.longitude);
    }

    //calculates the distance between two stops
    public Double calculateDistance(Stop s){
        return GPS.distance(this.latitude, this.longitude, s.latitude, s.longitude);
    }
    
    //calculates the distance between a stop and the given coordinates
    public Double calculateDistance(double targetLatitude, double targetLongitude){
        return GPS.distance(this.latitude, this.longitude, targetLatitude, targetLongitude);
    }
    
}

