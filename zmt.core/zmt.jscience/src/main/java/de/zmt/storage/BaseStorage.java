package de.zmt.storage;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import sim.util.Valuable;

/**
 * Basic implementation of {@link Storage}. Stores an amount without error.
 * 
 * @author mey
 *
 * @param
 *            <Q>
 *            the type of {@link Quantity}
 */
public class BaseStorage<Q extends Quantity> implements Storage<Q>, Valuable {
    private static final long serialVersionUID = 1L;

    /** Value of the storage. */
    private double value;
    /** Unit the value is stored in. */
    private Unit<Q> unit;

    public BaseStorage(Amount<Q> amount) {
        setAmount(amount);
    }

    public BaseStorage(double value, Unit<Q> unit) {
        super();
        this.value = value;
        this.unit = unit;
    }

    @Override
    public Amount<Q> getAmount() {
        return Amount.valueOf(value, unit);
    }

    protected void setAmount(Amount<Q> amount) {
        this.value = amount.getEstimatedValue();
        this.unit = amount.getUnit();
    }

    protected double getValue() {
        return value;
    }

    protected void setValue(double value) {
        this.value = value;
    }

    protected Unit<Q> getUnit() {
        return unit;
    }

    protected void setUnit(Unit<Q> unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[amount=" + getAmount() + "]";
    }

    @Override
    public double doubleValue() {
        return value;
    }
}