package de.zmt.storage;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Growing;

public class FatStorage extends Compartment.AbstractCompartmentStorage {
    private static final long serialVersionUID = 1L;

    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR = 0.87;
    private static final double FACTOR_OUT = LOSS_FACTOR;
    private static final double FACTOR_IN = 1 / LOSS_FACTOR;
    /**
     * Fraction of biomass for deriving lower limit.
     * 
     * @see #getLowerLimit()
     */
    private static final double LOWER_LIMIT_BIOMASS_FRACTION = 0.005;
    /**
     * Fraction of biomass for deriving upper limit.
     * 
     * @see #getUpperLimit()
     */
    //TODO check fraction values
    private static final double UPPER_LIMIT_BIOMASS_FRACTION = 0.015;

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
     * lower_limit_kj = biomass &sdot; {@value #LOWER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (fat)
     * </pre>
     */
    @Override
    protected Amount<Energy> getLowerLimit() {
        return Type.FAT.toEnergy(growing.getBiomass().times(LOWER_LIMIT_BIOMASS_FRACTION));
    }

    /**
     * Upper limit as fraction of biomass. That fraction, converted to energy
     * acts as the limit.
     * 
     * <pre>
     * upper_limit_kj = biomass &sdot; {@value #UPPER_LIMIT_BIOMASS_FRACTION} &sdot; kJ / g (fat)
     * </pre>
     */
    @Override
    protected Amount<Energy> getUpperLimit() {
        return Type.FAT.toEnergy(growing.getBiomass().times(UPPER_LIMIT_BIOMASS_FRACTION));
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
        return Type.FAT;
    }

}