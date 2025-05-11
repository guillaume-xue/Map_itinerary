package fr.u_paris.gla.project;

import java.util.Objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.io.File;

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
     * Default path constants
     */
    private static final String STOPS_DEFAULT = "stopsData.csv";
    private static final String JUNCTIONS_DEFAULT = "junctionsData.csv";
    private static final String SCHEDULES_DEFAULT = "Schedules/";
    private static final String JAR_PATH = "path/projet-"
            + readApplicationProperties().getProperty("app.version", UNSPECIFIED);

    private static boolean isUsingDefault = false;

    /**
     * Application entry point.
     *
     * @param args launching arguments
     */
    public static void main(String[] args) {

        String command;
        String[] command_args;

        // Si aucun arguments spécifié on affiche le manuel.
        if (args.length == 0) { // On a rien
            printHelp();
            return;
        } else if (args.length == 1) { // On a que la commande
            command = args[0];
            command_args = new String[] { STOPS_DEFAULT, JUNCTIONS_DEFAULT, SCHEDULES_DEFAULT };
            isUsingDefault = true;
        } else if (args.length == 4) { // On a la commande + les arguments
            if (areCommandsArgsValid(args)) { // Les arguments sont valides
                command = args[0];
                command_args = new String[] { args[1], args[2], args[3] };
            } else { // Pas valides
                System.out.println("Invalid usage. See usage below:\n");
                printHelp();
                return;
            }
        } else { // Tout les autres cas
            System.out.println("Invalid usage. See usage below:\n");
            printHelp();
            return;
        }

        switch (command) {
            case "--info":
                printAppInfos(System.out);
                break;
            case "--help":
                printHelp();
                break;
            case "--create-files":
                IDFMNetworkExtractor.parse(command_args);
                break;
            case "--parse":
                CSVExtractor.makeObjectsFromCSV(command_args);
                break;
            case "--gui":

                Boolean isValid = isUsingDefault && !areCommandsArgsValid(
                        new String[] { "", command_args[0], command_args[1], command_args[2] });
                // Si on veut utiliser les arguments par défault mais qu'ils n'ont pas été créé
                if (isValid) {
                    System.out.println(
                            "GUI called with default arguments but default files do not exist yet. Calling parser.");
                }
                Launcher launcher = new Launcher(command_args, isValid);
                break;
            default:
                System.out.println("Unknown command, see usage below:\n");
                printHelp();
                break;
        }
    }

    /**
     * Validates the arguments for the command. Verifies that the path leads to a
     * correct expected file.
     *
     * @param args The arguments
     *
     * @return True if arguments are valid, false otherwise
     */
    public static boolean areCommandsArgsValid(String[] args) {
        File stopsFile = new File(args[1]);
        File junctionsFile = new File(args[2]);
        File schedulesDir = new File(args[3]);

        if (!stopsFile.exists() || !stopsFile.isFile() || !args[1].endsWith(".csv")) {
            System.out.println("Error: '" + args[1] + "' does not exist or is not a CSV file. See usage.");
            return false;
        }

        if (!junctionsFile.exists() || !junctionsFile.isFile() || !args[2].endsWith(".csv")) {
            System.out.println("Error: '" + args[2] + "' does not exist or is not a CSV file. See usage.");
            return false;
        }

        if (!schedulesDir.exists() || !schedulesDir.isDirectory()) {
            System.out.println("Error: '" + args[3] + "' does not exist or is not a directory. See usage.");
            return false;
        }

        return true;
    }

    /**
     * Prints application infos (name, version, team).
     *
     * @param out The out stream
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
     * @return a Properties object containing application information
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
     * Prints the help message.
     */
    public static void printHelp() {
        System.out.println("Usage: java -jar " + JAR_PATH + ".jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("    --help              Shows this help message and exits.");
        System.out.println();
        System.out.println("    --info              Shows the application information and exits.");
        System.out.println();
        System.out.println("    --create-files [stopsData.csv junctionsData.csv schedules_dir]");
        System.out.println("                        Creates the files from the IDFM Network used by the model.");
        System.out
                .println("                        Arguments are optional. Provide all three or none to use defaults.");
        System.out.println();
        System.out.println("    --parse [stopsData.csv junctionsData.csv schedules_dir]");
        System.out.println("                        Parses the provided files in argument to generate the model");
        System.out
                .println("                        Arguments are optional. Provide all three or none to use defaults.");
        System.out.println();
        System.out.println(
                "    --gui               Main entry point of the application. Automatically manages the previous commands and launches the GUI.");
        System.out.println();
    }
}