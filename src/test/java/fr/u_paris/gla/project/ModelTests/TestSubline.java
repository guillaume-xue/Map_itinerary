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

        System.out.println(subline1);
        System.out.println(subline2);
    }

    @Test
    public void addDepartureTimesTest() throws Exception {

        ArrayList<LocalTime> departureTimesAtStartOfSubline = new ArrayList<>();
        ArrayList<LocalTime> departureTimes = new ArrayList<>();

        Stop stop1 = new Stop(48.78153003135253F, 2.29759632254211F, "Bourg-la-Reine");
        Stop stop2 = new Stop(0F, 0F, "Sceaux");
        Stop stop3 = new Stop(0F, 0F, "Fontenay aux Roses");
        Stop stop4 = new Stop(0F, 0F, "Robinson");

        Subline subline1 = new Subline("B1");
        Subline subline2 = new Subline("B2");
        Subline subline3 = new Subline("B2");
        Subline subline4 = new Subline("B2");

        departureTimes.add(LocalTime.of(14,00));
        departureTimes.add(LocalTime.of(14,15));
        departureTimes.add(LocalTime.of(14,30));
        departureTimes.add(LocalTime.of(14,45));
        departureTimes.add(LocalTime.of(15,00));
        departureTimes.add(LocalTime.of(15,15));
        departureTimes.add(LocalTime.of(15,30));
        departureTimes.add(LocalTime.of(15,45));
        departureTimes.add(LocalTime.of(16,00));

        departureTimesAtStartOfSubline.addAll(departureTimes);

        //subline2.addDepartureTimes(stop2, departureTimes);

        // for(LocalTime time : departureTimesAtStartOfSubline){
        //     System.out.println(time);
        // }


    }
}
