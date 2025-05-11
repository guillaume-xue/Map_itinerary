package fr.u_paris.gla.project;

import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import fr.u_paris.gla.project.graph.Stop;
import fr.u_paris.gla.project.graph.Subline;

public class TestSubline {
    @Test
    public void addNextStopTest(){

        ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();

        Stop stop1 = new Stop(48.78153003135253F, 2.29759632254211F, "Bourg-la-Reine");
        Stop stop2 = new Stop(0F, 0F, "Sceaux");
        Stop stop3 = new Stop(0F, 0F, "Fontenay aux Roses");
        Stop stop4 = new Stop(0F, 0F, "Robinson");

        Subline subline1 = new Subline("B1");
        Subline subline2 = new Subline("B2");


        subline1.addNextStop(stop1);
        subline2.addNextStop(stop2);
        subline2.addNextStop(stop3);
        subline2.addNextStop(stop4);

        assert(stop1.getNameOfAssociatedStation() == "Bourg-la-Reine");
        assert(stop2.getNameOfAssociatedStation() == "Sceaux");
        assert(stop3.getNameOfAssociatedStation() == "Fontenay aux Roses");
        assert(stop4.getNameOfAssociatedStation() == "Robinson");
    }

}
