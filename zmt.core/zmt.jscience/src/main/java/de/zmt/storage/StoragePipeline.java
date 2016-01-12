package de.zmt.storage;

import java.util.Collection;
import java.util.concurrent.*;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Pipeline containing storage objects with an expiration delay.
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

    /**
     * {@link Storage} implementing {@link Delayed}. {@link #getDelay(TimeUnit)}
     * is passed and should be implemented in child class.
     * 
     * @author mey
     * @param
     * 	   <Q>
     *            the stored {@link Quantity}
     * 
     */
    public static abstract class DelayedStorage<Q extends Quantity> extends BaseStorage<Q> implements Delayed {
	private static final long serialVersionUID = 1L;

	public DelayedStorage(Amount<Q> amount) {
	    this.setAmount(amount);
	}

	@Override
	public int compareTo(Delayed o) {
	    // from TimerQueue#DelayedTimer
	    long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
	    return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
	}
    }
}