package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;
import de.zmt.util.UnitConstants;

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
     * Lower limit as fraction of energy stored in expected biomass:
     * 
     * <pre>
     * lower_limit_kj = expected_biomass &sdot; {@value #PROTEIN_LOWER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     * 
     * @see de.zmt.storage.Compartment.Type#getKjPerGram()
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return growing.getExpectedBiomass().times(PROTEIN_LOWER_LIMIT_EXP_BIOMASS_FRACTION)
		.times(Type.PROTEIN.getKjPerGram()).to(UnitConstants.CELLULAR_ENERGY);
    }

    /**
     * Upper limit as fraction of energy stored in expected biomass:
     * 
     * <pre>
     * upper_limit_kj = expected_biomass &sdot; {@value #PROTEIN_UPPER_LIMIT_EXP_BIOMASS_FRACTION} &sdot; kJ / g (repro)
     * </pre>
     * 
     * @see de.zmt.storage.Compartment.Type#getKjPerGram()
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return growing.getExpectedBiomass().times(PROTEIN_UPPER_LIMIT_EXP_BIOMASS_FRACTION)
		.times(Type.PROTEIN.getKjPerGram()).to(UnitConstants.CELLULAR_ENERGY);
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