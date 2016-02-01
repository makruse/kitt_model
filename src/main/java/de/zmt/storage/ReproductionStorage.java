package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;
import ec.util.MersenneTwisterFast;

/**
 * A storage for reproductive energy. Limits are computed from fractions applied
 * on the agent's biomass. A random component is added each time the limits are
 * refreshed. For the lower limit this is done each time the storage is cleared,
 * while the upper limit is refreshed when energy is added to the storage.
 * <p>
 * 
 * <pre>
 * lower_limit_kj = biomass [g] &sdot; ({@value #LOWER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #LOWER_LIMIT_DEVIATION}) &sdot; kJ / g (repro)
 * upper_limit_kj = biomass [g] &sdot; ({@value #UPPER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #UPPER_LIMIT_DEVIATION})&sdot; kJ / g (repro)
 * </pre>
 * 
 * @author mey
 *
 */
public class ReproductionStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the reproduction storage */
    private static final double LOSS_FACTOR = 0.87;
    /** Fraction of biomass for deriving lower limit. */
    private static final double LOWER_LIMIT_BIOMASS_FRACTION = 0.1;
    /** Standard deviation for lower limit. */
    private static final double LOWER_LIMIT_DEVIATION = 0.01;
    /** Fraction of biomass for deriving upper limit. */
    private static final double UPPER_LIMIT_BIOMASS_FRACTION = 0.25;
    /** Standard deviation for upper limit. */
    private static final double UPPER_LIMIT_DEVIATION = 0.025;

    private final Growing growing;
    private final MersenneTwisterFast random;

    private Amount<Energy> lowerLimit;
    private Amount<Energy> upperLimit;

    public ReproductionStorage(Growing growing, MersenneTwisterFast random) {
	super();
	this.growing = growing;
	this.random = random;
    }

    @Override
    public ChangeResult<Energy> add(Amount<Energy> amountToAdd) {
	double estimatedValue = amountToAdd.getEstimatedValue();

	if (estimatedValue < 0) {
	    throw new IllegalArgumentException(
		    "Cannot add " + amountToAdd + " to storage. Negative amounts are not allowed.");
	}
	if (estimatedValue > 0) {
	    upperLimit = computeLimit(UPPER_LIMIT_BIOMASS_FRACTION, UPPER_LIMIT_DEVIATION);
	}
	return super.add(amountToAdd);
    }

    /** Refreshes lower limit before clearing the storage. */
    @Override
    public Amount<Energy> clear() {
	lowerLimit = computeLimit(LOWER_LIMIT_BIOMASS_FRACTION, LOWER_LIMIT_DEVIATION);
	return super.clear();
    }

    private Amount<Energy> computeLimit(double fraction, double margin) {
	double deviation = random.nextGaussian() * margin;
	return Type.REPRODUCTION.toEnergy(growing.getBiomass().times(fraction + deviation));
    }

    /**
     * Lower limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit after adding a random component.
     * 
     * <pre>
     * lower_limit_kj = biomass &sdot; ({@value #LOWER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #LOWER_LIMIT_DEVIATION}) &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return lowerLimit;
    }

    /**
     * Upper limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit after adding a random component.
     * 
     * <pre>
     * upper_limit_kj = biomass [g] &sdot; ({@value #UPPER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #UPPER_LIMIT_DEVIATION})&sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return upperLimit;
    }

    @Override
    protected double getFactorIn() {
	return LOSS_FACTOR;
    }

    @Override
    public Type getType() {
	return Type.REPRODUCTION;
    }

}