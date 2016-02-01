package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;

public class ProteinStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the protein storage. */
    private static final double LOSS_FACTOR = 0.90;
    private static final double FACTOR_OUT = LOSS_FACTOR;
    private static final double FACTOR_IN = 1 / LOSS_FACTOR;
    /**
     * Fraction of expected biomass for deriving lower limit.
     * 
     * {@link #getLowerLimit()}
     */
    private static final double LOWER_LIMIT_EXP_BIOMASS_FRACTION = 0.6;
    /**
     * Fraction of expected biomass for deriving upper limit.
     * 
     * {@link #getUpperLimit()}
     */
    private static final double UPPER_LIMIT_EXP_BIOMASS_FRACTION = 1.2;

    private final Growing growing;

    public ProteinStorage(Amount<Energy> amount, Growing growing) {
	super(amount);
	this.growing = growing;
    }

    /**
     * Lower limit as fraction of expected biomass. That fraction, converted to
     * energy acts as the limit.
     * 
     * <pre>
     * lower_limit_kj = expected_biomass &sdot; {@value #LOWER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return Type.PROTEIN.toEnergy(growing.getExpectedBiomass().times(LOWER_LIMIT_EXP_BIOMASS_FRACTION));
    }

    /**
     * Upper limit as fraction of expected biomass. That fraction, converted to
     * energy acts as the limit.
     * 
     * <pre>
     * upper_limit_kj = expected_biomass &sdot; {@value #UPPER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return Type.PROTEIN.toEnergy(growing.getExpectedBiomass().times(UPPER_LIMIT_EXP_BIOMASS_FRACTION));
    }

    @Override
    protected double getFactorIn() {
	return FACTOR_IN;
    }

    @Override
    protected double getFactorOut() {
	return FACTOR_OUT;
    }

    @Override
    public Type getType() {
	return Type.PROTEIN;
    }

}