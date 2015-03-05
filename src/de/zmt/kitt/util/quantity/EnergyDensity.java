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
public interface EnergyDensity extends Quantity {
    public final static Unit<EnergyDensity> UNIT = new ProductUnit<EnergyDensity>(
    	KILO(JOULE).divide(GRAM));
}