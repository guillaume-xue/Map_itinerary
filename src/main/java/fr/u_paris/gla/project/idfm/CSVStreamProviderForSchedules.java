package fr.u_paris.gla.project.idfm;


import fr.u_paris.gla.project.io.ScheduleFormat;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;


/**
 * Fournisseur de lignes CSV pour les horaires d'une station donnée sur une ligne de transport.
 * <p>
 * Cette classe permet d'itérer sur une liste de couples (heure de passage, numéro de bifurcation)
 * et de générer à chaque appel une ligne de données conforme au format attendu dans les fichiers CSV clients.
 * </p>
 */
public class CSVStreamProviderForSchedules implements Supplier<String[]> {

    private final Iterator<Pair<String, String>> timeIterator;
    private final String lineId;
    private final String stopName;
    private String[] line = new String[ScheduleFormat.NUMBER_COLUMNS];

    /**
     * Crée un fournisseur de lignes CSV à partir d'une liste d'horaires et du contexte de ligne.
     *
     * @param times     liste triée de couples (heure de passage, numéro de bifurcation)
     * @param lineId    identifiant de la ligne
     * @param stopName  nom de la station concernée
     */
    public CSVStreamProviderForSchedules(List<Pair<String, String>> times, String lineId, String stopName) {
        this.timeIterator = times.iterator();
        this.lineId = lineId;
        this.stopName = stopName;
    }

    /**
     * Renvoie une ligne formatée pour un fichier CSV horaires, ou {@code null} s'il n'y a plus d'horaires.
     * <p>
     * Chaque appel génère un tableau contenant les colonnes : identifiant de ligne, numéro de bifurcation (entre crochets),
     * nom de la station (terminus), et heure normalisée.
     * </p>
     *
     * @return un tableau de chaînes représentant une ligne CSV, ou {@code null} si fin de la séquence
     */
    @Override
    public String[] get() {
        if (timeIterator.hasNext()) {
            Pair<String, String> entry = timeIterator.next();
            String heurePassage = entry.getLeft();
            String numBifurcation = entry.getRight();

            String heureNormalisee = normalizeTime(heurePassage);

            this.line[ScheduleFormat.LINE_ID_INDEX] = lineId;
            this.line[ScheduleFormat.TRIP_SEQUENCE_INDEX] = "["+ numBifurcation +"]"; // On met le numéro de bifurcation
            this.line[ScheduleFormat.TERMINUS_INDEX] = stopName;
            this.line[ScheduleFormat.TIME_INDEX] = heureNormalisee;

            return this.line;
        }
        return null;
    }

    /**
     * Convertit une heure de type {@code HH:mm:ss} (même au-delà de 24h) en format {@code HH:mm} modulo 24h.
     *
     * @param heurePassage heure brute sous forme de String
     * @return heure normalisée au format {@code HH:mm}
     * @throws IllegalArgumentException si le format d'entrée est invalide
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

