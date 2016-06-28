package de.zmt.storage;

import javax.measure.quantity.Quantity;

/**
 * Storage that can store only limited amounts and rejects any excess.
 * 
 * @author mey
 * 
 * @param
 *            <Q>
 */
public interface LimitedStorage<Q extends Quantity> extends MutableStorage<Q> {

    /** @return <code>true</code> if storage is at its lower limit */
    boolean atLowerLimit();

    /** @return <code>true</code> if storage is at its upper limit */
    boolean atUpperLimit();

}