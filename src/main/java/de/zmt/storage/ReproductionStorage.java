package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;

public class ReproductionStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the reproduction storage */
    private static final double LOSS_FACTOR_REPRO = 0.87;
    /**
     * Fraction of biomass for deriving upper limit.
     * 
     * @see #getUpperLimit()
     */
    private static final double REPRO_UPPER_LIMIT_BIOMASS_FRACTION = 0.3;

    private final Growing growing;

    public ReproductionStorage(Growing growing) {
	super();
	this.growing = growing;
    }

    /**
     * Upper limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit.
     * 
     * <pre>
     * upper_limit_kj = biomass &sdot; {@value #REPRO_UPPER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return Type.REPRODUCTION.toEnergy(growing.getBiomass().times(REPRO_UPPER_LIMIT_BIOMASS_FRACTION));
    }

    @Override
    protected double getFactorIn() {
	return LOSS_FACTOR_REPRO;
    }

    @Override
    public Type getType() {
	return Type.REPRODUCTION;
    }

}