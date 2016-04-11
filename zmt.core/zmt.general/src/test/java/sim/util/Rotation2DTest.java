package sim.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static sim.util.Rotation2D.*;

import org.hamcrest.Double2DCloseTo;
import org.junit.Test;

public class Rotation2DTest {

    private static final double ANGLE_ZERO = 0;
    private static final double ANGLE_EIGHTH = 0.25 * Math.PI;
    private static final double ANGLE_QUARTER = 0.5 * Math.PI;
    private static final double ANGLE_HALF = Math.PI;
    private static final double MAX_ERROR = 1E-15d;

    @Test
    public void multiply() {
	Rotation2D start = ZERO;

	multiply(start, EIGHTH, 8);
	multiply(start, EIGHTH.opposite(), 8);
    }

    private static void multiply(Rotation2D start, Rotation2D toMult, int iterations) {
	for (int i = 0; i < iterations; i++) {
	    Rotation2D nextRotation = start.multiply(toMult);
	    assertCloseTo(nextRotation.multiply(start.opposite()), toMult);
	    start = nextRotation;
	}
    }

    @Test
    public void fromAngle() {
	// 0°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_ZERO), ZERO);
	// 45°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_EIGHTH), EIGHTH);
	// 90°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_QUARTER), QUARTER);
	// 135°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_QUARTER + ANGLE_EIGHTH), QUARTER.multiply(EIGHTH));

	// 180°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_HALF), HALF);
	// 225°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_HALF + ANGLE_EIGHTH), HALF.multiply(EIGHTH));
	// 270°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_HALF + ANGLE_QUARTER), HALF.multiply(QUARTER));
	// 315°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_HALF + ANGLE_QUARTER + ANGLE_EIGHTH),
		HALF.multiply(QUARTER).multiply(EIGHTH));

	// 360°
	assertCloseTo(Rotation2D.fromAngle(ANGLE_HALF * 2), ZERO);
    }

    @Test
    public void toAngle() {
	// 0°
	assertAngleCloseTo(ZERO.toAngle(), ANGLE_ZERO);
	// 45°
	assertAngleCloseTo(EIGHTH.toAngle(), ANGLE_EIGHTH);
	// 90°
	assertAngleCloseTo(QUARTER.toAngle(), ANGLE_QUARTER);
	// 135°
	assertAngleCloseTo(QUARTER.multiply(EIGHTH).toAngle(), ANGLE_QUARTER + ANGLE_EIGHTH);

	// 180°
	assertAngleCloseTo(HALF.toAngle(), ANGLE_HALF);
	// 225°
	assertAngleCloseTo(HALF.multiply(EIGHTH).toAngle(), ANGLE_ZERO - ANGLE_QUARTER - ANGLE_EIGHTH);
	// 270°
	assertAngleCloseTo(HALF.multiply(QUARTER).toAngle(), ANGLE_ZERO - ANGLE_QUARTER);
	// 315°
	assertAngleCloseTo(ZERO.multiply(EIGHTH.opposite()).toAngle(), ANGLE_ZERO - ANGLE_EIGHTH);

	// 360°
	assertAngleCloseTo(HALF.multiply(HALF).toAngle(), ANGLE_ZERO);
    }

    @Test
    public void slerp() {
	// special case: opposite rotations, always expect clockwise
	assertCloseTo(ZERO.slerp(HALF, 0.75), QUARTER.multiply(EIGHTH));
	assertCloseTo(HALF.slerp(ZERO, 0.25), HALF.multiply(EIGHTH));

	// normal case: clockwise
	assertCloseTo(QUARTER.slerp(HALF, 0.5), QUARTER.multiply(EIGHTH));
	// normal case: anti-clockwise
	assertCloseTo(QUARTER.multiply(EIGHTH).slerp(ZERO, 2d / 3), EIGHTH);
	// normal case: around zero
	assertCloseTo(EIGHTH.opposite().slerp(EIGHTH, 0.5), ZERO);
    }

    @Test
    public void isClockwise() {
	assertThat(QUARTER.isClockwise(), is(true));
	assertThat(HALF.isClockwise(), is(false));
	assertThat(HALF.multiply(QUARTER).isClockwise(), is(false));
    }

    @Test
    public void compareTo() {
	assertThat(ZERO.compareTo(QUARTER), is(-1));
	assertThat(QUARTER.compareTo(ZERO), is(1));
	assertThat(QUARTER.compareTo(QUARTER), is(0));
    }

    public static void assertCloseTo(Rotation2D actual, Rotation2D expected) {
	assertThat(actual.getVector(), Double2DCloseTo.closeTo(expected.getVector(), MAX_ERROR));
    }

    public static void assertAngleCloseTo(double actual, double expected) {
	assertThat(actual, closeTo(expected, MAX_ERROR));
    }
}
