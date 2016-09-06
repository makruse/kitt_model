package de.zmt.ecs.component;

import de.zmt.ecs.Component;

/**
 * {@link Component} specifying the time a larva transforms into a fish.
 * 
 * @author mey
 *
 */
public class Metamorphic implements Component {
    private static final long serialVersionUID = 1L;

    /** The time of metamorphosis. */
    private final double metamorphosisTime;

    public Metamorphic(double metamorphosisTime) {
        super();
        this.metamorphosisTime = metamorphosisTime;
    }

    /**
     * Returns the time of metamorphosis.
     * 
     * @return the time of metamorphosis
     */
    public double getMetamorphosisTime() {
        return metamorphosisTime;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[metamorphosisTime=" + metamorphosisTime + "]";
    }
}
