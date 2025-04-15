/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.text.MessageFormat;
import java.util.Objects;

//on pourra enlever le fait que ça implémente l'interface de comparaison

/** A transport stop data.
 * 
 * @author Emmanuel Bigeon */
public class StopEntry implements Comparable<StopEntry> {
    private final String stopName;
    private final String stopId;
    public final double longitude;
    public final double latitude;

    /** Create the stop
     * 
     * @param lname
     * @param longitude
     * @param latitude */
    public StopEntry(String stopName, String stopId, double longitude, double latitude) {
        super();
        this.stopName = stopName;
        this.stopId = stopId;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    public String getStopId() {
    	return this.stopId;
    }

    public String getStopName() {
    	return stopName;
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("{0}, {3} [{1}, {2}]", this.stopName, this.longitude, //$NON-NLS-1$
                this.latitude, this.stopId);
    }

    @Override
    public int compareTo(StopEntry o) {
        if (latitude < o.latitude) {
            return -1;
        }
        if (latitude > o.latitude) {
            return 1;
        }
        if (longitude < o.longitude) {
            return -1;
        }
        if (longitude > o.longitude) {
            return 1;
        }
        return stopName.compareTo(o.stopName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, stopName, stopId, longitude);
    }

    //on prend en compte stopId pour vérifier l'égalité de deux stops
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        StopEntry other = (StopEntry) obj;
        return Double.doubleToLongBits(latitude) == Double
                .doubleToLongBits(other.latitude) && Objects.equals(stopName, other.stopName)
                && Double.doubleToLongBits(longitude) == Double
                        .doubleToLongBits(other.longitude) && Objects.equals(stopId, other.stopId);
    }
}
