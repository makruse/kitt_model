package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Compartments.AbstractCompartmentStorage;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.SpecificEnergy;

public class FatStorage extends AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_FAT = 0.87;
    private static final double FAT_MIN_CAPACITY_BIOMASS_VALUE = 0.05;
    private static final double FAT_MAX_CAPACITY_BIOMASS_VALUE = 0.1;
    /**
     * Fat minimum storage capacity on biomass:<br>
     * {@link de.zmt.storage.Compartment.Type#getKjPerGram()}(fat) *
     * {@value #FAT_MIN_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MIN_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MIN_CAPACITY_BIOMASS_VALUE);
    /**
     * Fat maximum storage capacity on biomass:<br>
     * {@link de.zmt.storage.Compartment.Type#getKjPerGram()}(fat) *
     * {@value #FAT_MAX_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MAX_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MAX_CAPACITY_BIOMASS_VALUE);

    private final Growing growing;

    public FatStorage(Amount<Energy> amount, Growing growing) {
	super(amount);
	this.growing = growing;
    }

    @Override
    protected Amount<Energy> getLowerLimit() {
	return growing.getBiomass().times(FAT_MIN_CAPACITY_BIOMASS)
		.to(UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected Amount<Energy> getUpperLimit() {
	return growing.getBiomass().times(FAT_MAX_CAPACITY_BIOMASS)
		.to(UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected double getFactorIn() {
	return LOSS_FACTOR_FAT;
    }

    @Override
    protected double getFactorOut() {
	return 1 / getFactorIn();
    }

    @Override
    public Type getType() {
	return Type.FAT;
    }

}