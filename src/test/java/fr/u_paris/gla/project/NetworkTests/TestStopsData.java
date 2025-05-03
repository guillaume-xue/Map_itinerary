package fr.u_paris.gla.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import fr.u_paris.gla.project.utils.*;
import fr.u_paris.gla.project.io.UpgradedNetworkFormat;

/**
 * Test class to verify the integrity of a sample of the stopsData.csv ( file parsed on 02/05/2025 )
 */
public class TestStopsData{
    
    // Sample file
    private static final String STOPS_DATA_FILE = "src/test/resources/TestStopsData.csv";

    // Margin of error (5%)
    private static final double DISTANCE_TOLERANCE = 0.05;
    
    private static List<String[]> stopsData;
    
    @BeforeAll
    public static void setUp() throws IOException{
        stopsData = new ArrayList<>();
        Path path = Paths.get(STOPS_DATA_FILE);
        CSVTools.readCSVFromFile(path.toString(), stopsData::add);
    }
    
    @Test
    @DisplayName("Test that all lines in the file follow the UpgradedNetworkFormat")
    public void allLineAreValidTest(){
        for ( String[] line : stopsData ){
            assertTrue(CSVExtractor.isMapDataLineValid(line), 
                    "Each line should follow the UpgradedNetworkFormat");
        }
    }
    
    @Test
    @DisplayName("Test that distances in the file match calculated GPS distances")
    public void ExpectedDistanceVsActualDistanceTest(){        
        for ( String[] line : stopsData ){
            if ( !CSVExtractor.isMapDataLineValid(line) ){
                continue;
            }
            
            String[] stopACoordString = line[UpgradedNetworkFormat.START_INDEX + 1].split(",");
            double stopALat = Double.parseDouble(stopACoordString[0]);
            double stopALon = Double.parseDouble(stopACoordString[1]);
            
            String[] stopBCoordString = line[UpgradedNetworkFormat.STOP_INDEX + 1].split(",");
            double stopBLat = Double.parseDouble(stopBCoordString[0]);
            double stopBLon = Double.parseDouble(stopBCoordString[1]);
            
            float actualDistance = Float.parseFloat(line[UpgradedNetworkFormat.DISTANCE_INDEX]);             
            double expectedDistance = GPS.distance(stopALat, stopALon, stopBLat, stopBLon);
            
            // Skipping very close stops, which margin of error doesnt allow to pass
            if ( actualDistance < 0.001 && expectedDistance < 0.001 ) continue;

            
            double relativeError = Math.abs(actualDistance - expectedDistance) / actualDistance;
            
            assertTrue(relativeError <= DISTANCE_TOLERANCE,
                    String.format("Distance for stops %s to %s should match calculated distance (Actual: %.6f, Expected: %.6f)",
                            line[UpgradedNetworkFormat.START_INDEX], 
                            line[UpgradedNetworkFormat.STOP_INDEX],
                            actualDistance,
                            expectedDistance));
        }
    }
    
    @Test
    @DisplayName("Test that all required fields are present and not empty")
    public void everyFieldIsNotEmptyTest(){
        for ( String[] line : stopsData ){
            // Skip invalid lines
            if ( line.length != UpgradedNetworkFormat.NUMBER_COLUMNS ){
                continue;
            }
            
            // Check that all required fields are not empty
            assertFalse(line[UpgradedNetworkFormat.LINE_ID_INDEX].isEmpty(), "Line ID should not be empty");
            assertFalse(line[UpgradedNetworkFormat.LINE_NAME_INDEX].isEmpty(), "Line name should not be empty");
            assertFalse(line[UpgradedNetworkFormat.TYPE_INDEX].isEmpty(), "Transport type should not be empty");
            assertFalse(line[UpgradedNetworkFormat.START_INDEX].isEmpty(), "Start stop name should not be empty");
            assertFalse(line[UpgradedNetworkFormat.START_INDEX + 1].isEmpty(), "Start stop coordinates should not be empty");
            assertFalse(line[UpgradedNetworkFormat.STOP_INDEX].isEmpty(), "End stop name should not be empty");
            assertFalse(line[UpgradedNetworkFormat.STOP_INDEX + 1].isEmpty(), "End stop coordinates should not be empty");
            assertFalse(line[UpgradedNetworkFormat.DURATION_INDEX].isEmpty(), "Duration should not be empty");
            assertFalse(line[UpgradedNetworkFormat.DISTANCE_INDEX].isEmpty(), "Distance should not be empty");
        }
    }
    
    @Test
    @DisplayName("Test that coordinates can be parsed as doubles")
    public void coordinatesFormatTest(){
        for ( String[] line : stopsData ){
            // Skip invalid lines
            if ( line.length != UpgradedNetworkFormat.NUMBER_COLUMNS ){
                continue;
            }
            
            try {
                // Test start coordinates
                String[] startCoords = line[UpgradedNetworkFormat.START_INDEX + 1].split(",");
                assertEquals(2, startCoords.length, "Start coordinates should have latitude and longitude");
                Double.parseDouble(startCoords[0]); // latitude
                Double.parseDouble(startCoords[1]); // longitude
                
                // Test end coordinates
                String[] endCoords = line[UpgradedNetworkFormat.STOP_INDEX + 1].split(",");
                assertEquals(2, endCoords.length, "End coordinates should have latitude and longitude");
                Double.parseDouble(endCoords[0]); // latitude
                Double.parseDouble(endCoords[1]); // longitude
            } catch (NumberFormatException e){
                fail("Coordinates should be parsable as doubles: " + e.getMessage());
            }
        }
    }
}