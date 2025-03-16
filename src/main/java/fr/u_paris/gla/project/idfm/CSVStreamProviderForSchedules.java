package fr.u_paris.gla.project.idfm;


import fr.u_paris.gla.project.io.ScheduleFormat;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.Objects;

public class CSVStreamProviderForSchedules implements Supplier<String[]> {

    private final Iterator<String> timeIterator;
    private final String lineName;
    private final String stopName;
    private String[] line = new String[ScheduleFormat.NUMBER_COLUMNS];

    public CSVStreamProviderForSchedules(Iterator<String> timeIterator, String lineName, String stopName) {
        this.timeIterator = timeIterator;
        this.lineName = lineName;
        this.stopName = stopName;
    }


    @Override
    public String[] get() {
    	if (timeIterator.hasNext()) {
    		String heurePassage = timeIterator.next();
    		this.line[ScheduleFormat.LINE_INDEX] = lineName;
    		this.line[ScheduleFormat.TRIP_SEQUENCE_INDEX] = "[]"; //à modifier une fois qu'on aura déterminé comment récupérer les bifurcations
    		this.line[ScheduleFormat.TERMINUS_INDEX] = stopName;
    		this.line[ScheduleFormat.TIME_INDEX] = heurePassage;
    		return this.line;
    	}
    	return null;
    }
}

