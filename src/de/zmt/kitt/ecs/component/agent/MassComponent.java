package de.zmt.kitt.ecs.component.agent;

import static javax.measure.unit.SI.GRAM;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import sim.util.Proxiable;
import ecs.Component;

public class MassComponent implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Biomass of fish (wet weight). */
    private Amount<Mass> biomass;

    public MassComponent(Amount<Mass> initialBiomass) {
	this.biomass = initialBiomass;
    }

    public Amount<Mass> getBiomass() {
        return biomass;
    }

    public void setBiomass(Amount<Mass> biomass) {
        this.biomass = biomass;
    }

    @Override
    public String toString() {
	return "MassComponent [biomass=" + biomass + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public double getBiomass_g() {
	    return getBiomass().doubleValue(GRAM);
	}
    }

}
