package de.zmt.ecs.component.agent;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.ValuableAmountAdapter;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * Grants a simulation object the ability to grow.
 * 
 * @author mey
 *
 */
public class Growing implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Biomass of fish (wet weight). */
    private Amount<Mass> biomass;

    /** Expected biomass of fish derived from its virtual age. */
    private Amount<Mass> expectedBiomass;

    /** Length of the entity. */
    private Amount<Length> length;

    /** The highest biomass the agent ever had. */
    private Amount<Mass> topBiomass;


    public Growing(Amount<Mass> initialBiomass, Amount<Length> initialLength) {
	this.biomass = initialBiomass;
	this.expectedBiomass = initialBiomass;
	this.topBiomass = initialBiomass;
	this.length = initialLength;
    }

    public Amount<Mass> getBiomass() {
	return biomass;
    }

    public void setBiomass(Amount<Mass> biomass) {
	this.biomass = biomass;
	if (biomass.isGreaterThan(topBiomass)) {
	    this.topBiomass = biomass;
	}
    }

    public Amount<Mass> getExpectedBiomass() {
	return expectedBiomass;
    }

    public void setExpectedBiomass(Amount<Mass> expectedBiomass) {
	this.expectedBiomass = expectedBiomass;
    }

    public Amount<Length> getLength() {
	return length;
    }

    public void setLength(Amount<Length> length) {
	this.length = length;
    }

    /** @return <code>true</code> if biomass was never higher */
    public boolean hasTopBiomass() {
	return biomass.equals(topBiomass);
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [biomass=" + biomass + ", length=" + length + "]";
    }

    public class MyPropertiesProxy {
	public Valuable getBiomass() {
	    return ValuableAmountAdapter.wrap(biomass);
	}

	public Valuable getExpectedBiomass() {
	    return ValuableAmountAdapter.wrap(expectedBiomass);
	}

	public Valuable getLength() {
	    return ValuableAmountAdapter.wrap(length);
	}

	public Valuable getTopBiomass() {
	    return ValuableAmountAdapter.wrap(topBiomass);
	}

	@Override
	public String toString() {
	    return Growing.this.getClass().getSimpleName();
	}
    }
}
