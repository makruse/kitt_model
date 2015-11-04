package de.zmt.storage;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.storage.ConfigurableStorage;

@SuppressWarnings("serial")
public class LimitedTestStorage extends ConfigurableStorage<Dimensionless> {
    public static final Amount<Dimensionless> LOWER_LIMIT = Amount.valueOf(10.5, Unit.ONE);
    public static final Amount<Dimensionless> UPPER_LIMIT = Amount.valueOf(50.7, Unit.ONE);
    public static final double FACTOR_IN = 1.5;
    public static final double FACTOR_OUT = 0.8;

    public LimitedTestStorage() {
	super(Unit.ONE, true);
    }

    @Override
    protected Amount<Dimensionless> getLowerLimit() {
	return LOWER_LIMIT;
    }

    @Override
    protected Amount<Dimensionless> getUpperLimit() {
	return UPPER_LIMIT;
    }

    @Override
    protected double getFactorIn() {
	return FACTOR_IN;
    }

    @Override
    protected double getFactorOut() {
	return FACTOR_OUT;
    }

}
