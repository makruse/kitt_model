package sim.util;

import java.io.Serializable;

/**
 * A two-dimensional rotation. It is represented as a unit vector
 * 
 * <pre>
 * (cos(theta), sin(theta))
 * </pre>
 * 
 * where {@code theta} is the angle.
 * <p>
 * Objects of this class are immutable.
 * 
 * @author mey
 *
 */
public final class Rotation2D implements Comparable<Rotation2D>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The identity rotation. It will not change the orientation of a vector
     * when applied.
     */
    public static final Rotation2D ZERO = new Rotation2D(1, 0);
    private static final double INV_SQRT_2 = 1 / Math.sqrt(2);
    /** The rotation of an eighth revolution. */
    public static final Rotation2D EIGHTH = new Rotation2D(INV_SQRT_2, INV_SQRT_2);
    /** The rotation of a quarter revolution. */
    public static final Rotation2D QUARTER = new Rotation2D(0, 1);
    /** The rotation of a half revolution. */
    public static final Rotation2D HALF = new Rotation2D(-1, 0);

    /** The unit vector representing the rotation. */
    private final Double2D vector;

    /**
     * Constructs a new {@link Rotation2D}./
     * 
     * @param cos
     *            the cosine of the rotation
     * @param sin
     *            the sine of the rotation
     */
    private Rotation2D(double cos, double sin) {
	this(new Double2D(cos, sin));
    }

    /**
     * Constructs a new {@link Rotation2D}.
     * 
     * @param vector
     *            the unit vector representing the rotation
     */
    private Rotation2D(Double2D vector) {
	super();
	this.vector = vector;
    }

    /**
     * Concatenates this rotation with another rotation.
     * 
     * @param other
     *            the other rotation
     * @return the concatenated rotation
     */
    public Rotation2D multiply(Rotation2D other) {
	return new Rotation2D(multiply(other.vector));
    }

    /**
     * Applies this rotation on a vector by rotating around its origin.
     * 
     * @param vector
     *            the vector to rotate
     * @return the rotated vector
     */
    public Double2D multiply(Double2D vector) {
	// similar to rotation matrix multiplication
	double x = getCos() * vector.x - getSin() * vector.y;
	double y = getSin() * vector.x + getCos() * vector.y;
	return new Double2D(x, y);
    }

    /**
     * Returns the opposite of this direction.
     * 
     * @return the opposite of this rotation
     */
    public Rotation2D opposite() {
	// similar to rotation matrix transposition
	return new Rotation2D(getCos(), -getSin());
    }

    /**
     * Performs a linear interpolation between the vectors of this rotation and
     * the other. The normalized result is used to create a valid rotation.
     * 
     * @param other
     *            the other rotation
     * @param t
     *            the interpolation parameter
     * @return the resulting rotation
     * @throws ArithmeticException
     *             if rotations are opposite
     */
    public Rotation2D nlerp(Rotation2D other, double t) {
	return new Rotation2D(lerp(vector, other.vector, t).normalize());
    }

    /**
     * Linear interpolation between start and end vector.
     * 
     * @param start
     *            the start vector
     * @param end
     *            the end vector
     * @param t
     *            the interpolation parameter
     * @return the resulting vector
     */
    private static Double2D lerp(Double2D start, Double2D end, double t) {
	checkInterpolationParameter(t);
	return start.add(end.subtract(start).multiply(t));
    }

    /**
     * Performs a spherical linear interpolation between this rotation and the
     * other. If the other rotation is opposite to this, the interpolation takes
     * place clockwise.
     * 
     * @param other
     *            the other rotation
     * @param t
     *            the interpolation parameter
     * @return the resulting rotation
     */
    public Rotation2D slerp(Rotation2D other, double t) {
	if (this.equals(other)) {
	    return this;
	}

	double dot = vector.dot(other.vector);

	Double2D p0 = this.vector;
	Double2D p1;
	double scale0;
	double scale1;
	// special case:
	// rotations pointing in opposite directions, go clockwise
	if (dot <= -1) {
	    // perpendicular to p0 from this rotation
	    p1 = new Double2D(-p0.y, p0.x);

	    scale0 = Math.cos(t * Math.PI);
	    scale1 = Math.sin(t * Math.PI);
	}
	// the normal case
	else {
	    p1 = other.vector;
	    double theta = Math.acos(dot);
	    double invSinTheta = 1 / Math.sin(theta);
	    scale0 = Math.sin((1 - t) * theta) * invSinTheta;
	    scale1 = Math.sin(t * theta) * invSinTheta;
	}

	return new Rotation2D(p0.multiply(scale0).add(p1.multiply(scale1)));
    }

    /**
     * Throws an {@link IllegalArgumentException} if interpolation parameter is
     * negative.
     * 
     * @param t
     *            the interpolation parameter
     */
    private static void checkInterpolationParameter(double t) {
	if (t < 0) {
	    throw new IllegalArgumentException("The interpolation parameter must be positive, but was " + t);
	}
    }

    /**
     * Returns <code>true</code> if the shortest path following this rotation is
     * clockwise. Otherwise, <code>false</code> is returned
     * 
     * @return <code>true</code> if clockwise
     */
    public boolean isClockwise() {
	return getSin() > 0;
    }

    /**
     * Returns the angle of this rotation in radians.
     * 
     * @return the angle of this rotation
     */
    public double toAngle() {
	return Math.atan2(getSin(), getCos());
    }

    /**
     * Returns the rotation as direction vector.
     * 
     * @return the direction vector
     */
    public Double2D getVector() {
	return vector;
    }

    /**
     * Returns the rotation which is equal to the given angle.
     * 
     * @param theta
     *            the angle in radians
     * @return the rotation equal to the given angle
     */
    public static Rotation2D fromAngle(double theta) {
	return new Rotation2D(Math.cos(theta), Math.sin(theta));
    }

    /**
     * Creates a rotation from a direction vector. The result is the rotation
     * between the direction vector (1,0) and the given one.
     * <p>
     * <b>NOTE:</b> The vector must be of unit length. Otherwise the resulting
     * rotation is invalid.
     * 
     * @param vector
     *            the direction <b>unit</b> vector
     * @return the rotation object
     */
    public static Rotation2D fromVector(Double2D vector) {
	assertUnitLength(vector);
	return new Rotation2D(vector);
    }

    /**
     * Creates a rotation from {@code direction1} to {@code direction2}.
     * <p>
     * <b>NOTE:</b> Both vectors must be of unit length. Otherwise the resulting
     * rotation is invalid.
     * 
     * @param direction1
     * @param direction2
     * @return the rotation between both vectors
     */
    public static Rotation2D fromBetween(Double2D direction1, Double2D direction2) {
	assertUnitLength(direction1);
	assertUnitLength(direction2);

	double cos = direction1.dot(direction2);
	double sin = direction1.perpDot(direction2);
	return new Rotation2D(cos, sin);
    }

    /**
     * Asserts that given vector is of unit length.
     * 
     * @param vector
     *            the vector to check
     */
    private static void assertUnitLength(Double2D vector) {
	assert (vector.lengthSq() - 1) < 1e-10 : "Vector " + vector + " must be of unit length.";
    }

    /**
     * Returns the cosine of this rotation.
     * 
     * @return the cosine of this rotation
     */
    public double getCos() {
	return vector.x;
    }

    /**
     * Returns the sine of this rotation.
     * 
     * @return the sine of this rotation
     */
    public double getSin() {
	return vector.y;
    }

    @Override
    public int compareTo(Rotation2D o) {
	return Double.compare(o.getCos(), this.getCos());
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[" + getCos() + "," + getSin() + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((vector == null) ? 0 : vector.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	Rotation2D other = (Rotation2D) obj;
	if (vector == null) {
	    if (other.vector != null) {
		return false;
	    }
	} else if (!vector.equals(other.vector)) {
	    return false;
	}
	return true;
    }
}
