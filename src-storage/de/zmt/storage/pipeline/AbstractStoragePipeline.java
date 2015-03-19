package de.zmt.storage.pipeline;

import java.util.*;
import java.util.concurrent.DelayQueue;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.AmountUtil;
import de.zmt.storage.MutableStorage;

/**
 * Implementation of {@link StoragePipeline} with a {@link DelayQueue} as the
 * pipeline. Storage capacity and change factors are given by a
 * {@link MutableStorage} that stores the sum of all {@link DelayedStorage}s
 * queued up there. Only the amount from expired objects can be removed.
 * 
 * @author cmeyer
 * 
 */
public abstract class AbstractStoragePipeline<Q extends Quantity> implements
	StoragePipeline<Q> {
    private final MutableStorage<Q> sum;
    private final Queue<DelayedStorage<Q>> pipeline = new DelayQueue<DelayedStorage<Q>>();

    /**
     * 
     * @param sum
     *            {@link MutableStorage} defining factors and capacity limits
     *            for this {@link StoragePipeline}
     */
    public AbstractStoragePipeline(MutableStorage<Q> sum) {
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
    @Override
    public Amount<Q> drainExpired() {
	Amount<Q> returnedAmount = AmountUtil.zero(sum.getAmount());
	while (true) {
	    DelayedStorage<Q> head = pipeline.poll();
	    if (head != null) {
		Amount<Q> amount = head.getAmount();
		// subtract amount of this storage from sum
		Amount<Q> storedAmount = sum.add(amount.opposite()).getStored();
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

    @Override
    public Collection<DelayedStorage<Q>> getPipelineContent() {
	return Collections.unmodifiableCollection(pipeline);
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

	// do not add storage for zero amounts
	if (result.getStored().getEstimatedValue() > 0) {
	    pipeline.offer(createDelayedStorage(result.getStored()));
	}

	return result;
    }

    @Override
    public Amount<Q> clear() {
	pipeline.clear();
	return sum.clear();
    }

    @Override
    public String toString() {
	return "StoragePipeline [sum amount=" + sum.getAmount()
		+ ", queue size=" + pipeline.size() + "]";
    }
}
