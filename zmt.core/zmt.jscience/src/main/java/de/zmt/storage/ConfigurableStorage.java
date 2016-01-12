package de.zmt.storage;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil;
import sim.util.Proxiable;

/**
 * A {@link MutableStorage} that rejects any amount exceeding its limits. Apart
 * from that, there are factors for incoming and outgoing amounts, simulating
 * losses and gains during exchange.
 * 
 * @author mey
 * 
 * @param
 * 	   <Q>
 */
public class ConfigurableStorage<Q extends Quantity> extends BaseStorage<Q> implements LimitedStorage<Q>, Proxiable {
    private static final long serialVersionUID = 1L;

    private static final int DIRECTION_UPPER = 1;
    private static final int DIRECTION_LOWER = -1;
    /**
     * Set to false for preventing error calculation in amount.
     */
    private final boolean storeError;

    /**
     * Create an empty storage (at lower limit) with the given unit.
     * 
     * @param unit
     */
    public ConfigurableStorage(Unit<Q> unit) {
	this(unit, false);
    }

    /**
     * Create an empty storage with the given unit and if storage should take
     * calculation errors into account. Amount is initialized to zero.
     * 
     * @param unit
     * @param storeError
     */
    public ConfigurableStorage(Unit<Q> unit, boolean storeError) {
	this.storeError = storeError;
	setAmount(AmountUtil.zero(unit));
    }

    /** @return True if storage is at its lower limit. */
    @Override
    public final boolean atLowerLimit() {
	return atLimit(getLowerLimit(), DIRECTION_LOWER);
    }

    /** @return True if storage is at its upper limit. */
    @Override
    public final boolean atUpperLimit() {
	return atLimit(getUpperLimit(), DIRECTION_UPPER);
    }

    /**
     * Check if at limit. Because limits can be dynamic, exceeding amounts need
     * to be considered as well.
     * 
     * @param limit
     * @param direction
     *            -1 or 1 indicating direction towards lower or upper limit
     * @return True if amount equals or exceeds given limit.
     */
    private boolean atLimit(Amount<Q> limit, int direction) {
	// no limit set, return false
	if (limit == null) {
	    return false;
	}

	int result = getAmount().compareTo(limit);
	return result == 0 || result == direction;
    }

    /**
     * Zero if not overridden.
     * 
     * @return Minimum storage value or {@code null} for no limit
     */
    protected Amount<Q> getLowerLimit() {
	return AmountUtil.zero(getAmount());
    }

    /**
     * Null (= no limit) if not overridden.
     * 
     * @return Maximum storage value or {@code null} for no limit
     */
    protected Amount<Q> getUpperLimit() {
	return null;
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
     * <p>
     * <b>NOTE:</b> The stored amount includes the factor while the rejected
     * will not:<br>
     * {@code stored + rejected != amountToAdd * factor} if {@code factor != 1}.
     * 
     * @param amountToAdd
     * @return {@link de.zmt.storage.MutableStorage.ChangeResult} including the
     *         amount actually added / removed from storage, and the rejected
     *         one.
     * 
     */
    @Override
    public ChangeResult<Q> add(Amount<Q> amountToAdd) {
	boolean positive = amountToAdd.getEstimatedValue() > 0;
	Amount<Q> limit = positive ? getUpperLimit() : getLowerLimit();
	int direction = positive ? DIRECTION_UPPER : DIRECTION_LOWER;
	double factor = positive ? getFactorIn() : getFactorOut();

	if (atLimit(limit, direction)) {
	    // if already at limit, return full amount
	    return new ChangeResult<>(AmountUtil.zero(amountToAdd), amountToAdd);
	}

	Amount<Q> productAmount = amountToAdd.times(factor);

	if (limit != null) {
	    Amount<Q> capacityLeft = limit.minus(getAmount());
	    Amount<Q> rejectedAmount = productAmount.minus(capacityLeft);

	    // limit exceeded, return rejected amount without the factor
	    if (rejectedAmount.getEstimatedValue() > 0 == positive) {
		Amount<Q> storedAmount = capacityLeft;
		setAmount(limit);
		// remove the factor
		rejectedAmount = rejectedAmount.divide(factor);
		return new ChangeResult<>(storedAmount, rejectedAmount);
	    }
	}

	// limit not exceeded or not set, rejected amount is zero
	Amount<Q> storedAmount = productAmount;
	Amount<Q> rejectedAmount = AmountUtil.zero(getAmount());
	setAmount(getAmount().plus(storedAmount));

	if (!storeError) {
	    // clean error
	    setAmount(Amount.valueOf(getAmount().getEstimatedValue(), getAmount().getUnit()));
	}

	return new ChangeResult<>(storedAmount, rejectedAmount);
    }

    /** Set the storage to its lower limit or to zero if no limit set. */
    @Override
    public Amount<Q> clear() {
	Amount<Q> lowerLimit = getLowerLimit();
	Amount<Q> removedAmount;

	if (lowerLimit != null) {
	    removedAmount = getAmount().minus(getLowerLimit()).times(getFactorOut()).to(getAmount().getUnit());
	    setAmount(getLowerLimit());
	}
	// set storage to zero if no lower limit set
	else {
	    removedAmount = getAmount();
	    setAmount(AmountUtil.zero(getAmount()));
	}

	return removedAmount;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Amount<Q> getAmount() {
	    return ConfigurableStorage.this.getAmount();
	}

	public Amount<Q> getLowerLimit() {
	    return ConfigurableStorage.this.getLowerLimit();
	}

	public Amount<Q> getUpperLimit() {
	    return ConfigurableStorage.this.getUpperLimit();
	}

	public double getFactorIn() {
	    return ConfigurableStorage.this.getFactorIn();
	}

	public double getFactorOut() {
	    return ConfigurableStorage.this.getFactorOut();
	}
    }
}
