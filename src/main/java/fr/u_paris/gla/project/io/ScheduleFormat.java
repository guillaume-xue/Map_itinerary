/**
 * 
 */
package fr.u_paris.gla.project.io;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.ArrayList;

/** A tool class for the schedule format.
 * 
 */
public final class ScheduleFormat {
	
	public static final int NUMBER_COLUMNS = 4;
	
    public static final int LINE_ID_INDEX       = 0;
    public static final int TRIP_SEQUENCE_INDEX = 1;
    public static final int TERMINUS_INDEX      = 2;
    public static final int TIME_INDEX          = 3;

    /** Hidden constructor for tool class */
    private ScheduleFormat() {
        // Tool class
    }
    
}
