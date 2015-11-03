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
    private static final double REPRO_UPPER_LIMIT_BIOMASS_FRACTION = 0.25;
    /**
     * Fraction of biomass for deriving lower limit, 10% of upper limit.
     * 
     * @see #getLowerLimit()
     */
    private static final double REPRO_LOWER_LIMIT_BIOMASS_FRACTION = REPRO_UPPER_LIMIT_BIOMASS_FRACTION * 0.1;

    private final Growing growing;

    public ReproductionStorage(Growing growing) {
	super();
	this.growing = growing;
    }

    /**
     * Lower limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit. The lower limit is 10% of the upper limit, i.e. when
     * the storage is cleared after reproduction, 10% of the energy will remain.
     * 
     * <pre>
     * lower_limit_kj = biomass &sdot; {@value #REPRO_LOWER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return Type.REPRODUCTION.toEnergy(growing.getBiomass().times(REPRO_LOWER_LIMIT_BIOMASS_FRACTION));
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