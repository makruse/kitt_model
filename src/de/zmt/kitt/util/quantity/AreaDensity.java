package de.zmt.kitt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.*;

/**
 * Gram per square meter, for example algal standing crop in a habitat.
 * 
 * @author cmeyer
 * 
 */
public interface AreaDensity extends Quantity {
    public final static Unit<AreaDensity> UNIT = new ProductUnit<AreaDensity>(
	    GRAM.divide(SQUARE_METRE));
}
