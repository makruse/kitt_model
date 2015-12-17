package de.zmt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.*;

/**
 * Energy per mass, for example the energy density of food. Default unit is
 * <tt>j /
 * kg</tt>.
 * 
 * @author mey
 * 
 */
public interface SpecificEnergy extends Quantity {
    /**
     * Default unit used in verification.
     * 
     * @see Unit#asType(Class)
     */
    public final static Unit<SpecificEnergy> UNIT = new ProductUnit<>(JOULE.divide(KILOGRAM));
}