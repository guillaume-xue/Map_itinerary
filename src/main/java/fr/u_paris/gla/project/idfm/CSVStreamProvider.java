/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fr.u_paris.gla.project.io.NetworkFormat;
import fr.u_paris.gla.project.utils.GPS;

public final class CSVStreamProvider {
    private static final NumberFormat GPS_FORMATTER            = NetworkFormat
            .getGPSFormatter();
    private static final NumberFormat MINUTES_SECOND_FORMATTER = NumberFormat
            .getInstance(Locale.ENGLISH);
    static {
        MINUTES_SECOND_FORMATTER.setMinimumIntegerDigits(2);
    }
    /** Number of seconds in a minute. */
    private static final int  SECONDS_IN_MINUTES = 60;
    private static final long SECONDS_IN_HOURS   = 3_600;
    // Magically chosen values
    /** Maximal speed in km/h */
    private static final double MAX_SPEED             = 5;
    /** Distance to reach maximal speed in km */
    private static final double TWO_ACCELERATION_DISTANCE = 0.2;

    private String[] line = new String[NetworkFormat.NUMBER_COLUMNS];

    private Iterator<TraceEntry>      currentTrace;
    private Iterator<List<StopEntry>> currentPath         = Collections.emptyIterator();
    private Iterator<StopEntry>       currentSegmentStart = Collections.emptyIterator();
    private Iterator<StopEntry>       currentSegmentEnd   = Collections.emptyIterator();
    Map<StopEntry, Set<StopEntry>>    lineSegments        = new HashMap<>();

    private StopEntry start = null;
    private StopEntry end   = null;

    private boolean hasNext = false;
    private boolean onNext  = false;

    /** Create the stream provider */
    public CSVStreamProvider(Iterator<TraceEntry> traces) {
        this.currentTrace = traces;
    }

    public boolean hasNext() {
        if (!this.onNext) {
            skipToNext();
        }
        return this.hasNext;
    }

    private void skipToNext() {
        if (this.onNext) {
            return;
        }
        while (!this.onNext) {
            if (!this.currentSegmentEnd.hasNext()) {
                skipToNextCandidatePath();
            }
            if (this.onNext) {
                return;
            }
            skipToNextNewSegment();
        }
    }

    private void skipToNextNewSegment() {
        do {
            this.start = this.currentSegmentStart.next();
            this.lineSegments.putIfAbsent(this.start, new HashSet<>());
            this.end = this.currentSegmentEnd.next();
        } while (this.lineSegments.get(this.start).contains(this.end)
                && this.currentSegmentEnd.hasNext());
        if (!this.lineSegments.get(this.start).contains(this.end)) {
            this.lineSegments.get(this.start).add(this.end);
            this.onNext = true;
            this.hasNext = true;
        }
    }

    /** Move the reading head of path to the next one that has at least two
     * elements */
    private void skipToNextCandidatePath() {
        currentSegmentStart = null;
        do {
            while (!this.currentPath.hasNext()) {
                if (!this.currentTrace.hasNext()) {
                    this.hasNext = false;
                    this.onNext = true;
                    return;
                }
                TraceEntry trace = this.currentTrace.next();
                this.currentPath = trace.getPaths().iterator();
                this.line[NetworkFormat.LINE_INDEX] = trace.lname;
                this.lineSegments.clear();
            }
            List<StopEntry> path = this.currentPath.next();
            this.currentSegmentEnd = path.iterator();
            if (this.currentSegmentEnd.hasNext()) {
                this.currentSegmentEnd.next();
                this.currentSegmentStart = path.iterator();
            }
        } while (currentSegmentStart == null);
    }

    public String[] next() {
        if (!this.onNext) {
            skipToNext();
        }
        this.onNext = false;

        fillStation(this.start, this.line, NetworkFormat.START_INDEX);
        fillStation(this.end, this.line, NetworkFormat.STOP_INDEX);
        double distance = GPS.distance(this.start.latitude, this.start.longitude,
                this.end.latitude, this.end.longitude);
        this.line[NetworkFormat.DISTANCE_INDEX] = NumberFormat.getInstance(Locale.ENGLISH)
                .format(distance);
        this.line[NetworkFormat.DURATION_INDEX] = formatTime(
                (long) Math.ceil(distanceToTime(distance) * SECONDS_IN_HOURS));
        this.line[NetworkFormat.VARIANT_INDEX] = Integer
                .toString(this.lineSegments.get(this.start).size() - 1);

        return Arrays.copyOf(this.line, this.line.length);
    }

    /** @param stop1
     * @param nextLine
     * @param i */
    private static void fillStation(StopEntry stop, String[] nextLine, int index) {
        nextLine[index] = stop.lname;
        nextLine[index + 1] = MessageFormat.format("{0}, {1}", //$NON-NLS-1$
                GPS_FORMATTER.format(stop.latitude),
                GPS_FORMATTER.format(stop.longitude));

    }

    /** @param distanceToTime
     * @return */
    private static String formatTime(long time) {
        return MessageFormat.format("{0}:{1}", //$NON-NLS-1$
                MINUTES_SECOND_FORMATTER.format(time / SECONDS_IN_MINUTES), MINUTES_SECOND_FORMATTER.format(time % SECONDS_IN_MINUTES));
    }

    /** A tool method to give a delay to go through a certain distance.
     * <p>
     * This is a model with an linear acceleration and deceleration periods and a
     * constant speed in between.
     * 
     * @param distance the distance (in km)
     * @return the duration of the trip (in hours) */
    private static double distanceToTime(double distance) {
        return Math.max(0, distance - TWO_ACCELERATION_DISTANCE) / MAX_SPEED
                + Math.pow(Math.min(distance, TWO_ACCELERATION_DISTANCE) / MAX_SPEED, 2);
    }

}
