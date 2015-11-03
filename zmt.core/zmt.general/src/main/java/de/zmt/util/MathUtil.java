package de.zmt.util;

import sim.util.Double2D;

/**
 * Methods for various math operations.
 * 
 * @author mey
 *
 */
public class MathUtil {
    private MathUtil() {

    }

    /**
     * @see Double2D#angle()
     * @param direction1
     * @param direction2
     * @return shortest angle between both directions
     */
    public static final double angleBetween(Double2D direction1, Double2D direction2) {
	double difference = direction2.angle() - direction1.angle();
	if (difference > Math.PI) {
	    return Math.abs(Math.PI - difference % Math.PI);
	}
	return difference;
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return {@code value} clamped between {@code min} and {@code max}.
     */
    public static int clamp(int value, int min, int max) {
	return Math.max(Math.min(value, max), min);
    }
}
