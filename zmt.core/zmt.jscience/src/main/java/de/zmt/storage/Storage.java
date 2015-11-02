package de.zmt.storage;

import java.io.Serializable;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Interface for a simple storage containing an {@link Amount}.
 * 
 * @author mey
 *
 * @param
 * 	   <Q>
 *            type of {@link Quantity}
 */
public interface Storage<Q extends Quantity> extends Serializable {
    /**
     * 
     * @return stored amount
     */
    Amount<Q> getAmount();
}