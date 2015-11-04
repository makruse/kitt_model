package de.zmt.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Storage with an amount that can be changed.
 * 
 * @author mey
 * 
 * @param
 * 	   <Q>
 */
public interface MutableStorage<Q extends Quantity> extends Storage<Q> {
    /**
     * Adds given amount to storage.
     * 
     * @param amountToAdd
     * @return {@link ChangeResult}
     */
    ChangeResult<Q> add(Amount<Q> amountToAdd);

    /**
     * Clear the storage.
     * 
     * @return Amount retrieved by clearing the storage.
     */
    Amount<Q> clear();

    /**
     * Result for a storage change. Includes stored and rejected amount.
     * 
     * @author mey
     * @param
     * 	   <Q>
     *            the {@link Quantity} of this ChangeResult
     * 
     */
    class ChangeResult<Q extends Quantity> {
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
	 * Amount rejected by the storage, for example if a limit is exceeded.
	 * Signs of rejected and added amount match.
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
