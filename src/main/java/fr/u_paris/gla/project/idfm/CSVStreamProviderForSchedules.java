package fr.u_paris.gla.project.idfm;


import fr.u_paris.gla.project.io.ScheduleFormat;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

/*public class CSVStreamProviderForSchedules implements Supplier<String[]> {

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
    		String heureNormalisee = normalizeTime(heurePassage);
    		this.line[ScheduleFormat.LINE_INDEX] = lineName;
    		this.line[ScheduleFormat.TRIP_SEQUENCE_INDEX] = "[]"; //à modifier une fois qu'on aura déterminé comment récupérer les bifurcations
    		this.line[ScheduleFormat.TERMINUS_INDEX] = stopName;
    		this.line[ScheduleFormat.TIME_INDEX] = heureNormalisee;
    		return this.line;
    	}
    	return null;
    }
    
    /* convertit un heure sous forme de String comme "25:12:56" 
     * en "01:12", format attendu dans les csv horaires
     * 
     */
    /*private static String normalizeTime(String heurePassage) {
        String[] parts = heurePassage.split(":");
        
        if (parts.length != 3) {
            throw new IllegalArgumentException("Format d'heure invalide : " + heurePassage);
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            hours = hours % 24;

            return String.format("%02d:%02d", hours, minutes);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format d'heure invalide : " + heurePassage, e);
        }
    }
}*/


public class CSVStreamProviderForSchedules implements Supplier<String[]> {

    private final Iterator<Pair<String, String>> timeIterator;
    private final String lineName;
    private final String stopName;
    private String[] line = new String[ScheduleFormat.NUMBER_COLUMNS];

    public CSVStreamProviderForSchedules(List<Pair<String, String>> times, String lineName, String stopName) {
        this.timeIterator = times.iterator();
        this.lineName = lineName;
        this.stopName = stopName;
    }

    @Override
    public String[] get() {
        if (timeIterator.hasNext()) {
            Pair<String, String> entry = timeIterator.next();
            String heurePassage = entry.getLeft();
            String numBifurcation = entry.getRight();

            String heureNormalisee = normalizeTime(heurePassage);

            this.line[ScheduleFormat.LINE_INDEX] = lineName;
            this.line[ScheduleFormat.TRIP_SEQUENCE_INDEX] = "["+ numBifurcation +"]"; // On met le numéro de bifurcation
            this.line[ScheduleFormat.TERMINUS_INDEX] = stopName;
            this.line[ScheduleFormat.TIME_INDEX] = heureNormalisee;

            return this.line;
        }
        return null;
    }

    /* Convertit une heure sous forme de String comme "25:12:56" 
     * en "01:12", format attendu dans les CSV horaires
     */
    private static String normalizeTime(String heurePassage) {
        String[] parts = heurePassage.split(":");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Format d'heure invalide : " + heurePassage);
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            hours = hours % 24;

            return String.format("%02d:%02d", hours, minutes);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format d'heure invalide : " + heurePassage, e);
        }
    }
}

