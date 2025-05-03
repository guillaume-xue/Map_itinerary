/**
 * 
 */
package fr.u_paris.gla.project.utils;

/** A utility class for computations related to GPS.
 * 
 */
public final class GPS {

    /** The value of a flat angle, in degrees. */
    private static final int    FLAT_ANGLE_DEGREE = 180;
    /** The (approximated) earth radius in km. */
    private static final double EARTH_RADIUS      = 6_370.0;

    /** Hidden constructor for tool class */
    private GPS() {
        // Tool class
    }

    /** Convert a degree angle value in a radian angle one.
     * 
     * @param degree the degree value
     * @return the radian value */
    private static double degreeToRadian(double degree) {
        return degree / FLAT_ANGLE_DEGREE * Math.PI;
    }

    // TESTME -> Testme proved initial implementation was wrong
    /** Compute the flying distance between two GPS positions.
     * 
     * @param latitude1 the latitude of the first position
     * @param longitude1 the longitude of the first position
     * @param latitude2 the latitude of the second position
     * @param longitude2 the longitude of the second position
     * @return the flying distance in kilometers*/
    public static double distance(double latitude1, double longitude1, double latitude2, double longitude2) {

        double deltaLatitude = degreeToRadian(latitude2 - latitude1);
        double deltaLongitude = degreeToRadian(longitude2 - longitude1);

        double a =  haversine(deltaLatitude) + 
                    Math.cos(degreeToRadian(latitude1)) * Math.cos(degreeToRadian(latitude2)) *
                    haversine(deltaLongitude);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private static double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
