package fr.u_paris.gla.project.graph;

import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

/**
 * Représente un arrêt dans un réseau de transport, avec sa position géographique,
 * les horaires de passage pour chaque sous-ligne qui y passe, et les connexions avec les arrêts adjacents.
 */
public class Stop {

    private double longitude;
    private double latitude;
    private String nameOfAssociatedStation;

    // Connexions avec les arrêts adjacents, associées à un mode de transport,
    // une durée de trajet et une distance.
    private HashMap<Pair<Stop, TransportTypes >, Pair<Duration, Float>> timeDistancePerAdjacentStop = new HashMap<>();

    // Pour chaque sous-ligne passant par cet arrêt, une liste des horaires de passage
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
    
    /**
     * Calcule la distance vers un autre arrêt (peu importe le mode).
     */
    public double getDistanceTo(Stop otherStop) {
        for (Map.Entry<Pair<Stop, TransportTypes>, Pair<Duration, Float>> entry : timeDistancePerAdjacentStop.entrySet()) {
            if (entry.getKey().getKey().equals(otherStop)) {
                return entry.getValue().getValue(); 
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Calcule la durée minimale pour atteindre un autre arrêt à partir d'une heure de départ donnée.
     *
     * @param to          Arrêt cible.
     * @param departTime  Heure de départ.
     * @return Durée minimale de trajet vers l'arrêt.
     */
    public Duration getTimeTo(Stop to, LocalTime departTime) {
        ArrayList<Triple<Stop, Subline, LocalTime>> nextStops = this.giveNextStopsArrivalTime(departTime);
        Duration minDuration = Duration.ofHours(9999); 
        for (Triple<Stop, Subline, LocalTime> triple : nextStops) {
            if (triple.getLeft().equals(to)) {
                Duration duration = durationWithMidnightWrap(departTime, triple.getRight());
                if (duration.compareTo(minDuration) < 0) {
                    minDuration = duration;
                }
            }
        }
        return minDuration;
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
    
    /**
     * Calcule l'heure du prochain départ (le plus proche après l'heure de référence).
     *
     * @param referenceTime Heure actuelle.
     * @param departures    Liste des horaires de départ.
     * @return Prochain horaire ou null si aucun.
     */
    public static LocalTime veryNextDeparture(LocalTime referenceTime, ArrayList<LocalTime> departures) {
        if (departures == null || departures.isEmpty()) return null;
        Duration minPositiveDuration = durationWithMidnightWrap(referenceTime, departures.get(0));
        LocalTime bestCandidate = departures.get(0);

        for (LocalTime time : departures) {
            Duration d = durationWithMidnightWrap(referenceTime, time);
            if (d.compareTo(minPositiveDuration) < 0) {
            	minPositiveDuration = d;
                bestCandidate = time;
            }
        }
        return bestCandidate;
    }

    
    /**
     * Détermine l'heure de départ effective à utiliser depuis cet arrêt avec une sous-ligne donnée.
     * Si c’est un transport, renvoie le prochain départ de la sous-ligne, 
     * sinon renvoie simplement l’heure de départ du segment à faire à pied.
     *
     * @param departureTime Heure de départ actuelle.
     * @param usedSubline   Sous-ligne empruntée.
     * @return Heure de départ effective.
     */
    public LocalTime giveDepartureTimeFromUsedSubline(LocalTime departureTime, Subline usedSubline) {
    	ArrayList<LocalTime> horaires = departures.get(usedSubline);
    	LocalTime next = veryNextDeparture(departureTime, horaires);
        return (next != null) ? next : departureTime;
    }

    
    /**
     * Donne la liste des arrêts suivants atteignables depuis cet arrêt en partant à une heure donnée,
     * avec l’heure estimée d’arrivée en fonction des prochains horaires de passage des sous-lignes.
     * Applique un filtre pour ne garder que les trajets les plus rapides par couple d'arrêts.
     *
     * @param departTime Heure de départ actuelle.
     * @return Liste de triples (arrêt suivant, sous-ligne, heure d’arrivée).
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
            	LocalTime departure = veryNextDeparture(departTime, departureTimes);
            	result.add(Triple.of(nextStop, subline, departure.plus(travelTime)));
                alreadyProcessed.add(new Pair(nextStop, subline));
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
        return filterByEarliestArrivalTimePerStop(result, departTime);  
    }

    /**
     * Calcule la durée entre deux horaires, en tenant compte du passage à minuit.
     *
     * @param t1 Heure de départ.
     * @param t2 Heure d’arrivée.
     * @return Durée positive même si t2 est avant t1.
     */
    public static Duration durationWithMidnightWrap(LocalTime t1, LocalTime t2) {
        if (t2.isBefore(t1)) {
            return Duration.between(t1, t2).plusHours(24); //on ajoute 24h à la durée négative pr simuler le fait d'etre le lendemain
        } else {
            return Duration.between(t1, t2);
        }
    }
    
    
    /**
     * Filtre une liste de triples (Stop, Subline, LocalTime) pour ne conserver, pour chaque {@code Stop},
     * que celui permettant une arrivée la plus proche (mais après) du {@code departTime} donné.
     * <p>
     * Cette méthode est utile lorsqu'un arrêt peut être atteint par plusieurs sous-lignes,
     * et qu'on souhaite ne conserver que le trajet permettant d'y arriver le plus tôt (en tenant compte
     * d’un éventuel passage à minuit).
     *
     * @param originalList La liste des triples à filtrer, où chaque triple contient un arrêt, une sous-ligne,
     *                     et une heure d'arrivée potentielle à cet arrêt.
     * @param departTime   L'heure de départ de référence, utilisée pour calculer le délai jusqu'à chaque heure d'arrivée.
     * @return Une nouvelle liste ne contenant, pour chaque arrêt, que le triple avec le plus court délai depuis {@code departTime}.
     */
    public static ArrayList<Triple<Stop, Subline, LocalTime>> filterByEarliestArrivalTimePerStop(
            ArrayList<Triple<Stop, Subline, LocalTime>> originalList, LocalTime departTime) {

        Map<Stop, Triple<Stop, Subline, LocalTime>> bestByStop = new HashMap<>();

        for (Triple<Stop, Subline, LocalTime> triple : originalList) {
            Stop stop = triple.getLeft();
            LocalTime arrivalTime = triple.getRight();
            Duration arrivalDelay = durationWithMidnightWrap(departTime, arrivalTime);
            if (!bestByStop.containsKey(stop)) {
                bestByStop.put(stop, triple);
            } else {
                LocalTime currentBestTime = bestByStop.get(stop).getRight();
                Duration currentDelay = durationWithMidnightWrap(departTime, currentBestTime);

                if (arrivalDelay.minus(currentDelay).isNegative()) {
                    bestByStop.put(stop, triple);
                }
            }
        }
        return new ArrayList<>(bestByStop.values());
    }


    /**
     * Récupère une sous-ligne de marche existante entre {@code this} et {@code neighborStop}, 
     * ou en crée une nouvelle si elle n'existe pas.
     *
     * @param neighborStop L'arrêt voisin vers lequel une sous-ligne de marche est recherchée ou créée.
     * @return Une sous-ligne de type {@code Walk} entre {@code this} et {@code neighborStop}.
     */
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

