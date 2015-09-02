package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;

public class ProteinStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the protein storage. */
    private static final double LOSS_FACTOR_PROTEIN = 0.90;
    /**
     * Fraction of expected biomass for deriving lower limit.
     * 
     * {@link #getLowerLimit()}
     */
    private static final double PROTEIN_LOWER_LIMIT_EXP_BIOMASS_FRACTION = 0.6;
    /**
     * Fraction of expected biomass for deriving upper limit.
     * 
     * {@link #getUpperLimit()}
     */
    private static final double PROTEIN_UPPER_LIMIT_EXP_BIOMASS_FRACTION = 1.2;

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
     * lower_limit_kj = expected_biomass &sdot; {@value #PROTEIN_LOWER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return Type.PROTEIN.toEnergy(growing.getExpectedBiomass().times(PROTEIN_LOWER_LIMIT_EXP_BIOMASS_FRACTION));
    }

    /**
     * Upper limit as fraction of expected biomass. That fraction, converted to
     * energy acts as the limit.
     * 
     * <pre>
     * upper_limit_kj = expected_biomass &sdot; {@value #PROTEIN_UPPER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return Type.PROTEIN.toEnergy(growing.getExpectedBiomass().times(PROTEIN_UPPER_LIMIT_EXP_BIOMASS_FRACTION));
    }

    @Override
    protected double getFactorIn() {
	return LOSS_FACTOR_PROTEIN;
    }

    @Override
    protected double getFactorOut() {
	return 1 / getFactorIn();
    }

    @Override
    public Type getType() {
	return Type.PROTEIN;
    }

}