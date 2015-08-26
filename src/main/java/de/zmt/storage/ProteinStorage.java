package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Compartments.AbstractCompartmentStorage;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.SpecificEnergy;

public class ProteinStorage extends AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the protein storage */
    private static final double LOSS_FACTOR_PROTEIN = 0.90;
    private static final double PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE = 0.6;
    private static final double PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE = 1.2;
    /**
     * Protein minimum storage capacity on expected biomass:<br>
     * {@link de.zmt.storage.Compartment.Type#getKjPerGram()}(protein)
     * {@value #PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE}
     * <p>
     * Exceeding this limit will result in starvation.
     */
    private static final Amount<SpecificEnergy> PROTEIN_MIN_CAPACITY_EXP_BIOMASS = Compartment.Type.PROTEIN
	    .getKjPerGram().times(PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE);
    /**
     * Protein maximum storage capacity on expected biomass:<br>
     * {@link de.zmt.storage.Compartment.Type#getKjPerGram()}(protein)
     * {@value #PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> PROTEIN_MAX_CAPACITY_EXP_BIOMASS = Compartment.Type.PROTEIN
	    .getKjPerGram().times(PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE);

    private final Growing expBiomassComp;
    private final Reproducing reproComp;

    public ProteinStorage(Amount<Energy> amount, Growing growing, Reproducing reproducing) {
	super(amount);
	this.expBiomassComp = growing;
	this.reproComp = reproducing;
    }

    @Override
    protected Amount<Energy> getLowerLimit() {
	// amount is factor of protein amount in total expected biomass
	return PROTEIN_MIN_CAPACITY_EXP_BIOMASS.times(expBiomassComp.getExpectedBiomass())
		.times(Type.PROTEIN.getGrowthFraction(reproComp.isReproductive())).to(UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected Amount<Energy> getUpperLimit() {
	return PROTEIN_MAX_CAPACITY_EXP_BIOMASS.times(expBiomassComp.getExpectedBiomass())
		.times(Type.PROTEIN.getGrowthFraction(reproComp.isReproductive())).to(UnitConstants.CELLULAR_ENERGY);
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