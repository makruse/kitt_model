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
    public static final Double2D NEUTRAL = new Double2D(0, 0);
    /** Direction pointing east: (1,0) */
    public static final Double2D EAST = new Double2D(1, 0);
    /** Direction pointing south: (0,1) */
    public static final Double2D SOUTH = new Double2D(0, 1);
    /** Direction pointing west: (-1,0) */
    public static final Double2D WEST = new Double2D(-1, 0);
    /** Direction pointing north: (0,-1) */
    public static final Double2D NORTH = new Double2D(0, -1);

    private static final double INV_SQRT_2 = 1 / Math.sqrt(2);
    /** Direction pointing south-east: (1,1)^ */
    public static final Double2D SOUTHEAST = new Double2D(INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing south-west: (-1,1)^ */
    public static final Double2D SOUTHWEST = new Double2D(-INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing north-west: (-1,-1)^ */
    public static final Double2D NORTHWEST = new Double2D(-INV_SQRT_2, -INV_SQRT_2);
    /** Direction pointing north-east: (1,-1)^ */
    public static final Double2D NORTHEAST = new Double2D(INV_SQRT_2, -INV_SQRT_2);

    private static final double PI_TIMES_2 = Math.PI * 2;

    private DirectionUtil() {

    }

    /**
     * Returns the angle between two vectors. Parameter order matters.
     * Anti-clockwise angles are negative.
     * 
     * @see #angleBetweenFast(Double2D, Double2D)
     * @see Double2D#angle()
     * @param fromVector
     * @param toVector
     * @return shortest angle between {@code fromVector} and {@code toVector}
     */
    public static double angleBetween(Double2D fromVector, Double2D toVector) {
	return angleBetween(fromVector.angle(), toVector.angle());
    }

    /**
     * Returns the angle between two angles. Parameter order matters.
     * Anti-clockwise angles are negative.
     * 
     * @see <a href=
     *      "http://stackoverflow.com/questions/1878907/the-smallest-difference-between-2-angles">
     *      Stack Overflow: The smallest difference between 2 Angles</a>
     * @param fromAngle
     * @param toAngle
     * @return shortest angle between {@code fromAngle} and {@code toAngle}
     */
    public static double angleBetween(double fromAngle, double toAngle) {
	return normalizeAngle(toAngle - fromAngle);
    }

    /**
     * Faster version of {@link #angleBetween(Double2D, Double2D)}, assuming
     * both directions are unit vectors.
     * <p>
     * <b>NOTE:</b> The result is always positive, regardless of parameter
     * order. This is different from {@link #angleBetween(Double2D, Double2D)}.
     * 
     * @param direction
     * @param otherDirection
     * @return shortest angle between {@code fromDirection} and
     *         {@code toDirection}
     */
    public static double angleBetweenFast(Double2D direction, Double2D otherDirection) {
	double dotProduct = direction.dot(otherDirection);
	return Math.acos(dotProduct);
    }

    /**
     * Normalizes a radian angle between PI and -PI.
     * 
     * @param angle
     *            in radians
     * @return normalized angle
     */
    public static double normalizeAngle(double angle) {
	if (angle > Math.PI) {
	    return angle - PI_TIMES_2;
	}
	if (angle < -Math.PI) {
	    return angle + PI_TIMES_2;
	}
	return angle;
    }

    /**
     * Rotates a vector.
     * 
     * @param vector
     *            the vector to rotate
     * @param theta
     *            the angle of rotation
     * @return the rotated vector
     */
    public static Double2D rotate(Double2D vector, double theta) {
	double cos = Math.cos(theta);
	double sin = Math.sin(theta);
	return rotate(vector, cos, sin);
    }

    /**
     * Rotates a vector.
     * 
     * @param vector
     *            the vector to rotate
     * @param direction
     *            the direction unit vector specifying the rotation
     * @return the rotated vector
     */
    public static Double2D rotate(Double2D vector, Double2D direction) {
	return rotate(vector, direction.x, direction.y);
    }

    /**
     * Rotates a vector.
     * 
     * @param vector
     *            the vector to rotate
     * @param cos
     *            the cosine of the angle
     * @param sin
     *            the sine of the angle
     * @return the rotated vector
     */
    private static Double2D rotate(Double2D vector, double cos, double sin) {
	double x = vector.x * cos - vector.y * sin;
	double y = vector.x * sin + vector.y * cos;
	return new Double2D(x, y);
    }

    /**
     * Creates a direction vector pointing towards given angle.
     * 
     * @param theta
     *            angle that vector points towards
     * @return direction vector pointing towards {@code theta}
     */
    public static Double2D fromAngle(double theta) {
	return new Double2D(Math.cos(theta), Math.sin(theta));
    }

    /**
     * Generates a random direction vector.
     * 
     * @param random
     *            random number generator to be used
     * @return random direction unit vector
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
