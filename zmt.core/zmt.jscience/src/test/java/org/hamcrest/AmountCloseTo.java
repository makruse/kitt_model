package org.hamcrest;

import static org.hamcrest.Matchers.closeTo;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jscience.physics.amount.Amount;

public class AmountCloseTo extends TypeSafeMatcher<Amount<?>> {
    private final Amount<?> amount;
    private static final double MAX_ERROR = 1E-14d;

    AmountCloseTo(Class<?> expectedType, Amount<?> amount) {
	super(expectedType);
	this.amount = amount;
    }

    @Override
    public void describeTo(Description description) {
	closeTo(amount.getEstimatedValue(), MAX_ERROR).describeTo(description);
    }

    @Override
    protected boolean matchesSafely(Amount<?> item) {
	return Matchers.closeTo(amount.getEstimatedValue(), MAX_ERROR)
		.matches(item.to(amount.getUnit()).getEstimatedValue());
    }

    @Factory
    public static org.hamcrest.Matcher<Amount<?>> amountCloseTo(final Amount<?> amount) {
	return new AmountCloseTo(Amount.class, amount);
    }
}