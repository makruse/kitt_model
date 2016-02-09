package de.zmt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.Unit;

/**
 * Amount of mass per unit length to specify mass-length relationships. Default
 * unit is {@code kg / m}.
 * 
 * @author mey
 *
 */
public interface LinearMassDensity extends Quantity {
    /**
     * Default unit used in verification.
     * 
     * @see Unit#asType(Class)
     */
    public final static Unit<LinearMassDensity> UNIT = new ProductUnit<>(KILOGRAM.divide(METER));

}
