package de.zmt.kitt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.*;

/**
 * Energy per mass, for example the energy density of food.
 * 
 * @author cmeyer
 * 
 */
public interface SpecificEnergy extends Quantity {
    public final static Unit<SpecificEnergy> UNIT = new ProductUnit<SpecificEnergy>(
	    JOULE.divide(KILOGRAM));
}