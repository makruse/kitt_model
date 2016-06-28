package de.zmt.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Storage with an amount that can be changed. There are two different methods
 * for changing the stored amount. {@link #add(Amount)} may store the given
 * amount differently and report about it while {@link #store(Amount)} changes
 * the storage by exactly the given amount report about the one required and may
 * fail.
 * 
 * @author mey
 * 
 * @param
 *            <Q>
 *            the type of {@link Quantity} stored
 */
public interface MutableStorage<Q extends Quantity> extends Storage<Q> {
    /**
     * Adds given amount to the storage. Use a negative value to remove from
     * storage. The stored amount added may differ and can be rejected partly or
     * entirely. This is reflected within the returned {@link ChangeResult}.
     * 
     * @param amount
     *            the amount offered to the storage
     * @return {@link ChangeResult}
     */
    ChangeResult<Q> add(Amount<Q> amount);

    /**
     * Stores given amount. Use a negative value to remove from storage. The
     * amount added to the storage will be exactly like the given. A returned
     * amount reflects the cost of storing the given amount and both may differ.
     * 
     * @param amount
     *            the amount the storage is changed by
     * @return the amount required to store the given amount or
     *         <code>null</code> if it is rejected
     */
    Amount<Q> store(Amount<Q> amount);

    /**
     * Clears the storage.
     * 
     * @return Amount retrieved by clearing the storage.
     */
    Amount<Q> clear();

    /**
     * Result for a storage change. Includes stored and rejected amount.
     * 
     * @author mey
     * @param
     *            <Q>
     *            the {@link Quantity} of this ChangeResult
     * 
     */
    public class ChangeResult<Q extends Quantity> {
        private final Amount<Q> stored;
        private final Amount<Q> rejected;

        public ChangeResult(Amount<Q> stored, Amount<Q> rejected) {
            this.stored = stored;
            this.rejected = rejected;
        }

        /**
         * Amount that the storage is changed by. It can be positive or negative
         * and may differ from the added amount, for example if a limit is
         * exceeded or a loss factor is applied.
         * 
         * @return stored amount
         */
        public Amount<Q> getStored() {
            return stored;
        }

        /**
         * Returns amount rejected by the storage, for example if a limit is
         * exceeded. Signs of rejected and added amount match.
         * 
         * @return rejected amount
         */
        public Amount<Q> getRejected() {
            return rejected;
        }

        @Override
        public String toString() {
            return "ChangeResult [stored=" + stored + ", rejected=" + rejected + "]";
        }
    }
}
