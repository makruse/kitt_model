package sim.engine.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public interface Storage<Q extends Quantity> {
    /**
     * 
     * @return stored amount
     */
    Amount<Q> getAmount();
}