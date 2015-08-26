package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Compartments.AbstractCompartmentStorage;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.SpecificEnergy;

public class ReproductionStorage extends AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the reproduction storage */
    private static final double LOSS_FACTOR_REPRO = 0.87;
    private static final double REPRO_MAX_CAPACITY_BIOMASS_VALUE = 0.3;
    /**
     * Reproduction maximum storage capacity on biomass:<br>
     * {@link de.zmt.storage.Compartment.Type#getKjPerGram()}(reproduction)
     * * {@value #REPRO_MAX_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> REPRO_MAX_CAPACITY_BIOMASS = Compartment.Type.REPRODUCTION
	    .getKjPerGram().times(REPRO_MAX_CAPACITY_BIOMASS_VALUE);

    private final Growing growing;

    public ReproductionStorage(Growing growing) {
	super();
	this.growing = growing;
    }

    @Override
    protected Amount<Energy> getUpperLimit() {
	return growing.getBiomass().times(REPRO_MAX_CAPACITY_BIOMASS)
		.to(UnitConstants.CELLULAR_ENERGY);
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