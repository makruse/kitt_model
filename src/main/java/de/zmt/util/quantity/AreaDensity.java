package de.zmt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.Unit;

/**
 * Gram per square meter, for example algal standing crop in a habitat. Default
 * unit is <tt>kg / m<sup>2</sup></tt>.
 * 
 * @author mey
 * 
 */
public interface AreaDensity extends Quantity {
    /**
     * Default unit used in verification.
     * 
     * @see Unit#asType(Class)
     */
    public final static Unit<AreaDensity> UNIT = new ProductUnit<>(KILOGRAM.divide(SQUARE_METRE));
}
