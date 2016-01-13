package de.zmt.storage;

import java.util.Collection;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Pipeline containing storage objects which can be drained when expired.
 * 
 * @see AbstractLimitedStoragePipeline
 * @author mey
 * @param
 * 	   <Q>
 *            the stored {@link Quantity}
 * 
 */
public interface StoragePipeline<Q extends Quantity> extends MutableStorage<Q> {

    /**
     * All expired elements removed.
     * 
     * @return amount of expired elements
     */
    Amount<Q> drainExpired();

    /** @return content of pipeline as {@link Collection} */
    Collection<? extends Storage<Q>> getContent();
}