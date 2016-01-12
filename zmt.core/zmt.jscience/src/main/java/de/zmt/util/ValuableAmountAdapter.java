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

    public ValuableAmountAdapter(Amount<?> amount) {
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
}
