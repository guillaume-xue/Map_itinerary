package fr.u_paris.gla.project;

import fr.u_paris.gla.project.idfm.TraceEntry;
import fr.u_paris.gla.project.idfm.IDFMNetworkExtractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;


class IDFMNetworkExtractorTest {

	@Test
	public void addLineTest() {
		String[] lineCSV = {"IDFMC00319","4301","4301","Bus","78C696"};
		Map<String, TraceEntry> traces = new HashMap<>();
		IDFMNetworkExtractor.addLine(lineCSV, traces);
		assertFalse(traces.isEmpty(), "La map ne doit pas être vide après l'ajout d'une ligne.");
        assertTrue(traces.containsKey("IDFMC00319"), "La clé IDFM:C00319 devrait exister dans la map.");

        TraceEntry expectedEntry1 = new TraceEntry("4301", "IDFMC00319", "Bus", "78C696");
        assertEquals(expectedEntry1, traces.get("IDFMC00319"), "L'entrée ajoutée ne correspond pas à l'attendu.");
        
        
        // Ajouter une deuxième ligne et tester
        String[] line2 = {"IDFMC00319", "4985", "4985", "Subway", "0xFFFF"};
        IDFMNetworkExtractor.addLine(line2, traces);

        // Vérifier la mise à jour de la TraceEntry dans la map
        TraceEntry expectedEntry2 = new TraceEntry("4985", "IDFMC00319", "Subway", "0xFFFF");
        assertEquals(expectedEntry2, traces.get("IDFMC00319"), "L'entrée devrait être mise à jour avec les nouvelles valeurs.");
	}

	@Test 
	public void addStopTest() {
		
	}
	
}
