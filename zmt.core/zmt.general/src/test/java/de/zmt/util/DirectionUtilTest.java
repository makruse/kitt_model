package de.zmt.util;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Double2DCloseTo;
import org.hamcrest.Matcher;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

public class DirectionUtilTest {

    private static final double QUARTER_REVOLUTION = 0.5 * Math.PI;
    private static final double MAX_ERROR = 1E-15d;
    private static final int GENERATE_ITERATIONS = 100;

    @Test
    public void angleBetween() {
	assertThat(DirectionUtil.angleBetween(EAST, SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(SOUTH, WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(WEST, NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(NORTH, EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));

	assertThat(DirectionUtil.angleBetween(EAST, NORTH),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(NORTH, WEST),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(WEST, SOUTH),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(SOUTH, EAST),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));

	// same direction with different length
	assertThat(DirectionUtil.angleBetween(EAST.multiply(2), EAST),
		is(closeTo(0d, MAX_ERROR)));

	// non-unit vector
	assertThat(DirectionUtil.angleBetween(EAST.multiply(0.8), SOUTH.multiply(2.6)),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
    }

    @Test
    public void angleBetweenFast() {
	assertThat(DirectionUtil.angleBetweenFast(EAST, SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(SOUTH, WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(WEST, NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(NORTH, EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));

	assertThat(DirectionUtil.angleBetweenFast(EAST, NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(NORTH, WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(WEST, SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(SOUTH, EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
    }

    @Test
    public void fromAngle() {
	double right = 0 * Math.PI;
	double down = 0.5 * Math.PI;
	double left = 1 * Math.PI;
	double up = 1.5 * Math.PI;

	assertThat(DirectionUtil.fromAngle(right), is(closeToWithError(EAST)));
	assertThat(DirectionUtil.fromAngle(down), is(closeToWithError(SOUTH)));
	assertThat(DirectionUtil.fromAngle(left), is(closeToWithError(WEST)));
	assertThat(DirectionUtil.fromAngle(up), is(closeToWithError(NORTH)));
    }

    @Test
    public void rotate() {
	assertThat(DirectionUtil.rotate(EAST, QUARTER_REVOLUTION), is(closeToWithError(SOUTH)));
	assertThat(DirectionUtil.rotate(SOUTH, QUARTER_REVOLUTION), is(closeToWithError(WEST)));
	assertThat(DirectionUtil.rotate(WEST, QUARTER_REVOLUTION), is(closeToWithError(NORTH)));
	assertThat(DirectionUtil.rotate(NORTH, QUARTER_REVOLUTION), is(closeToWithError(EAST)));

	assertThat(DirectionUtil.rotate(EAST, -QUARTER_REVOLUTION), is(closeToWithError(NORTH)));
	assertThat(DirectionUtil.rotate(NORTH, -QUARTER_REVOLUTION), is(closeToWithError(WEST)));
	assertThat(DirectionUtil.rotate(WEST, -QUARTER_REVOLUTION), is(closeToWithError(SOUTH)));
	assertThat(DirectionUtil.rotate(SOUTH, -QUARTER_REVOLUTION), is(closeToWithError(EAST)));
    }

    @Test
    public void generate() {
	MersenneTwisterFast random = new MersenneTwisterFast(0);

	Double2D generatedLast = NEUTRAL;
	for (int i = 0; i < GENERATE_ITERATIONS; i++) {
	    Double2D generated = DirectionUtil.generate(random);

	    assertThat(generated, is(not(generatedLast)));
	    assertThat(generated, is(closeToWithError(generated.normalize())));

	    generatedLast = generated;
	}
    }

    public static Matcher<Double2D> closeToWithError(Double2D operand) {
	return Double2DCloseTo.closeTo(operand, MAX_ERROR);
    }
}
