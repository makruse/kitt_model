package de.zmt.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import sim.util.Valuable;

/**
 * Basic implementation of {@link Storage}.
 * 
 * @author mey
 *
 * @param
 * 	   <Q>
 *            type of {@link Quantity}
 */
public class BaseStorage<Q extends Quantity> implements Storage<Q>, Valuable {
    private static final long serialVersionUID = 1L;

    private Amount<Q> amount;

    @Override
    public Amount<Q> getAmount() {
	return amount;
    }

    protected void setAmount(Amount<Q> amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[amount=" + getAmount() + "]";
    }

    @Override
    public double doubleValue() {
	return amount.getEstimatedValue();
    }
}