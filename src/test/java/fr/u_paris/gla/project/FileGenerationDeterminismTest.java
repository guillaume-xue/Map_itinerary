package fr.u_paris.gla.project;

import static org.junit.jupiter.api.Assertions.*;
import fr.u_paris.gla.project.idfm.IDFMNetworkExtractor;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.Test;


/**
 * Classe de test permettant de vérifier que la génération de fichiers à partir de l'extracteur IDFM
 * est déterministe, c’est-à-dire qu'elle produit toujours les mêmes fichiers à partir des mêmes entrées.
 * 
 * <p>Ce test est long à exécuter et n'est lancé que si le système est configuré avec la propriété :
 * {@code -DrunDeterminismTest=true}</p>
 * 
 * <p>Les fichiers temporaires sont générés dans {@code target/test-temp} et supprimés après exécution.</p>
 */
class FileGenerationDeterminismTest {
	
    /**
     * Répertoire temporaire où sont placés les fichiers de test.
     * Supprimé à chaque exécution de {@code mvn clean}.
     */
	private static final Path TEMP_DIR = Paths.get("target", "test-temp");

    /**
     * Test principal de déterminisme.
     * Il exécute deux fois la génération à partir de l'extracteur {@code IDFMNetworkExtractor},
     * puis compare les fichiers produits pour s'assurer qu'ils sont strictement identiques.
     *
     * @throws Exception en cas d’erreur d’IO ou de génération
     */
	@Test
	@EnabledIfSystemProperty(named = "runDeterminismTest", matches = "true")
	public void testFileGenerationIsDeterministic() throws Exception {
		
		Files.createDirectories(TEMP_DIR);
		
		
        Path map1 = TEMP_DIR.resolve("mapDataForTest1.csv");
        Path map2 = TEMP_DIR.resolve("mapDataForTest2.csv");
        Path junction1 = TEMP_DIR.resolve("junctionsDataForTest1.csv");
        Path junction2 = TEMP_DIR.resolve("junctionsDataForTest2.csv");
        Path scheduleDir1 = TEMP_DIR.resolve("ScheduleForTest1");
        Path scheduleDir2 = TEMP_DIR.resolve("ScheduleForTest2");

        // on remplit les deux jeux de fichiers/dossier
        IDFMNetworkExtractor.parse(new String[]{
            map1.toString(), junction1.toString(), scheduleDir1.toString()
        });
        IDFMNetworkExtractor.parse(new String[]{
            map2.toString(), junction2.toString(), scheduleDir2.toString()
        });

        // Comparaison des fichiers CSV
        assertFilesEqual(map1, map2);
        assertFilesEqual(junction1, junction2);

        // Comparaison du contenu des dossiers de schedule
        assertDirectoriesEqual(scheduleDir1, scheduleDir2);

        System.out.println("REUSSITE DES TESTS ! Tous les fichiers sont identiques");
        
        // Suppression des fichiers après le test 
        deleteRecursively(scheduleDir1);
        deleteRecursively(scheduleDir2);
        Files.deleteIfExists(map1);
        Files.deleteIfExists(map2);
        Files.deleteIfExists(junction1);
        Files.deleteIfExists(junction2);
    }

    /**
     * Compare ligne par ligne le contenu de deux fichiers texte.
     * Lève une erreur si une différence est détectée, avec indication de la ligne fautive.
     *
     * @param file1 chemin vers le premier fichier
     * @param file2 chemin vers le second fichier
     * @throws IOException en cas d'erreur de lecture
     */
	private void assertFilesEqual(Path file1, Path file2) throws IOException {
	    try (
	        Stream<String> stream1 = Files.lines(file1);
	        Stream<String> stream2 = Files.lines(file2)
	    ) {
	        Iterator<String> it1 = stream1.iterator();
	        Iterator<String> it2 = stream2.iterator();

	        int lineNum = 1;
	        while (it1.hasNext() && it2.hasNext()) {
	            String line1 = it1.next();
	            String line2 = it2.next();

	            assertEquals(line1, line2, 
	                "Les fichiers diffèrent à la ligne " + lineNum + " :\n" +
	                "  fichier 1 : " + line1 + "\n" +
	                "  fichier 2 : " + line2
	            );
	            lineNum++;
	        }

	        // Vérifie s’il reste des lignes dans l’un ou l’autre
	        if (it1.hasNext() || it2.hasNext()) {
	            fail("Les fichiers n'ont pas le même nombre de lignes. Ligne divergente : " + lineNum);
	        }
	    }
	}


    /**
     * Compare deux répertoires pour vérifier que :
     * <ul>
     *   <li>Ils contiennent les mêmes fichiers (même noms)</li>
     *   <li>Chaque fichier correspondant est strictement identique ligne par ligne</li>
     * </ul>
     *
     * @param dir1 premier dossier à comparer
     * @param dir2 second dossier à comparer
     * @throws IOException en cas d'erreur de lecture ou d'accès
     */
    private void assertDirectoriesEqual(Path dir1, Path dir2) throws IOException {
        assertTrue(Files.isDirectory(dir1), dir1 + " n'est pas un dossier.");
        assertTrue(Files.isDirectory(dir2), dir2 + " n'est pas un dossier.");

        // Liste les fichiers dans chaque répertoire
        try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(dir1);
             DirectoryStream<Path> stream2 = Files.newDirectoryStream(dir2)) {

            List<Path> files1 = StreamSupport.stream(stream1.spliterator(), false)
                                             .map(p -> p.getFileName())
                                             .sorted()
                                             .toList();

            List<Path> files2 = StreamSupport.stream(stream2.spliterator(), false)
                                             .map(p -> p.getFileName())
                                             .sorted()
                                             .toList();

            assertEquals(files1, files2, "Les fichiers présents dans les deux dossiers diffèrent.");

            // Comparaison du contenu de chaque fichier
            for (Path relativePath : files1) {
                Path file1 = dir1.resolve(relativePath);
                Path file2 = dir2.resolve(relativePath);
                assertFilesEqual(file1, file2);
            }
        }
    }


    /**
     * Supprime récursivement un dossier ou un fichier.
     *
     * @param path chemin vers le fichier ou dossier à supprimer
     * @throws IOException si une erreur survient lors de la suppression
     */
    private void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;

        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    System.err.println("Erreur suppression : " + p + " (" + e.getMessage() + ")");
                }
            });
    }
}