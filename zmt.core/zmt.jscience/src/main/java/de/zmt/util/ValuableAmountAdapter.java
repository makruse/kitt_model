package de.zmt.util;

import org.jscience.physics.amount.Amount;

import sim.util.Valuable;

/**
 * Simple adapter class wrapping around an {@link Amount} to be used as a
 * {@link Valuable}.
 * 
 * @author mey
 */
public class ValuableAmountAdapter implements Valuable {
    private final Amount<?> amount;

    private ValuableAmountAdapter(Amount<?> amount) {
	super();
	this.amount = amount;
    }

    @Override
    public double doubleValue() {
	return amount.getEstimatedValue();
    }
    
    @Override
    public String toString() {
	return amount.toString();
    }

    /**
     * Returns {@code amount} wrapped into a {@link ValuableAmountAdapter} or
     * <code>null</code> if {@code amount} is null.
     * 
     * @param amount
     * @return wrapped {@code amount}
     */
    public static ValuableAmountAdapter wrap(Amount<?> amount) {
	if (amount != null) {
	    return new ValuableAmountAdapter(amount);
	}
	return null;
    }
}
