package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;

public class FatStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_FAT = 0.87;
    /**
     * Fraction of biomass for deriving lower limit.
     * 
     * @see #getLowerLimit()
     */
    private static final double FAT_LOWER_LIMIT_BIOMASS_FRACTION = 0.05;
    /**
     * Fraction of biomass for deriving upper limit.
     * 
     * @see #getUpperLimit()
     */
    private static final double FAT_UPPER_LIMIT_BIOMASS_FRACTION = 0.1;

    private final Growing growing;

    public FatStorage(Amount<Energy> amount, Growing growing) {
	super(amount);
	this.growing = growing;
    }

    /**
     * Lower limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit.
     * 
     * <pre>
     * lower_limit_kj = biomass &sdot; {@value #FAT_LOWER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (fat)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
	return Type.FAT.toEnergy(growing.getBiomass().times(FAT_LOWER_LIMIT_BIOMASS_FRACTION));
    }

    /**
     * Upper limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit.
     * 
     * <pre>
     * upper_limit_kj = biomass &sdot; {@value #FAT_UPPER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (fat)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
	return Type.FAT.toEnergy(growing.getBiomass().times(FAT_UPPER_LIMIT_BIOMASS_FRACTION));
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