/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import fr.u_paris.gla.project.utils.GPS;

/** A transport stop with unidentified name and potential candidates as to their
 * real value.
 * 
 * @author Emmanuel Bigeon */
public final class UnidentifiedStopEntry extends StopEntry {
    private List<StopEntry> candidates = new ArrayList<>();

    /** Create the stop
     * 
     * @param longitude
     * @param latitude */
    public UnidentifiedStopEntry(double longitude, double latitude) {
        super("Unidentified", longitude, latitude); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return MessageFormat.format("UnidentifiedStop [candidates={0}]", this.candidates); //$NON-NLS-1$
    }

    /** Get the most likely choice for the stop
     * 
     * @return the most likely candidate */
    public StopEntry resolve() {
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        Collections.sort(candidates, (Comparator<? super StopEntry>) (StopEntry s1,
                StopEntry s2) -> (int) Math.signum((GPS.distance(latitude, longitude,
                        s1.latitude, s1.longitude)
                        - GPS.distance(latitude, longitude, s2.latitude, s2.longitude))));

        return candidates.get(0);
    }

    /** Add a candidate.
     * 
     * @param entry the candidate */
    public void addCandidate(StopEntry entry) {
        candidates.add(entry);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        UnidentifiedStopEntry other = (UnidentifiedStopEntry) obj;
        return Objects.equals(candidates, other.candidates);
    }

}
