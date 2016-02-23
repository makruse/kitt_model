package de.zmt.storage;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil;
import de.zmt.util.ValuableAmountAdapter;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * A {@link MutableStorage} that rejects any amount exceeding its limits. Apart
 * from that, there are factors for incoming and outgoing amounts, simulating
 * losses and gains during exchange.
 * <p>
 * Gains and losses are applied on the amount that changes the storage.
 * Therefore, in-factors below 1 lead to a loss. They are applied on positive
 * values. Out-factors with the same value lead to a gain because they are
 * applied on negative values. To apply the same gain or the same loss on both
 * ends, one factor needs to be the inverse of the other.
 * 
 * @author mey
 * 
 * @param
 * 	   <Q>
 *            the type of {@link Quantity}
 */
public class ConfigurableStorage<Q extends Quantity> extends BaseStorage<Q> implements LimitedStorage<Q>, Proxiable {
    private static final long serialVersionUID = 1L;

    private static final int DIRECTION_UPPER = 1;
    private static final int DIRECTION_LOWER = -1;

    /**
     * Create an empty storage with the given unit.
     * 
     * @param unit
     */
    public ConfigurableStorage(Unit<Q> unit) {
	super(0, unit);
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
     * Factor applied on the stored amount for positive changes. Values &lt;1
     * will lead to a loss, values &gt;1 to a gain.
     * <p>
     * Neutral if not overridden (factor 1).
     * 
     * @return factor applied when adding storage
     */
    protected double getFactorIn() {
	return 1;
    }

    /**
     * Factor applied on the stored amount for negative changes. Values &lt;1
     * will lead to a gain, values &gt;1 to a loss.
     * <p>
     * Neutral if not overridden (factor 1).
     * 
     * @return factor applied when removing storage
     */
    protected double getFactorOut() {
	return 1;
    }

    /**
     * Adds given amount to the storage without exceeding the limits. If the
     * amount is positive, factor in is applied, otherwise factor out.
     * <p>
     * <b>NOTE:</b> The stored amount includes the factor while the rejected
     * will not:<br>
     * {@code stored + rejected != amountToAdd * factor} if {@code factor != 1}.
     * 
     * @param amount
     * @return {@link de.zmt.storage.MutableStorage.ChangeResult} including the
     *         amount actually added / removed from storage, and the rejected
     *         one
     * 
     */
    @Override
    public ChangeResult<Q> add(Amount<Q> amount) {
	double value = amount.doubleValue(getUnit());

	if (value == 0) {
	    // nothing added
	    return new ChangeResult<>(AmountUtil.zero(amount), amount);
	}

	ChangeData data = new ChangeData(value);
	double productValue = value * data.factor;
	double rejectedValue = checkCapacity(data, productValue);

	// if limit exceeded, return rejected amount without the factor
	if (rejectedValue != 0) {
	    double storedValue = productValue - rejectedValue;
	    setAmount(data.limit);

	    rejectedValue /= data.factor;
	    return new ChangeResult<>(createAmount(storedValue), createAmount(rejectedValue));
	}

	// limit not exceeded or not set, full amount can be stored
	double storedValue = productValue;
	setValue(getValue() + storedValue);

	return new ChangeResult<>(createAmount(storedValue), createAmount(rejectedValue));
    }

    /**
     * Stores exactly the given amount. If it exceeds a limit, <code>null</code>
     * will be returned. The returned amount includes the factor applied to
     * incoming or outgoing amounts. If passed to {@link #add(Amount)}, the
     * stored amount would be equal to the given amount.
     */
    @Override
    public Amount<Q> store(Amount<Q> amount) {
	double value = amount.doubleValue(getUnit());
	if (value == 0) {
	    // nothing added
	    return AmountUtil.zero(amount);
	}

	ChangeData data = new ChangeData(value);
	if (checkCapacity(data, value) != 0) {
	    // amount exceeds capacity, cannot be stored
	    return null;
	}

	// limit not exceeded or not set: change is accepted
	setValue(getValue() + value);
	return createAmount(value / data.factor);
    }

    /**
     * Checks if given value would exceed limits when added and return rejected.
     * 
     * @param data
     * @param value
     * @return value exceeding limits
     */
    private double checkCapacity(ChangeData data, double value) {
	if (atLimit(data.limit, data.direction)) {
	    // if already at limit: nothing can be accepted
	    return value;
	}

	if (data.limit != null) {
	    double capacityLeft = data.limit.doubleValue(getUnit()) - getValue();
	    double rejectedValue = value - capacityLeft;

	    // limit exceeded, return capacity left
	    if (rejectedValue > 0 == data.positive) {
		return rejectedValue;
	    }
	}

	// value does not exceed limits
	return 0;
    }

    /**
     * @param value
     * @return {@link Amount} with given value with the unit of this storage
     */
    private Amount<Q> createAmount(double value) {
	return Amount.valueOf(value, getUnit());
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

    private class ChangeData {
	private final boolean positive;
	private final Amount<Q> limit;
	private final int direction;
	private final double factor;

	public ChangeData(double value) {
	    positive = value > 0;
	    limit = positive ? getUpperLimit() : getLowerLimit();
	    direction = positive ? DIRECTION_UPPER : DIRECTION_LOWER;
	    factor = positive ? getFactorIn() : getFactorOut();
	}
    }

    public class MyPropertiesProxy {
	public Valuable getAmount() {
	    return ValuableAmountAdapter.wrap(ConfigurableStorage.this.getAmount());
	}

	public Valuable getLowerLimit() {
	    return ValuableAmountAdapter.wrap(ConfigurableStorage.this.getLowerLimit());
	}

	public Valuable getUpperLimit() {
	    return ValuableAmountAdapter.wrap(ConfigurableStorage.this.getUpperLimit());
	}

	public double getFactorIn() {
	    return ConfigurableStorage.this.getFactorIn();
	}

	public double getFactorOut() {
	    return ConfigurableStorage.this.getFactorOut();
	}

	@Override
	public String toString() {
	    // will appear in window title when viewing in MASON GUI
	    return ConfigurableStorage.this.getClass().getSimpleName();
	}
    }
}
