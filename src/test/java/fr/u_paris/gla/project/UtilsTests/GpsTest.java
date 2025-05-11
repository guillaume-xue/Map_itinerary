package fr.u_paris.gla.project;

import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.u_paris.gla.project.utils.GPS;
import fr.u_paris.gla.project.graph.Stop;

public class GpsTest{

	@Test
	public void distanceBetweenTwoStopsTest(){
		Stop stopA = new Stop(48.85795502218887, 2.437061897943258, "Croix de Chavaux");
		Stop stopB = new Stop(48.85642255157002, 2.4451689512360044, "Gabriel Péri");

		double expectedDistance = 0.6; // distance approximative, source: google.maps
    	double actualDistance = stopA.calculateDistance(stopB);

    	assertEquals(expectedDistance, actualDistance, 0.04); // + ou - 40 metres
	}						  

	@Test
	public void distanceBetweenTwoCoordsTest(){
		Stop stopA = new Stop(48.85795502218887, 2.437061897943258, "Croix de Chavaux");

		double expectedDistance = 0.6;
		double actualDistance = stopA.calculateDistance(48.85642255157002, 2.4451689512360044);

		assertEquals(expectedDistance, actualDistance, 0.04);
	}

	@Test
	public void distandMethodTest(){
		double expectedDistance = 0.6;
		double actualDistance = GPS.distance(48.85795502218887, 2.437061897943258, 48.85642255157002, 2.4451689512360044);

		assertEquals(expectedDistance, actualDistance, 0.04);
	}

}