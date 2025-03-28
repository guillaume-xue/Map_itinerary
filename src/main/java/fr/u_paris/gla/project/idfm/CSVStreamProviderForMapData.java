
package fr.u_paris.gla.project.idfm;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;

import fr.u_paris.gla.project.io.UpgradedNetworkFormat;
import fr.u_paris.gla.project.utils.GPS;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.MessageFormat;

public class CSVStreamProviderForMapData implements Supplier<String[]> {

	private static final NumberFormat GPS_FORMATTER = UpgradedNetworkFormat
            .getGPSFormatter();
    private static final NumberFormat MINUTES_SECOND_FORMATTER = NumberFormat
            .getInstance(Locale.ENGLISH);
    static {
        MINUTES_SECOND_FORMATTER.setMinimumIntegerDigits(2);
    }
    /** Number of seconds in a minute. */
    private static final int  SECONDS_IN_MINUTES = 60;
    private static final long SECONDS_IN_HOURS   = 3_600;
    
    
    private final Iterator<Map.Entry<String, TraceEntry>> tracesIterator; // Premier itérateur
    private Iterator<Pair<StopEntry, StopEntry>> adjacentStopsIterator = Collections.emptyIterator(); // Deuxième itérateur

    private String currentLineName;
    private String currentLineType;
    private String currentLineColor;
    private String[] line = new String[UpgradedNetworkFormat.NUMBER_COLUMNS];
    
    
    public CSVStreamProviderForMapData(Map<String, TraceEntry> traces) {
        this.tracesIterator = traces.entrySet().iterator();
        advanceToNextValidTrace(); 
    }
    
    @Override
    public String[] get() {
        if (!adjacentStopsIterator.hasNext() && !advanceToNextValidTrace()) {
            return null;
        }
        
        Pair<StopEntry, StopEntry> pair = adjacentStopsIterator.next();
        StopEntry first = pair.getLeft();
        StopEntry second = pair.getRight();
        this.line[UpgradedNetworkFormat.LINE_INDEX] = currentLineName;
        this.line[UpgradedNetworkFormat.TYPE_INDEX] = currentLineType;
        this.line[UpgradedNetworkFormat.COLOR_INDEX] = currentLineColor;
        fillStation(first, this.line, UpgradedNetworkFormat.START_INDEX);
        fillStation(second, this.line, UpgradedNetworkFormat.STOP_INDEX);
        
        double distance = GPS.distance(first.latitude, first.longitude,
                second.latitude, second.longitude);
        this.line[UpgradedNetworkFormat.DISTANCE_INDEX] = NumberFormat.getInstance(Locale.ENGLISH)
                .format(distance);
        this.line[UpgradedNetworkFormat.DURATION_INDEX] = formatTime(
                (long) Math.ceil(distanceToTime(distance, this.currentLineType) * SECONDS_IN_HOURS));
        return line;
    }
    
    private boolean advanceToNextValidTrace() {
        while (tracesIterator.hasNext()) {
            Map.Entry<String, TraceEntry> traceEntry = tracesIterator.next();
            currentLineName = traceEntry.getValue().getLineName();
            currentLineType = traceEntry.getValue().getLineType(); 
            currentLineColor = traceEntry.getValue().getLineColor();
            adjacentStopsIterator = traceEntry.getValue().getStopPairs().iterator();

            if (adjacentStopsIterator.hasNext()) {
                return true; 
            }
        }
        return false; 
    }
    
    private static void fillStation(StopEntry stop, String[] nextLine, int index) {
        nextLine[index] = stop.getStopName();
        nextLine[index + 1] = MessageFormat.format("{0}, {1}", //$NON-NLS-1$
                GPS_FORMATTER.format(stop.latitude),
                GPS_FORMATTER.format(stop.longitude));
    }
    
    //retourne un temps en h
    private static double distanceToTime(double distance, String currentLineType) {
    	int speed; 

        switch (currentLineType) {
            case "Rail":
                speed = UpgradedNetworkFormat.RAIL_AVG_SPEED;
                break;
            case "Subway":
                speed = UpgradedNetworkFormat.SUBWAY_AVG_SPEED;
                break;
            case "Tram":
                speed = UpgradedNetworkFormat.TRAM_AVG_SPEED;
                break;
            case "Bus":
                speed = UpgradedNetworkFormat.BUS_AVG_SPEED;
                break;
            default:
                //throw new IllegalArgumentException("Type de ligne inconnu : " + this.currentLineType);
            	speed = UpgradedNetworkFormat.OTHER_AVG_SPEED;
            	break;
        }

        return distance / speed; 
    }
    
    private static String formatTime(long time) {
        return MessageFormat.format("{0}:{1}", //$NON-NLS-1$
                MINUTES_SECOND_FORMATTER.format(time / SECONDS_IN_MINUTES), MINUTES_SECOND_FORMATTER.format(time % SECONDS_IN_MINUTES));
    }
}
