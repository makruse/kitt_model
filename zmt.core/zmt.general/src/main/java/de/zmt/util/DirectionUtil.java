package de.zmt.util;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

/**
 * Contains methods for creating and manipulating directions represented as
 * {@link Double2D} unit vectors.
 * 
 * @author mey
 *
 */
public final class DirectionUtil {
    /** Neutral direction not pointing anywhere: (0,0) */
    public static final Double2D DIRECTION_NEUTRAL = new Double2D(0, 0);
    /** Direction pointing east: (1,0) */
    public static final Double2D DIRECTION_EAST = new Double2D(1, 0);
    /** Direction pointing south: (0,1) */
    public static final Double2D DIRECTION_SOUTH = new Double2D(0, 1);
    /** Direction pointing west: (-1,0) */
    public static final Double2D DIRECTION_WEST = new Double2D(-1, 0);
    /** Direction pointing north: (0,-1) */
    public static final Double2D DIRECTION_NORTH = new Double2D(0, -1);

    private static final double INV_SQRT_2 = 1 / Math.sqrt(2);
    /** Direction pointing south-east: (1,1)^ */
    public static final Double2D DIRECTION_SOUTHEAST = new Double2D(INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing south-west: (-1,1)^ */
    public static final Double2D DIRECTION_SOUTHWEST = new Double2D(-INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing north-west: (-1,-1)^ */
    public static final Double2D DIRECTION_NORTHWEST = new Double2D(-INV_SQRT_2, -INV_SQRT_2);
    /** Direction pointing north-east: (1,-1)^ */
    public static final Double2D DIRECTION_NORTHEAST = new Double2D(INV_SQRT_2, -INV_SQRT_2);

    private static final double PI_TIMES_2 = Math.PI * 2;

    private DirectionUtil() {

    }

    /**
     * @see Double2D#angle()
     * @param fromDirection
     * @param toDirection
     * @return shortest angle between {@code fromDirection} and
     *         {@code toDirection}
     */
    public static final double angleBetween(Double2D fromDirection, Double2D toDirection) {
	return angleBetween(fromDirection.angle(), toDirection.angle());
    }

    /**
     * @see <a href=
     *      "http://stackoverflow.com/questions/1878907/the-smallest-difference-between-2-angles">
     *      Stackoverflow: The smallest difference between 2 Angles</a>
     * @param fromAngle
     * @param toAngle
     * @return shortest angle between {@code fromAngle} and {@code toAngle}
     */
    public static double angleBetween(double fromAngle, double toAngle) {
	double difference = toAngle - fromAngle;
	if (difference > Math.PI) {
	    return difference - PI_TIMES_2;
	} else if (difference < -Math.PI) {
	    return difference + PI_TIMES_2;
	}
	return difference;
    }

    /**
     * Rotate a vector.
     * 
     * @param vector
     *            vector to rotate
     * @param theta
     *            angle of rotation
     * @return rotated vector
     */
    public static Double2D rotate(Double2D vector, double theta) {
	double cos = Math.cos(theta);
	double sin = Math.sin(theta);
	double x = vector.x * cos - vector.y * sin;
	double y = vector.x * sin + vector.y * cos;
	return new Double2D(x, y);
    }

    /**
     * Create a direction vector pointing towards given angle.
     * 
     * @param theta
     *            angle that vector points towards
     * @return direction vector pointing towards {@code theta}
     */
    public static Double2D fromAngle(double theta) {
	return new Double2D(Math.cos(theta), Math.sin(theta));
    }

    /**
     * Generates a random direction vector
     * 
     * @param random
     *            random number generator to be used
     * @return random direction vector
     */
    public static Double2D generate(MersenneTwisterFast random) {
	double x = random.nextDouble() * 2 - 1;
	// length = sqrt(x^2 + y^2)
	// chooses y so that length = 1
	double y = Math.sqrt(1 - x * x);

	// ...and randomize sign
	if (random.nextBoolean()) {
	    y = -y;
	}

	return new Double2D(x, y);
    }
}
