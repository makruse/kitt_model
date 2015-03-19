package de.zmt.kitt.util.quantity;

import static javax.measure.unit.SI.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.*;

/**
 * Mass per energy, inverse quantity of {@link EnergyDensity}.
 * 
 * @see EnergyDensity.
 * @author cmeyer
 * 
 */
public interface MassDensity extends Quantity {
    public final static Unit<MassDensity> UNIT = new ProductUnit<MassDensity>(
	    (GRAM).divide(KILO(JOULE)));
}
