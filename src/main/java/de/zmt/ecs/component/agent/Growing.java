package de.zmt.ecs.component.agent;

import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import sim.util.Proxiable;

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

    /**
     * Age reflecting entity growth. It will fall below {@link Aging#age} if
     * entity could not consume enough food to grow ideally.
     */
    private Amount<Duration> virtualAge;

    /** Length of the entity. */
    private Amount<Length> length;

    /** Expected length if entity can grow ideally. */
    private Amount<Length> expectedLength;

    /**
     * Virtual age if entity could accumulate enough mass and grow towards the
     * current {@link #expectedLength}.
     * 
     * {@link #acceptExpected()}
     */
    private Amount<Duration> virtualAgeForExpectedLength;

    public Growing(Amount<Duration> initialAge, Amount<Mass> initialBiomass, Amount<Length> initialLength) {
	this.biomass = initialBiomass;
	this.expectedBiomass = initialBiomass;
	this.virtualAge = initialAge;
	this.length = initialLength;
	this.expectedLength = initialLength;
	this.virtualAgeForExpectedLength = initialAge;
    }

    /**
     * Called after growth succeeded. Expected values for length and virtual age
     * are taken over.
     */
    public void acceptExpected() {
	length = expectedLength;
	virtualAge = virtualAgeForExpectedLength;
    }

    public Amount<Duration> getVirtualAge() {
	return virtualAge;
    }

    public Amount<Mass> getBiomass() {
	return biomass;
    }

    public void setBiomass(Amount<Mass> biomass) {
	this.biomass = biomass;
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

    public Amount<Length> getExpectedLength() {
	return expectedLength;
    }

    public void setExpectedLength(Amount<Length> expectedLength) {
	this.expectedLength = expectedLength;
    }

    public void setVirtualAgeForExpectedLength(Amount<Duration> virtualAgeForExpectedLength) {
	this.virtualAgeForExpectedLength = virtualAgeForExpectedLength;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public String toString() {
	return "Growing [biomass=" + biomass + ", length=" + length + "]";
    }

    public class MyPropertiesProxy {
	public double getBiomass_g() {
	    return getBiomass().doubleValue(GRAM);
	}

	public double getExpectedBiomass_g() {
	    return expectedBiomass.doubleValue(GRAM);
	}

	public double getLength_cm() {
	    return length.doubleValue(CENTIMETER);
	}

	public double getVirtualAge_day() {
	    return virtualAge.doubleValue(DAY);
	}
    }
}
