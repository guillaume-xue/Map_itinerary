package fr.u_paris.gla.project;

import java.util.Objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import fr.u_paris.gla.project.controllers.Launcher;
import fr.u_paris.gla.project.idfm.IDFMNetworkExtractor;
import fr.u_paris.gla.project.utils.CSVExtractor;

/**
 * Simple application model.
 *
 * @author Emmanuel Bigeon
 */
public class App {
    /**
     * Unspecified value.
     */
    private static final String UNSPECIFIED = "Unspecified"; //$NON-NLS-1$

    /**
     * Application entry point.
     *
     * @param args launching arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            for (String string : args) {
                if ("--info".equals(string)) { //$NON-NLS-1$
                    printAppInfos(System.out);
                    return;
                }
                if ("--gui".equals(string)) { //$NON-NLS-1$
                    new Launcher();
                    return;
                }
                if ("--parse".equals(string)) {
                    launchParser(args);
                    return;
                }
                if ("--createfiles".equals(string)) {
                	System.out.println("Création des fichiers en cours...");
                    launchMakingFilesParser( new String[] { args[1], args[2], args[3]} );
                    return;
                }
            }
        }
    }

    /**
     * Prints application infos.
     *
     * @param      out   The out stream
     */
    public static void printAppInfos(PrintStream out) {
        Properties props = readApplicationProperties();

        out.println("Application: " + props.getProperty("app.name", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("Version: " + props.getProperty("app.version", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("By: " + props.getProperty("app.team", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Loads and returns the application properties.
     *
     * @return     a Properties object containing application information
     */
    private static Properties readApplicationProperties() {
        Properties props = new Properties();
        try (InputStream is = App.class.getResourceAsStream("application.properties")) { //$NON-NLS-1$
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read application information", e); //$NON-NLS-1$
        }
        return props;
    }

    /**
     * Logs error messages based on the provided command and log context.
     *
     * @param      command  The string representation of the command
     * @param      log      The log from the context
     */
    public static void errorLog(String command, String log) {

        switch (command) {
            case "parser":
                System.out.println("Error: Invalid command line for objects parser. " + log + ".");
                System.out.println("Usage: --parse <stops_data.csv> <junctions_data.csv>");
                break;
            case "generator":
                System.out.println("Error: Invalid command line for IDFM parser. " + log + ".");
                System.out.println("Usage: --createfiles mapData.csv junctionsData.csv Schedule/");
                break;
            default:
                System.out.println("Error: Unknown");
                break;
        }
    }

    /**
     * Main entry point for IDFM network Extractor.
     *
     * @param      args  The arguments, expects length 3, two target files and a directory
     */
    public static void launchMakingFilesParser(String[] args) {
        if (args.length != 3) {
            errorLog("generator", "Needs two target files and a target directory.");
            return;
        }
        IDFMNetworkExtractor.parse(args);
    }

    /**
     * Main entry point for objects parser.
     *
     * @param      args  The arguments, expects length 3, the command and two target files
     */
    public static void launchParser(String[] args) {
        if ( args.length < 4 ){
            errorLog("parser","Missing inputs files");
            return;
        } else if ( args.length > 4 ){
            errorLog("parser", "Wrong arguments");
            return;
        }
        
        CSVExtractor.makeObjectsFromCSV(args);
    }
}