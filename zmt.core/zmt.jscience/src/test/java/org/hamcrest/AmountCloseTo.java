package org.hamcrest;

import static org.hamcrest.Matchers.closeTo;

import javax.measure.unit.Unit;

import org.hamcrest.number.IsCloseTo;
import org.jscience.physics.amount.Amount;

/**
 * {@code IsCloseTo} implementation for {@link Amount}.
 * 
 * @see IsCloseTo
 * @author mey
 *
 */
public class AmountCloseTo extends TypeSafeMatcher<Amount<?>> {
    private static final double DEFAULT_ERROR = 1E-14d;

    private final Amount<?> amount;
    private final double error;

    public AmountCloseTo(Class<?> expectedType, Amount<?> amount, double error) {
	super(expectedType);
	this.amount = amount;
	this.error = error;
    }

    @Override
    public void describeTo(Description description) {
	closeTo(amount.getEstimatedValue(), error).describeTo(description);
	description.appendText(" ").appendValue(amount.getUnit());
    }

    @Override
    protected boolean matchesSafely(Amount<?> item) {
	Unit<?> unit = amount.getUnit();
	return Matchers.closeTo(amount.getEstimatedValue(), error)
		.matches(item.to(unit).getEstimatedValue());
    }

    /**
     * 
     * @param amount
     * @return {@link Matcher} with default error
     */
    @Factory
    public static Matcher<Amount<?>> amountCloseTo(final Amount<?> amount) {
	return amountCloseTo(amount, DEFAULT_ERROR);
    }

    @Factory
    public static Matcher<Amount<?>> amountCloseTo(final Amount<?> amount, double error) {
	return new AmountCloseTo(Amount.class, amount, error);
    }
}