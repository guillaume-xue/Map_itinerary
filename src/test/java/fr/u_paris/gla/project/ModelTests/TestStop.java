package fr.u_paris.gla.project;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;


import fr.u_paris.gla.project.graph.Stop;

public class TestStop {

	
    @Test
    public void addAdjacentStopTest(){
        // 3 stations du RER C
        Stop stop1 = new Stop(48.828382841376715F, 2.379067231453914F, "Bibliothèque François Mitterand");
        Stop stop2 = new Stop(48.84252292928216F, 2.3672210648002685F, "Paris Austerlitz");
        Stop stop3 = new Stop(48.853767644075354F, 2.344640478142948F, "St-Michel Notre Dame");

        assert(stop1.getNameOfAssociatedStation() == "Bibliothèque François Mitterand");
        assert(stop2.getNameOfAssociatedStation() == "Paris Austerlitz");
        assert(stop3.getNameOfAssociatedStation() == "St-Michel Notre Dame");

    }
    
    

}

