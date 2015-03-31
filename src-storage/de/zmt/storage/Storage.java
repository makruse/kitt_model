package de.zmt.storage;

import java.io.Serializable;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public interface Storage<Q extends Quantity> extends Serializable {
    /**
     * 
     * @return stored amount
     */
    Amount<Q> getAmount();
}