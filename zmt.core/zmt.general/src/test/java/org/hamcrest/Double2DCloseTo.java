package org.hamcrest;

import sim.util.Double2D;

/**
 * {@code IsCloseTo} implementation for {@link Double2D}.
 * 
 * @see org.hamcrest.number.IsCloseTo
 */
public class Double2DCloseTo extends TypeSafeMatcher<Double2D> {
    private static final double DEFAULT_ERROR = 1E-14d;

    private final double delta;
    private final Double2D value;

    public Double2DCloseTo(Double2D value, double error) {
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
        return new Double2DCloseTo(operand, error);
    }

    /**
     * Creates a matcher of {@link Double2D}s that matches when an examined
     * {@code Double2D} is equal to the specified <code>operand</code> with both
     * coordinates, within a small range of error.
     * 
     * @param operand
     *            the expected value of matching {@code Double2D}s
     * @return created matcher
     */
    @Factory
    public static Matcher<Double2D> closeTo(Double2D operand) {
	return new Double2DCloseTo(operand, DEFAULT_ERROR);
    }

}