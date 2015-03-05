package sim.engine.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public abstract class AbstractStorage<Q extends Quantity> implements Storage<Q> {
    protected Amount<Q> amount;

    @Override
    public Amount<Q> getAmount() {
	return amount;
    }

    @Override
    public String toString() {
	return "Storage [amount=" + amount + "]";
    }
}