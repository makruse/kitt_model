package de.zmt.ecs.component.agent;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import de.zmt.params.SpeciesDefinition;
import de.zmt.util.UnitConstants;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import sim.util.AmountValuable;
import sim.util.Proxiable;
import sim.util.Valuable;

import org.apache.commons.math3.distribution.NormalDistribution;

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

    /** The Biomass in energy(kJ) */
    private Amount<Energy> energy;

    public Growing(Amount<Mass> initialBiomass, Amount<Length> initialLength, SpeciesDefinition def) {
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

    public void setEnergy(Amount<Energy> energy){
        this.energy = energy;
    }

    public Amount<Energy> getEnergy() {
        return energy;
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

    public boolean isLower120ExpectedBiomass(){
        return biomass.isLessThan(expectedBiomass.times(1.2));
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
            return AmountValuable.wrap(biomass);
        }

        public Valuable getExpectedBiomass() {
            return AmountValuable.wrap(expectedBiomass);
        }

        public Valuable getLength() {
            return AmountValuable.wrap(length);
        }

        public Valuable getTopBiomass() {
            return AmountValuable.wrap(topBiomass);
        }

        @Override
        public String toString() {
            return Growing.this.getClass().getSimpleName();
        }
    }
}
