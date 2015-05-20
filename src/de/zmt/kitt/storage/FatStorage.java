package de.zmt.kitt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.agent.Compartments.AbstractCompartmentStorage;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.SpecificEnergy;

public class FatStorage extends AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_FAT = 0.87;
    private static final double FAT_MIN_CAPACITY_BIOMASS_VALUE = 0.05;
    private static final double FAT_MAX_CAPACITY_BIOMASS_VALUE = 0.1;
    /**
     * Fat minimum storage capacity on biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(fat) *
     * {@value #FAT_MIN_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MIN_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MIN_CAPACITY_BIOMASS_VALUE);
    /**
     * Fat maximum storage capacity on biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(fat) *
     * {@value #FAT_MAX_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MAX_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MAX_CAPACITY_BIOMASS_VALUE);

    private final MassComponent massComp;

    public FatStorage(Amount<Energy> amount, MassComponent massComponent) {
	super(amount);
	this.massComp = massComponent;
    }

    @Override
    protected Amount<Energy> getLowerLimit() {
	return massComp.getBiomass().times(FAT_MIN_CAPACITY_BIOMASS).to(
		UnitConstants.CELLULAR_ENERGY);
    }

    @Override
    protected Amount<Energy> getUpperLimit() {
	return massComp.getBiomass().times(FAT_MAX_CAPACITY_BIOMASS).to(
		UnitConstants.CELLULAR_ENERGY);
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