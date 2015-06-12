package de.zmt.storage;

import javax.measure.quantity.Quantity;

/**
 * Storage that can store only limited amounts and rejects any excess.
 * 
 * @author cmeyer
 * 
 * @param <Q>
 */
public interface LimitedStorage<Q extends Quantity> extends MutableStorage<Q> {

    /** @return True if storage is at its lower limit. */
    boolean atLowerLimit();

    /** @return True if storage is at its upper limit. */
    boolean atUpperLimit();

}