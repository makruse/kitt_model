package de.zmt.util;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.*;
import org.junit.Test;

import sim.util.Double2D;

public class DirectionUtilTest {

    private static final double QUARTER_REVOLUTION = 0.5 * Math.PI;
    private static final double MAX_ERROR = 1E-15d;

    @Test
    public void angleBetween() {
	assertThat(DirectionUtil.angleBetween(DIRECTION_RIGHT, DIRECTION_DOWN),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_DOWN, DIRECTION_LEFT),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_LEFT, DIRECTION_UP),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_UP, DIRECTION_RIGHT),
		is(closeTo(QUARTER_REVOLUTION, MAX_ERROR)));

	assertThat(DirectionUtil.angleBetween(DIRECTION_RIGHT, DIRECTION_UP),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_UP, DIRECTION_LEFT),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_LEFT, DIRECTION_DOWN),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
	assertThat(DirectionUtil.angleBetween(DIRECTION_DOWN, DIRECTION_RIGHT),
		is(closeTo(-QUARTER_REVOLUTION, MAX_ERROR)));
    }

    @Test
    public void fromAngle() {
	double right = 0 * Math.PI;
	double down = 0.5 * Math.PI;
	double left = 1 * Math.PI;
	double up = 1.5 * Math.PI;

	assertThat(DirectionUtil.fromAngle(right), is(closeToWithError(DIRECTION_RIGHT)));
	assertThat(DirectionUtil.fromAngle(down), is(closeToWithError(DIRECTION_DOWN)));
	assertThat(DirectionUtil.fromAngle(left), is(closeToWithError(DIRECTION_LEFT)));
	assertThat(DirectionUtil.fromAngle(up), is(closeToWithError(DIRECTION_UP)));
    }

    @Test
    public void rotate() {
	assertThat(DirectionUtil.rotate(DIRECTION_RIGHT, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_DOWN)));
	assertThat(DirectionUtil.rotate(DIRECTION_DOWN, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_LEFT)));
	assertThat(DirectionUtil.rotate(DIRECTION_LEFT, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_UP)));
	assertThat(DirectionUtil.rotate(DIRECTION_UP, QUARTER_REVOLUTION), is(closeToWithError(DIRECTION_RIGHT)));
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
