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

import fr.u_paris.gla.project.utils.Pair;
import fr.u_paris.gla.project.utils.GPS;
import fr.u_paris.gla.project.io.UpgradedNetworkFormat;
import fr.u_paris.gla.project.utils.TransportTypes;
import java.util.PriorityQueue;


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

    public Stop(double longitude, double latitude, String nameOfAssociatedStation){
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameOfAssociatedStation = nameOfAssociatedStation;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getNameOfAssociatedStation() {
        return nameOfAssociatedStation;
    }

    public HashMap<Subline, ArrayList<LocalTime>> getDepartures() {
        return departures;
    }

    /*public void addAdjacentStop(Stop adjacentStop, Duration timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(adjacentStop, Pair.of(timeToNextStation, distanceToNextStation));
    }*/

    public void addAdjacentStop(Stop adjacentStop, TransportTypes mode, Duration timeToNextStation, Float distanceToNextStation){
        timeDistancePerAdjacentStop.put(new Pair(adjacentStop, mode), new Pair(timeToNextStation, distanceToNextStation));
    }

    
    public void addDeparture(Subline subline, ArrayList<LocalTime> times) {
        departures.put(subline, new ArrayList<>(times)); // defensive copy
    }

    public void addDeparture(Subline subline, LocalTime time) {
        departures.computeIfAbsent(subline, k -> new ArrayList<>()).add(time);
    }

    public double distanceBetweenAdjacentStop(Stop stop) {
        return Math.abs(this.latitude - stop.latitude) + Math.abs(this.longitude - stop.longitude);
    }

    /*public ArrayList<Stop> getAdjacentStops() {
        return new ArrayList<>(timeDistancePerAdjacentStop.keySet());
    }

    public HashMap<Stop, Pair<Duration, Float>> getTimeDistancePerAdjacentStop(){
        return this.timeDistancePerAdjacentStop;
    }*/
    
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

    public HashMap<Pair<Stop, TransportTypes>, Pair<Duration, Float>> getTimeDistancePerAdjacentStop() {
        return this.timeDistancePerAdjacentStop;
    }
    
    /*public double getDistanceTo(Stop otherStop) {
        Pair<Duration, Float> data = timeDistancePerAdjacentStop.get(otherStop);
        if (data != null) {
            return data.getValue(); // Retourne la distance
        }
        return Double.POSITIVE_INFINITY; // Arrêt non voisin, retourner un coût infini
    }*/
    
    
    //retourne la distance avec le prochain stop en sachant que peut importe le type de transport la distance est calculée pareil
    public double getDistanceTo(Stop otherStop) {
        // Parcourir toutes les clés de la map pour vérifier si le Stop correspond à otherStop
        for (Map.Entry<Pair<Stop, TransportTypes>, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            // Si la clé contient otherStop, on récupère la distance
            if (entry.getKey().getKey().equals(otherStop)) {
                return entry.getValue().getValue(); 
            }
        }
        // Si on n'a pas trouvé la paire, retourner une distance infinie
        return Double.POSITIVE_INFINITY;
    }


    
    public Duration getTimeTo(Stop to, LocalTime departTime) {
    	ArrayList<Pair<Stop, LocalTime>> nextStops = this.giveNextStopsArrivalTime(departTime);

        for (Pair<Stop, LocalTime> pair : nextStops) {
            if (pair.getKey().equals(to)) {
                return Duration.between(departTime, pair.getValue()); // coût = durée
            }
        }
        return Duration.ofHours(9999);
    }

    //si il y a plusieurs façons d'attendre le prochain stop, renvoie la durée la plus courte
    public Duration getTimeTo(Stop to, LocalTime departTime) {
        // Récupérer la liste des prochains arrêts avec leur heure d'arrivée
        ArrayList<Triple<Stop, TransportTypes, LocalTime>> nextStops = this.giveNextStopsArrivalTime(departTime);
        Duration minDuration = Duration.ofHours(9999); // On initialise à une durée très longue

        // Parcourir tous les triplets dans nextStops
        for (Triple<Stop, TransportTypes, LocalTime> triple : nextStops) {
            if (triple.getLeft().equals(to)) {
                // Calculer la durée entre departTime et l'heure d'arrivée
                Duration duration = Duration.between(departTime, triple.getRight());
                
                // Comparer et garder la durée minimale
                if (duration.compareTo(minDuration) < 0) {
                    minDuration = duration;
                }
            }
        }
        return minDuration;
    }


    /*public void showTimeDistancePerAdjacentStop() {
    	System.out.println(this.nameOfAssociatedStation);
        for (Map.Entry<Stop, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            Stop adjacentStop = entry.getKey();
            Pair<Duration, Float> data = entry.getValue();

            Duration duration = data.getKey();
            Float distance = data.getValue();

            System.out.println(adjacentStop.getNameOfAssociatedStation() + " - Durée: " + 
            UpgradedNetworkFormat.formatDuration(duration) + " min - Distance: " + distance + " m");
        }
    }*/
    
    
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


    /*en disant qu'on est au stop this au moment departTime alors ça nous dis à quelle 
    heure on pourra et devra être au stop suivant
    cad que si c'est un trajet à pied on aura la plus petite heure du depart du prochain 
    train telle que departTime + tempsTransfert < heureRenvoyee
    et si c'est un trajet en train alors ça donne l'horaire auquel le prochain train qui 
    va à la station suivante arrivera là-bas
    */
    /*public ArrayList<Pair<Stop, LocalTime>> giveNextStopsArrivalTime(LocalTime departTime) {
    	ArrayList<Pair<Stop, LocalTime>> result = new ArrayList<>();
        Set<Stop> alreadyProcessed = new HashSet<>(); // Pour éviter les doublons

        // 1. Cas des trajets en train
        for (Map.Entry<Subline, ArrayList<LocalTime>> entry : departures.entrySet()) {
            Subline subline = entry.getKey();
            ArrayList<LocalTime> departureTimes = entry.getValue();
            ArrayList<Stop> stops = subline.getListOfStops();

            //on récupere les stops suivants depuis les sublines qui passent par le quai this
            for (int i = 0; i < stops.size() - 1; i++) {
                if (stops.get(i).equals(this)) {
                    Stop nextStop = stops.get(i + 1);
                    Pair<Duration, Float> timeDist = timeDistancePerAdjacentStop.get(nextStop);

                    if (timeDist != null) {
                        Duration travelTime = timeDist.getKey();

                        for (LocalTime departure : departureTimes) {
                        	//on ajoute à result le premier horaire qui remplit les conditions
                            if (!departure.isBefore(departTime.plus(travelTime))) {
                                result.add(Pair.of(nextStop, departure));
                                alreadyProcessed.add(nextStop);
                                break;
                            }
                        }
                    }
                    break; 
                }
            }
        }

        // 2. Cas des trajets à pied
        for (Map.Entry<Stop, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            Stop nextStop = entry.getKey();
            if (!alreadyProcessed.contains(nextStop)) {
                Duration travelTime = entry.getValue().getKey();
                LocalTime arrivalTime = departTime.plus(travelTime);
                result.add(Pair.of(nextStop, arrivalTime));
            }
        }

        return result;
    }*/
    
    public Stop findNextStopInSubline(Subline subline) {
    	ArrayList<Stop> stops = subline.getListOfStops();
    	for(int i = 0; i<stops.size() -1; i++) {
    		if (stops.get(i).equals(this)) {
    			return stops.get(i + 1);
    		}
    	}
    	return null;
    }
    
    public ArrayList<Triple<Stop, TransportTypes, LocalTime>> giveNextStopsArrivalTime(LocalTime departTime) {
        ArrayList<Triple<Stop, TransportTypes, LocalTime>> result = new ArrayList<>();
        Set<Pair<Stop, TransportTypes>> alreadyProcessed = new HashSet<>();

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
               			result.add(Triple.of(nextStop, mode, departure));
                        alreadyProcessed.add(key);
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

            if (mode == TransportTypes.Walk && !alreadyProcessed.contains(key)) {
                Duration travelTime = entry.getValue().getKey();
                LocalTime arrivalTime = departTime.plus(travelTime);
                result.add(Triple.of(nextStop, mode, arrivalTime));
            }
        }

        return result;
    }


    public void showNextStopsArrivalTime(LocalTime depart) {
        ArrayList<Triple<Stop, TransportTypes, LocalTime>> result = giveNextStopsArrivalTime(depart);
        for (Triple<Stop, TransportTypes, LocalTime> t : result) {
            System.out.println("Prochain stop : " + t.getLeft().getNameOfAssociatedStation()
                + " à " + t.getRight() + " via " + t.getMiddle().toString());
        }
    }

    
    @Override
    public String toString() {
        return String.format(
            "Stop [" +
            "name: '%s'," +
            " longitude: %.6f," +
            " latitude: %.6f" +
            " connecting stops : %s" +
            "]\n",
            nameOfAssociatedStation, longitude, latitude, getAllConnections()
        );    
    }

    public String getAllConnections(){
        ArrayList<String> temp = new ArrayList<>();
        timeDistancePerAdjacentStop.forEach((k,v) -> temp.add(k.getKey().getNameOfAssociatedStation()));
        return String.join(", ", temp);
    }

    

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, nameOfAssociatedStation);
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
        return GPS.distance(this.longitude, this.latitude, s.longitude, s.latitude);
    }
    
    //calculates the distance between a stop and the given coordinates
    public Double calculateDistance(double targetLongitude, double targetLatitude){
        return GPS.distance(this.longitude, this.latitude, targetLongitude, targetLatitude);
    }
}

