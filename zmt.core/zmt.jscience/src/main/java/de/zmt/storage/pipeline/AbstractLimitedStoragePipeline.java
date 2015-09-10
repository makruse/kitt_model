package de.zmt.storage.pipeline;

import java.util.*;
import java.util.concurrent.*;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import de.zmt.storage.LimitedStorage;
import de.zmt.util.AmountUtil;

/**
 * Implementation of {@link StoragePipeline} with a {@link DelayQueue} as the
 * pipeline. Storage capacity and change factors are given by a
 * {@link LimitedStorage} that stores the sum of all
 * {@link de.zmt.storage.pipeline.StoragePipeline.DelayedStorage}s queued up
 * there. Only the amount from expired objects can be removed.
 * 
 * @author cmeyer
 * @param
 * 	   <Q>
 *            the stored {@link Quantity}
 * 
 */
public abstract class AbstractLimitedStoragePipeline<Q extends Quantity>
	implements StoragePipeline<Q>, LimitedStorage<Q> {
    private static final long serialVersionUID = 1L;

    private final LimitedStorage<Q> sum;
    private final Queue<DelayedStorage<Q>> queue = new SerializableDelayQueue<DelayedStorage<Q>>();

    /**
     * 
     * @param sum
     *            {@link LimitedStorage} defining factors and capacity limits
     *            for this {@link StoragePipeline}
     */
    public AbstractLimitedStoragePipeline(LimitedStorage<Q> sum) {
	this.sum = sum;
	syncQueue();
    }

    /** Clears queue and add an amount equal to sum. */
    private void syncQueue() {
	queue.clear();
	Amount<Q> amount = sum.getAmount();

	// if there is an amount in sum, add it to pipeline
	if (amount.getEstimatedValue() > 0) {
	    queue.offer(createDelayedStorage(amount));
	} else if (amount.getEstimatedValue() < 0) {
	    throw new IllegalArgumentException("Negative amounts are not supported.");
	}
    }

    /**
     * Child classes implementing this method specify the storage object to be
     * used within the pipeline.
     * 
     * @param storedAmount
     * @return {@link de.zmt.storage.pipeline.StoragePipeline.DelayedStorage}
     *         which will be added to pipeline.
     */
    protected abstract DelayedStorage<Q> createDelayedStorage(Amount<Q> storedAmount);

    @Override
    public boolean atLowerLimit() {
	return sum.atLowerLimit();
    }

    @Override
    public boolean atUpperLimit() {
	return sum.atUpperLimit();
    }

    /**
     * All expired elements removed.
     * 
     * @return amount of expired elements
     */
    @Override
    public Amount<Q> drainExpired() {
	Amount<Q> returnAmount = AmountUtil.zero(sum.getAmount());
	while (true) {
	    DelayedStorage<Q> head = queue.poll();
	    if (head != null) {
		Amount<Q> amount = head.getAmount();
		// subtract amount of this storage from sum
		ChangeResult<Q> changeResult = sum.add(amount.opposite());

		// sum the amount received from storage
		returnAmount = returnAmount.plus(amount.plus(changeResult.getRejected()));
	    } else {
		// no expired elements
		break;
	    }
	}

	// clear to prevent ever increasing numeric error
	if (queue.isEmpty()) {
	    clear();
	}

	return returnAmount;
    }

    @Override
    public Collection<DelayedStorage<Q>> getContent() {
	return Collections.unmodifiableCollection(queue);
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

	// do not add storage for zero amounts, e.g. storage is already at limit
	if (result.getStored().getEstimatedValue() > 0) {
	    queue.offer(createDelayedStorage(result.getStored()));
	}

	return result;
    }

    @Override
    public Amount<Q> clear() {
	Amount<Q> clearAmount = sum.clear();
	syncQueue();
	return clearAmount;
    }

    @Override
    public Amount<Q> getAmount() {
	return sum.getAmount();
    }

    @Override
    public String toString() {
	return "StoragePipeline [sum amount=" + sum.getAmount() + ", queue size=" + queue.size() + "]";
    }

    /**
     * A queue that stores {@link Delayed} elements in order and only returns
     * elements that are expired. Unlike the more complex {@link DelayQueue}
     * this class is not thread-safe but serializable.
     * 
     * @author cmeyer
     * 
     * @param <E>
     *            type of elements held in the queue
     */
    private static class SerializableDelayQueue<E extends Delayed> extends PriorityQueue<E> {
	private static final long serialVersionUID = 1L;

	/**
	 * Retrieves and removes the head of this queue, or returns
	 * <tt>null</tt> if this queue has no elements with an expired delay.
	 * 
	 * @return the head of this queue, or <tt>null</tt> if this queue has no
	 *         elements with an expired delay
	 * @see DelayQueue#poll()
	 */
	@Override
	public E poll() {
	    E first = peek();
	    if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
		return null;
	    } else {
		return super.poll();
	    }
	}
    }
}
