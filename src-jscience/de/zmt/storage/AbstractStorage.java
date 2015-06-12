package de.zmt.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public abstract class AbstractStorage<Q extends Quantity> implements Storage<Q> {
    private static final long serialVersionUID = 1L;

    protected Amount<Q> amount;

    @Override
    public Amount<Q> getAmount() {
	return amount;
    }

    @Override
    public String toString() {
	return amount.toString();
    }
}