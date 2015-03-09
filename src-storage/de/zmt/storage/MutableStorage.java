package de.zmt.storage;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Storage with an amount that can be changed.
 * 
 * @author cmeyer
 * 
 * @param <Q>
 */
public interface MutableStorage<Q extends Quantity> extends Storage<Q> {
    /**
     * Adds given amount to storage.
     * 
     * @param amount
     * @return {@link ChangeResult}
     */
    ChangeResult<Q> add(Amount<Q> amountToAdd);

    /**
     * Clear the storage.
     * 
     * @return Amount retrieved by clearing the storage.
     */
    public Amount<Q> clear();

    /**
     * Result for a storage change. Includes stored and rejected amount.
     * 
     * @author cmeyer
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
	 * This amount can be positive or negative and differs from the added
	 * amount, if a factor is exceeded or a loss factor is applied.
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
	    return "ChangeResult [stored=" + stored + ", rejected=" + rejected
		    + "]";
	}
    }
}
