package de.zmt.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil;
import sim.util.Proxiable;

/**
 * Implementation of {@link StoragePipeline} with a {@link DelayQueue} as the
 * pipeline. Storage capacity and change factors are given by a
 * {@link LimitedStorage} that stores the sum of all {@link DelayedStorage}s
 * queued up there. Only the amount from expired objects can be removed.
 * 
 * @author mey
 * @param
 *            <Q>
 *            the stored {@link Quantity}
 * 
 */
public abstract class AbstractLimitedStoragePipeline<Q extends Quantity>
        implements StoragePipeline<Q>, LimitedStorage<Q>, Proxiable {
    private static final long serialVersionUID = 1L;

    private final LimitedStorage<Q> sum;
    private final Queue<DelayedStorage<Q>> queue = new SerializableDelayQueue<>();

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
     * @return {@link DelayedStorage} which will be added to pipeline.
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
        /*
         * elements that cannot be removed due to sum and need to be put back
         * into queue
         */
        Collection<DelayedStorage<Q>> holdBack = new ArrayList<>();

        while (true) {
            DelayedStorage<Q> head = queue.poll();
            if (head != null) {
                Amount<Q> amount = head.getAmount();
                // subtract amount of this storage from sum
                Amount<Q> required = sum.store(amount.opposite());

                // if amount could be subtracted:
                if (required != null) {
                    // sum the amount received from storage (it is negative)
                    returnAmount = returnAmount.minus(required);
                } else {
                    holdBack.add(head);
                }
            } else {
                // no expired elements
                break;
            }

        }

        queue.addAll(holdBack);
        // clear to prevent ever increasing numeric error
        if (queue.isEmpty()) {
            clear();
        }

        return returnAmount;
    }

    @Override
    public Collection<? extends Storage<Q>> getContent() {
        return Collections.unmodifiableCollection(queue);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code amount} is negative
     */
    @Override
    public ChangeResult<Q> add(Amount<Q> amount) {
        ChangeResult<Q> result = sum.add(amount);
        addToPipeline(result.getStored());

        return result;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code amount} is negative
     */

    @Override
    public Amount<Q> store(Amount<Q> amount) {
        Amount<Q> required = sum.store(amount);
        if (required != null) {
            addToPipeline(amount);
        }
        return required;
    }

    private void addToPipeline(Amount<Q> amount) {
        if (amount.getEstimatedValue() < 0) {
            throw new IllegalArgumentException(amount + " cannot be added, must be positive.");
        }

        // do not add storage for zero amounts, e.g. storage is already at limit
        if (amount.getEstimatedValue() > 0) {
            queue.add(createDelayedStorage(amount));
        }
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
    public double doubleValue() {
        return sum.doubleValue();
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[sum=" + sum + "]";
    }

    /**
     * {@link Storage} implementing {@link Delayed}. {@link #getDelay(TimeUnit)}
     * is passed and should be implemented in child class.
     * 
     * @author mey
     * @param
     *            <Q>
     *            the stored {@link Quantity}
     * 
     */
    public static abstract class DelayedStorage<Q extends Quantity> extends BaseStorage<Q> implements Delayed {
        private static final long serialVersionUID = 1L;

        public DelayedStorage(Amount<Q> amount) {
            super(amount);
        }

        @Override
        public int compareTo(Delayed o) {
            // from TimerQueue#DelayedTimer
            long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
        }
    }

    /**
     * A queue that stores {@link Delayed} elements in order and only returns
     * elements that are expired. Unlike the more complex {@link DelayQueue}
     * this class is not thread-safe but serializable.
     * 
     * @author mey
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

    public class MyPropertiesProxy {
        public Storage<Q> getSum() {
            return sum;
        }

        public Collection<? extends Storage<Q>> getContent() {
            return AbstractLimitedStoragePipeline.this.getContent();
        }

        public int getContentSize() {
            return getContent().size();
        }

        @Override
        public String toString() {
            // will appear in window title when viewing in MASON GUI
            return AbstractLimitedStoragePipeline.this.getClass().getSimpleName();
        }
    }
}
