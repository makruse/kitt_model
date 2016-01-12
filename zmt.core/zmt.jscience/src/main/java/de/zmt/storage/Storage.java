package de.zmt.storage;

import java.io.Serializable;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import sim.util.Valuable;

/**
 * Interface for a simple storage containing an {@link Amount}.
 * 
 * @author mey
 *
 * @param
 * 	   <Q>
 *            type of {@link Quantity}
 */
public interface Storage<Q extends Quantity> extends Serializable, Valuable {
    /**
     * 
     * @return stored amount
     */
    Amount<Q> getAmount();
}