package de.zmt.kitt.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for scientific formulae.
 * 
 * @author cmeyer
 * 
 */
public class FormulaeUtil {
    /** Conversion factor for cm/s to m/min */
    private static final double SPEED_CONVERSION_FACTOR = 100d / 60d;

    /**
     * Net activity cost over the given time.
     * <p>
     * (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142<br>
     * Korsmeyer et al., 2002
     * 
     * @param speed
     *            in meter per minute during dt
     * @param dt
     *            delta time in minutes
     * @return cost in kj for maintaining the given speed over given time
     */
    public static final double netActivityCost(double speed, double dt) {
	double activityCostsPerHour = (1.193 * Math.pow(speed
		* SPEED_CONVERSION_FACTOR, 1.66)) * 0.0142;
	return (activityCostsPerHour / TimeUnit.HOURS.toMinutes(1)) * dt;
    }

}
