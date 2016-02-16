package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;
import ec.util.MersenneTwisterFast;

/**
 * A storage for reproductive energy. Limits are computed from fractions applied
 * on the agent's biomass. A random component is added each time the limits are
 * refreshed. Refresh needs to be done manually by calling
 * {@link #refreshLowerLimit()} and {@link #refreshUpperLimit()}.
 * <p>
 * 
 * <pre>
 * lower_limit_kj = biomass [g] &sdot; ({@value #LOWER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #LOWER_LIMIT_VARIANCE}) &sdot; kJ / g (repro)
 * upper_limit_kj = biomass [g] &sdot; ({@value #UPPER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #UPPER_LIMIT_VARIANCE})&sdot; kJ / g (repro)
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
    /** Variance for lower limit. */
    private static final double LOWER_LIMIT_VARIANCE = 0.01;
    /** Fraction of biomass for deriving upper limit. */
    private static final double UPPER_LIMIT_BIOMASS_FRACTION = 0.25;
    /** Variance for upper limit. */
    private static final double UPPER_LIMIT_VARIANCE = 0.025;

    private final Growing growing;
    private final MersenneTwisterFast random;

    private Amount<Energy> lowerLimit;
    private Amount<Energy> upperLimit;

    /**
     * Constructs an empty {@link ReproductionStorage}.
     * 
     * @param growing
     *            the {@link Growing} component of the entity this storage
     *            belongs to
     * @param random
     *            the random number generator of the simulation
     */
    public ReproductionStorage(Growing growing, MersenneTwisterFast random) {
	super();
	this.growing = growing;
	this.random = random;
    }

    /**
     * Constructs a filled {@link ReproductionStorage}.
     * 
     * @param growing
     *            the {@link Growing} component of the entity this storage
     *            belongs to
     * @param random
     *            the random number generator of the simulation
     * @param fillLevel
     *            value between 0-1 defining the initial fill level between
     *            lower and upper limit
     */
    public ReproductionStorage(Growing growing, MersenneTwisterFast random, double fillLevel) {
	this(growing, random);

	refreshLowerLimit();
	refreshUpperLimit();

	fill(fillLevel);
    }

    /**
     * Refreshes lower limit. A fraction of biomass, converted to energy acts as
     * the limit after adding a random component.
     * 
     * <pre>
     * lower_limit_kj = biomass &sdot; ({@value #LOWER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #LOWER_LIMIT_VARIANCE}) &sdot; kJ / g (repro)
     * </pre>
     * 
     */
    public void refreshLowerLimit() {
	lowerLimit = computeLimit(LOWER_LIMIT_BIOMASS_FRACTION, LOWER_LIMIT_VARIANCE);
    }

    /**
     * Refreshes upper limit. A fraction of biomass, converted to energy acts as
     * the limit after adding a random component.
     * 
     * <pre>
     * upper_limit_kj = biomass [g] &sdot; ({@value #UPPER_LIMIT_BIOMASS_FRACTION} &plusmn; {@value #UPPER_LIMIT_VARIANCE})&sdot; kJ / g (repro)
     * </pre>
     */
    public void refreshUpperLimit() {
	upperLimit = computeLimit(UPPER_LIMIT_BIOMASS_FRACTION, UPPER_LIMIT_VARIANCE);
    }

    private Amount<Energy> computeLimit(double fraction, double margin) {
	double variance = (random.nextDouble() * 2 - 1) * margin;
	return Type.REPRODUCTION.toEnergy(growing.getBiomass().times(fraction + variance));
    }

    @Override
    public ChangeResult<Energy> add(Amount<Energy> amountToAdd) {
	if (amountToAdd.getEstimatedValue() < 0) {
	    throw new IllegalArgumentException(
		    "Cannot add " + amountToAdd + " to storage. Negative amounts are not allowed.");
	}
	return super.add(amountToAdd);
    }

    /** @see #refreshLowerLimit() */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return lowerLimit;
    }

    /** @see #refreshUpperLimit() */
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