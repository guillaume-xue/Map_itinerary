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

class FileGenerationDeterminismTest {
	
	// pour que les fichiers testés soient dans target/test-temp et qu'ils soient suppr à chaque mvn clean
	private static final Path TEMP_DIR = Paths.get("target", "test-temp");

	//ce test n'est executé par maven que lorsqu'on utilise la commande 
	// mvn test -DrunDeterminismTest=true
	//sinon il est skipped car long
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
        //à voir si on suppr pas 
        deleteRecursively(scheduleDir1);
        deleteRecursively(scheduleDir2);
        Files.deleteIfExists(map1);
        Files.deleteIfExists(map2);
        Files.deleteIfExists(junction1);
        Files.deleteIfExists(junction2);
    }

	//vérifie mligne par ligne que deux fichiers sont identiques à l'aide d'un stream parce que gros fichiers
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