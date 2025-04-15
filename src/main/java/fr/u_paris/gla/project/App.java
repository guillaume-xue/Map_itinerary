package fr.u_paris.gla.project;

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
                    launchMakingFilesParser(new String[] { args[1], args[2], args[3] });
                    return;
                }
            }
        }
    }

    /** @param out */
    public static void printAppInfos(PrintStream out) {
        Properties props = readApplicationProperties();

        out.println("Application: " + props.getProperty("app.name", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("Version: " + props.getProperty("app.version", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("By: " + props.getProperty("app.team", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static Properties readApplicationProperties() {
        Properties props = new Properties();
        try (InputStream is = App.class.getResourceAsStream("application.properties")) { //$NON-NLS-1$
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read application informations", e); //$NON-NLS-1$
        }
        return props;
    }

    public static void errorLog(String command, String log) {

        // TODO ? Remplacer les System.out.prinln par un Logger
        switch (command) {
            case "parser":
                System.out.println("Error: Invalid command line for objects parser. " + log + ".");
                System.out.println("Usage: --parse <stops_data.csv> <junctions_data.csv>");
                break;
            case "generator":
                System.out.println("Error: Invalid command line for IDFM parser. " + log + ".");
                // FIXME
                System.out.println("Usage: --createfiles mapData.csv junctionsData.csv Schedule/");
                break;
            default:
                System.out.println("Error: Unknown");
                break;
        }
    }

    public static void launchMakingFilesParser(String[] args) {
        if (args.length != 3) {
            errorLog("generator", "Needs two target files and a target directory.");
            return;
        }
        IDFMNetworkExtractor.parse(args);
    }

    public static void launchParser(String[] args) {
        if (args.length != 3) {
            errorLog("parser", "Missing inputs files");
            return;
        }
        CSVExtractor.makeOjectsFromCSV(args);
    }
}