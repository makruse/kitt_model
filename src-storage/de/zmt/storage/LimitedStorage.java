package de.zmt.storage;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.AmountUtil;

/**
 * A {@link MutableStorage} that rejects any amount exceeding its limits. Apart
 * from that, there are factors for incoming and outgoing amounts, simulating
 * losses and gains during exchange.
 * 
 * @author cmeyer
 * 
 * @param <Q>
 */
public class LimitedStorage<Q extends Quantity> extends AbstractStorage<Q>
	implements MutableStorage<Q> {
    /**
     * Set to false for preventing error calculation in amount.
     */
    private final boolean storeError;

    /**
     * Create an empty storage (at lower limit) with the given unit.
     * 
     * @param unit
     */
    public LimitedStorage(Unit<Q> unit) {
	this(unit, false);
    }

    /**
     * Create an empty storage (at lower limit) with the given unit and if
     * storage should take calculation errors into account.
     * 
     * @param unit
     * @param storeError
     */
    public LimitedStorage(Unit<Q> unit, boolean storeError) {
	this.amount = AmountUtil.zero(unit);
	amount = getLowerLimit();
	this.storeError = storeError;
    }

    /** Set the storage to its lower limit. */
    @Override
    public Amount<Q> clear() {
	Amount<Q> removedAmount = amount.minus(getLowerLimit()).times(
		getFactorOut());
	amount = getLowerLimit();
	return removedAmount;
    }

    /** @return True if storage is at its lower limit. */
    public boolean atLowerLimit() {
	return amount.equals(getLowerLimit())
		|| amount.isLessThan(getLowerLimit());
    }

    /** @return True if storage is at its upper limit. */
    public boolean atUpperLimit() {
	return amount.equals(getUpperLimit())
		|| amount.isGreaterThan(getUpperLimit());
    }

    /**
     * Zero if not overridden.
     * 
     * @return Minimum storage value
     */
    protected Amount<Q> getLowerLimit() {
	return AmountUtil.zero(amount);
    }

    /**
     * No upper limit if not overridden ({@link Long#MAX_VALUE}).
     * 
     * @return Maximum storage value
     */
    protected Amount<Q> getUpperLimit() {
	return Amount.valueOf(Long.MAX_VALUE, amount.getUnit());
    }

    /**
     * Factor applied on the stored amount for positive changes. Values <1 will
     * lead to a loss, values >1 to a gain.
     * <p>
     * Neutral if not overridden (factor 1).
     * 
     * @return factor applied when adding storage
     */
    protected double getFactorIn() {
	return 1;
    }

    /**
     * Factor applied on the stored amount for negative changes. Values <1 will
     * lead to a gain, values >1 to a loss.
     * <p>
     * Neutral if not overridden (factor 1).
     * 
     * @return factor applied when removing storage
     */
    protected double getFactorOut() {
	return 1;
    }

    /**
     * Add given amount without exceeding the limits. Use a negative value to
     * remove from storage. If the amount is positive, stored amount will be
     * decreased by loss factor, otherwise the removed amount increases.
     * 
     * @param amountToAdd
     * @return {@link ChangeResult} including the amount actually added /
     *         removed from storage, and the rejected one.
     * 
     */
    @Override
    public ChangeResult<Q> add(Amount<Q> amountToAdd) {
	boolean positive = amountToAdd.getEstimatedValue() > 0;
	Amount<Q> limit = positive ? getUpperLimit() : getLowerLimit();
	double factor = positive ? getFactorIn() : getFactorOut();

	// check if at limit first
	if (atUpperLimit() && positive || atLowerLimit() && !positive) {
	    // return full amount
	    return new ChangeResult<Q>(AmountUtil.zero(amountToAdd),
		    amountToAdd);
	}

	Amount<Q> capacityLeft = limit.minus(amount);
	Amount<Q> productAmount = amountToAdd.times(factor);
	Amount<Q> rejectedAmount = productAmount.minus(capacityLeft);
	Amount<Q> storedAmount;

	// limit exceeded, return rejected amount without the factor
	if (rejectedAmount.getEstimatedValue() > 0 == positive) {
	    storedAmount = capacityLeft;
	    amount = limit;
	    rejectedAmount = rejectedAmount.divide(factor);
	}
	// limit not exceeded, rejected amount is zero
	else {
	    storedAmount = productAmount;
	    if (storeError) {
		amount = amount.plus(storedAmount);
	    } else {
		double sum = amount.getEstimatedValue()
			+ storedAmount.doubleValue(amount.getUnit());
		// prevent amount from storing error
		amount = Amount.valueOf(sum, amount.getUnit());
	    }
	    rejectedAmount = AmountUtil.zero(amount);
	}

	return new ChangeResult<Q>(storedAmount, rejectedAmount);
    }

    @Override
    public String toString() {
	return "LimitedStorage [amount=" + amount + ", getLowerLimit()="
		+ getLowerLimit() + ", getUpperLimit()=" + getUpperLimit()
		+ "]";
    }

}
