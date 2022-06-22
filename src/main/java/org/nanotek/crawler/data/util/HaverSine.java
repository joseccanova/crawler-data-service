package org.nanotek.crawler.data.util;

import java.util.Optional;

/**
 * Jason Winn
 * http://jasonwinn.org
 * Created July 10, 2013
 *
 * Description: Small class that provides approximate distance between
 * two points using the Haversine formula.
 *
 * Call in a static context:
 * Haversine.distance(47.6788206, -122.3271205,
 *                    47.6788206, -122.5271205)
 * --> 14.973190481586224 [km]
 *
 */

public class HaverSine {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public static double distance(Optional<Double>startLato, Optional<Double> startLongo,
                                  Optional<Double> endLato, Optional<Double>endLongo) {

        double dLat  = Math.toRadians((endLato.get() - startLato.get()));
        double dLong = Math.toRadians((endLongo.get() - startLongo.get()));

        double startLat = Math.toRadians(startLato.get());
        double endLat   = Math.toRadians(endLato.get());

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    
    public static double distance2(Optional<Double>startLato, Optional<Double> startLongo,
            Optional<Double> endLato, Optional<Double>endLongo) {
			
    		double φ1 = startLato.get() * Math.PI/180;
    		double φ2 = endLato.get() * Math.PI/180;
    		
    		double Δφ = (endLato.get()-startLato.get()) * Math.PI/180;
    		double Δλ = (endLato.get()-endLongo.get()) * Math.PI/180;
    		
    		double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
    		          Math.cos(φ1) * Math.cos(φ2) *
    		          Math.sin(Δλ/2) * Math.sin(Δλ/2);
    		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    		
			double d = EARTH_RADIUS * c; // in metres
			
			
			return d; // <-- d
}

    
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}