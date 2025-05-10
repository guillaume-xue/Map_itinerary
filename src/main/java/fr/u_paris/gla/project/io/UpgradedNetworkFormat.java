
package fr.u_paris.gla.project.io;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;


/** 
 * A tool class for the network format. 
 */
public class UpgradedNetworkFormat {

	public static final int NUMBER_COLUMNS = 10;
    public static final int GPS_PRECISION  = 18;
    
    //vitesses des différents type de transport, en km/h
    public static final int RAIL_AVG_SPEED = 54;
    public static final int BUS_AVG_SPEED = 8;
    public static final int SUBWAY_AVG_SPEED = 22;
    public static final int TRAM_AVG_SPEED = 18;
    public static final int WALK_AVG_SPEED = 4;

    public static final int OTHER_AVG_SPEED = 10;
	
    public static final int LINE_ID_INDEX   = 0;
    /** The index of the line name in the network format */
    public static final int LINE_NAME_INDEX = 1;
    /** The index of the type of the line of the segment in the network format */
    public static final int TYPE_INDEX      = 2;
    /** The index of the color of the transport line*/
    public static final int COLOR_INDEX     = 3;
    /** The index of the segment starting stop name in the network format */
    public static final int START_INDEX     = 4;
    /** The index of the segment end stop name in the network format */
    public static final int STOP_INDEX      = 6;
    /** The index of the segment trip duration in the network format */
    public static final int DURATION_INDEX  = 8;
    /** The index of the segment distance in the network format */
    public static final int DISTANCE_INDEX  = 9;
    
    private static final DateTimeFormatter DURATION_FORMATTER         = DateTimeFormatter
            .ofPattern("HH:mm:ss");
    private static final NumberFormat      DURATION_SECONDS_FORMATTER = NumberFormat
            .getIntegerInstance(Locale.ENGLISH);
    static {
        DURATION_SECONDS_FORMATTER.setMinimumIntegerDigits(2);
    }
    private static final Temporal ZERO = LocalTime.parse("00:00:00");
    
    /** Hidden constructor for utility class */
    private UpgradedNetworkFormat() {
        // Tool class
    }

    /**
     * Parses a string duration.
     *
     * @param      duration  The string duration
     *
     * @return     The duration
     */
    public static Duration parseDuration(String duration) {
        LocalTime time = LocalTime.parse("00:" + duration, DURATION_FORMATTER);
        return Duration.between(time, ZERO);
    }

    /**
     * Parses a string duration, which has more than 2 digits.
     *
     * @param      duration  The string duration
     *
     * @return     The duration
     */
    public static Duration parseLargeDuration(String duration) {
        String[] parts = duration.split(":");
        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[1]);
        return Duration.ofMinutes(minutes).plusSeconds(seconds);
    }

    /**
     * Formats a duration to a string.
     *
     * @param      duration  The duration
     *
     * @return     The string duration
     */
    public static String formatDuration(Duration duration) {
        return Long.toString(duration.toMinutes()) + ":"
                + DURATION_SECONDS_FORMATTER.format(duration.toSecondsPart());
    }

    /** Get a formatter for the numbers in a GPS coordinate pair
     * 
     * @return the formatter for numbers in a GPS coordinate pair */
    public static NumberFormat getGPSFormatter() {
        NumberFormat instance = NumberFormat.getNumberInstance(Locale.ENGLISH);
        instance.setMaximumFractionDigits(GPS_PRECISION);
        return instance;
    }

    
}
