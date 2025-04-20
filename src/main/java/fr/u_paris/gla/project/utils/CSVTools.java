/**
 * 
 */
package fr.u_paris.gla.project.utils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileInputStream;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/** A CSV tool class.
 * 
 * @author Emmanuel Bigeon */
public final class CSVTools {

    /** Hidden constructor of tool class */
    private CSVTools() {
        // Tool class
    }

    public static void readCSVFromURL(String url, Consumer<String[]> contentLineConsumer)
            throws IOException {
        ICSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (InputStream is = new URL(url).openStream();
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVReaderBuilder csvBuilder = new CSVReaderBuilder(reader)
                    .withCSVParser(parser);
            try (CSVReader csv = csvBuilder.build()) {
                String[] line = csv.readNextSilently(); // Eliminate header
                while (csv.peek() != null) {
                    line = csv.readNext();
                    contentLineConsumer.accept(line);
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid csv file", e); //$NON-NLS-1$
        }
    }

    public static void readCSVFromFile(String path, Consumer<String[]> contentLineConsumer) 
            throws IOException {
        ICSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (InputStream is = new FileInputStream(path);
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVReaderBuilder csvBuilder = new CSVReaderBuilder(reader)
                    .withCSVParser(parser);
            try (CSVReader csv = csvBuilder.build()) {
                String[] line;
                int count = 0;
                while (csv.peek() != null /*&& count <= 100*/ ) {
                    line = csv.readNext();
                    contentLineConsumer.accept(line);
                    //ount++;
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Invalid csv file", e); //$NON-NLS-1$
        }
    }

    public static void writeCSVToFile(String filename,
            Stream<String[]> contentLineConsumer) throws IOException {
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            CSVWriterBuilder wBuilder = new CSVWriterBuilder(writer).withSeparator(';');
            try (ICSVWriter csv = wBuilder.build()) {
                contentLineConsumer.forEachOrdered(csv::writeNext);
            }
        }
    }

    
    public static void readCSVFromZip(String zipFilePath, String csvFileName, Consumer<String[]> contentLineConsumer) throws IOException {
        URL zipUrl = CSVTools.class.getClassLoader().getResource(zipFilePath);
        if (zipUrl == null) {
            throw new FileNotFoundException("Fichier ZIP introuvable: " + zipFilePath);
        }
        try (InputStream zipInputStream = zipUrl.openStream(); ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(csvFileName)) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8))) {
                        ICSVParser parser = new CSVParserBuilder().withSeparator(',').build();
                        CSVReader csvReader = new CSVReaderBuilder(reader)
                            .withCSVParser(parser)
                            .build();
                        String[] line;
                        while ((line = csvReader.readNext()) != null) {
                            contentLineConsumer.accept(line);
                        }
                    } catch (CsvValidationException e) {
                        throw new IOException("Erreur de validation CSV", e);
                    }
                    break; 
                }
                zis.closeEntry();
            }
        }
    }



}
