package de.zmt.storage.pipeline;

import java.util.Collection;
import java.util.concurrent.*;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import de.zmt.storage.*;

/**
 * Pipeline containing storage objects with an expiration delay.
 * 
 * @see AbstractLimitedStoragePipeline
 * @author cmeyer
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
    Collection<DelayedStorage<Q>> getContent();

    /**
     * {@link Storage} implementing {@link Delayed}. {@link #getDelay(TimeUnit)}
     * is passed and should be implemented in child class.
     * 
     * @author cmeyer
     * 
     */
    public static abstract class DelayedStorage<Q extends Quantity> extends
	    AbstractStorage<Q> implements Delayed {
	private static final long serialVersionUID = 1L;

	public DelayedStorage(Amount<Q> amount) {
	    this.amount = amount;
	}

	@Override
	public int compareTo(Delayed o) {
	    // from TimerQueue#DelayedTimer
	    long diff = getDelay(TimeUnit.NANOSECONDS)
		    - o.getDelay(TimeUnit.NANOSECONDS);
	    return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
	}
    }
}