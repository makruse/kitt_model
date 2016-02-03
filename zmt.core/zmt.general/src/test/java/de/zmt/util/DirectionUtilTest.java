package de.zmt.util;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;

public class DirectionUtilTest {

    private static final double QUARTER_REVOLUTION = 0.5 * Math.PI;
    private static final double MAX_ERROR = 1E-15d;
    private static final int GENERATE_ITERATIONS = 100;

    @Test
    public void angleBetween() {
	assertThat(DirectionUtil.angleBetween(DIRECTION_EAST, DIRECTION_SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_SOUTH, DIRECTION_WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_WEST, DIRECTION_NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_NORTH, DIRECTION_EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));

	assertThat(DirectionUtil.angleBetween(DIRECTION_EAST, DIRECTION_NORTH),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_NORTH, DIRECTION_WEST),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_WEST, DIRECTION_SOUTH),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_SOUTH, DIRECTION_EAST),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));

	// same direction with different length
	assertThat(DirectionUtil.angleBetween(DIRECTION_EAST.multiply(2), DIRECTION_EAST),
		is(closeTo(0d, MAX_ERROR)));

	// non-unit vector
	assertThat(DirectionUtil.angleBetween(DIRECTION_EAST.multiply(0.8), DIRECTION_SOUTH.multiply(2.6)),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
    }

    @Test
    public void angleBetweenFast() {
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_EAST, DIRECTION_SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_SOUTH, DIRECTION_WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_WEST, DIRECTION_NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_NORTH, DIRECTION_EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));

	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_EAST, DIRECTION_NORTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_NORTH, DIRECTION_WEST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_WEST, DIRECTION_SOUTH),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetweenFast(DIRECTION_SOUTH, DIRECTION_EAST),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
    }

    @Test
    public void fromAngle() {
	double right = 0 * Math.PI;
	double down = 0.5 * Math.PI;
	double left = 1 * Math.PI;
	double up = 1.5 * Math.PI;

	assertThat(DirectionUtil.fromAngle(right), is(closeToWithError(DIRECTION_EAST)));
	assertThat(DirectionUtil.fromAngle(down), is(closeToWithError(DIRECTION_SOUTH)));
	assertThat(DirectionUtil.fromAngle(left), is(closeToWithError(DIRECTION_WEST)));
	assertThat(DirectionUtil.fromAngle(up), is(closeToWithError(DIRECTION_NORTH)));
    }

    @Test
    public void rotate() {
	assertThat(DirectionUtil.rotate(DIRECTION_EAST, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_SOUTH)));
	assertThat(DirectionUtil.rotate(DIRECTION_SOUTH, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_WEST)));
	assertThat(DirectionUtil.rotate(DIRECTION_WEST, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_NORTH)));
	assertThat(DirectionUtil.rotate(DIRECTION_NORTH, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_EAST)));

	assertThat(DirectionUtil.rotate(DIRECTION_EAST, -QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_NORTH)));
	assertThat(DirectionUtil.rotate(DIRECTION_NORTH, -QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_WEST)));
	assertThat(DirectionUtil.rotate(DIRECTION_WEST, -QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_SOUTH)));
	assertThat(DirectionUtil.rotate(DIRECTION_SOUTH, -QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_EAST)));
    }

    @Test
    public void generate() {
	MersenneTwisterFast random = new MersenneTwisterFast(0);

	Double2D generatedLast = DIRECTION_NEUTRAL;
	for (int i = 0; i < GENERATE_ITERATIONS; i++) {
	    Double2D generated = DirectionUtil.generate(random);

	    assertThat(generated, is(not(generatedLast)));
	    assertThat(generated, is(closeToWithError(generated.normalize())));

	    generatedLast = generated;
	}
    }

    public static Matcher<Double2D> closeToWithError(Double2D operand) {
	return IsCloseTo.closeTo(operand, MAX_ERROR);
    }

    /**
     * {@code IsCloseTo} implementation for {@link Double2D}.
     * 
     * @see org.hamcrest.number.IsCloseTo
     */
    private static class IsCloseTo extends TypeSafeMatcher<Double2D> {
	private final double delta;
	private final Double2D value;

	public IsCloseTo(Double2D value, double error) {
	    this.delta = error;
	    this.value = value;
	}

	@Override
	public boolean matchesSafely(Double2D item) {
	    return actualDelta(item) <= 0.0;
	}

	@Override
	public void describeMismatchSafely(Double2D item, Description mismatchDescription) {
	    mismatchDescription.appendValue(item).appendText(" differed by ").appendValue(actualDelta(item));
	}

	@Override
	public void describeTo(Description description) {
	    description.appendText("a numeric value within ").appendValue(delta).appendText(" of ").appendValue(value);
	}

	private double actualDelta(Double2D item) {
	    return (Math.abs((item.x - value.x)) - delta) + (Math.abs((item.y - value.y)) - delta);
	}

	/**
	 * Creates a matcher of {@link Double2D}s that matches when an examined
	 * {@code Double2D} is equal to the specified <code>operand</code> with
	 * both coordinates, within a range of +/- <code>error</code>.
	 * 
	 * @param operand
	 *            the expected value of matching {@code Double2D}s
	 * @param error
	 *            the delta (+/-) within which matches will be allowed
	 * @return created matcher
	 */
	@Factory
	public static Matcher<Double2D> closeTo(Double2D operand, double error) {
	    return new IsCloseTo(operand, error);
	}

    }
}
