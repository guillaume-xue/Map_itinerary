package fr.u_paris.gla.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import fr.u_paris.gla.project.idfm.IDFMNetworkExtractor;
import fr.u_paris.gla.project.utils.CSVExtractor;
import fr.u_paris.gla.project.views.Launcher;
import java.io.File;
import java.net.URL;


/** Simple application model.
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
                    //new Launcher();
                    return;
                }
                if ("--parse".equals(string)) {
                    launchParser( args );
                    return;
                }
                if ("--createfiles".equals(string)) {
                	launchMakingFilesParser( new String[] { args[1], args[2], args[3]} );
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

    public static void errorLog(String log){
        System.out.println("Error: Invalid command line for parser. " + log + ".");
        System.out.println("Usage: --parse <URL|CSV> <target-file.csv|input-file.csv>");
    }

    public static void launchMakingFilesParser(String[] args){
    	if (args.length != 3) {
            errorLog("Needs two target files and a target repertory.");
            return;
        }
        IDFMNetworkExtractor.parse(args);     
    }

    public static void launchParser(String[] args) {
    	if ( args.length != 2 ){
            errorLog("Missing target file");
            return;
        }
<<<<<<< HEAD

        if ( "URL".equals(args[1]) ){
            IDFMNetworkExtractor.parse(new String[]{args[2]});
        } else if ( "CSV".equals(args[1]) ){
            CSVExtractor.makeOjectsFromCSV(args[2]);
        } else {
            errorLog("Wrong argument for parser");
            return;
        }
=======
        CSVExtractor.makeOjectsFromCSV(args[1]);
>>>>>>> 64b3069 (refactor de la création des fichiers)
    }
}
