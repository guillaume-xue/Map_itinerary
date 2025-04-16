package fr.u_paris.gla.project.astar;

import fr.u_paris.gla.project.graph.*;
import java.time.LocalTime;
import java.time.Duration;

public class PassengerState {
    private Stop lastStopreached;
    private Subline currentSubline = null; // null si pas encore embarqué
    private LocalTime currentTime;

    public PassengerState(Stop lastStopreached, LocalTime currentTime) {
        this.lastStopreached = lastStopreached;
        this.currentTime = currentTime;
    }

    

    public void advanceTo(Stop nextStop, Duration travelTime, Subline newSubline) {
        this.lastStopreached = nextStop;
        this.currentTime = currentTime.plus(travelTime);
        this.currentSubline = newSubline;
    }
}

