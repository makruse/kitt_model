package sim.engine.storage;

import java.util.Queue;
import java.util.concurrent.*;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.AmountUtil;

/**
 * Pipeline containing storage objects with an expiration delay. Storage
 * capacity and change factors are given by an {@link MutableStorage} that
 * stores the sum of all {@link DelayedStorage}s. Only the amount of expired
 * objects can be removed.
 * 
 * @author cmeyer
 * 
 */
public abstract class StoragePipeline<Q extends Quantity> implements
	MutableStorage<Q> {
    private final MutableStorage<Q> sum;
    private final Queue<DelayedStorage<Q>> pipeline = new DelayQueue<DelayedStorage<Q>>();

    /**
     * 
     * @param sum
     *            {@link MutableStorage} defining factors and capacity limits
     *            for this {@link StoragePipeline}
     */
    public StoragePipeline(MutableStorage<Q> sum) {
	this.sum = sum;
    }

    /**
     * Child classes implementing this method specify the {@link DelayedStorage}
     * object to be used within the pipeline.
     * 
     * @param storedAmount
     * @return {@link DelayedStorage} which will be added to pipeline.
     */
    protected abstract DelayedStorage<Q> createDelayedStorage(
	    Amount<Q> storedAmount);

    /**
     * All expired elements removed.
     * 
     * @return amount of expired elements
     */
    public Amount<Q> drainExpired() {
	Amount<Q> returnedAmount = AmountUtil.zero(sum.getAmount());
	while (true) {
	    DelayedStorage<Q> head = pipeline.poll();
	    if (head != null) {
		Amount<Q> amount = head.getAmount();
		// subtract amount of this storage from sum
		Amount<Q> storedAmount = sum.add(amount.opposite())
			.getStoredAmount();
		// sum the opposite storage subtraction to include out factor
		returnedAmount = returnedAmount.plus(storedAmount.opposite());
	    } else {
		break;
	    }
	}

	// clear sum to prevent ever increasing numeric error
	if (pipeline.isEmpty()) {
	    sum.clear();
	}

	return returnedAmount;
    }

    public int getPipelineSize() {
	return pipeline.size();
    }

    @Override
    public Amount<Q> getAmount() {
	return sum.getAmount();
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code amountToAdd} is negative
     */
    @Override
    public ChangeResult<Q> add(Amount<Q> amountToAdd) {
	if (amountToAdd.getEstimatedValue() < 0) {
	    throw new IllegalArgumentException("amountToAdd must be positive.");
	}

	ChangeResult<Q> result = sum.add(amountToAdd);
	pipeline.offer(createDelayedStorage(result.getStoredAmount()));
	return result;
    }

    @Override
    public Amount<Q> clear() {
	pipeline.clear();
	return sum.clear();
    }

    /**
     * {@link Storage} implementing {@link Delayed}. {@link #getDelay(TimeUnit)}
     * is passed and should be implemented in child class.
     * 
     * @author cmeyer
     * 
     */
    public static abstract class DelayedStorage<Q extends Quantity> extends
	    AbstractStorage<Q> implements Delayed {
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

    @Override
    public String toString() {
	return "StoragePipeline [sum amount=" + sum.getAmount()
		+ ", queue size=" + pipeline.size() + "]";
    }
}
