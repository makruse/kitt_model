package de.zmt.storage;

import static javax.measure.unit.SI.*;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.params.def.SpeciesDefinition;

/**
 * A limited {@link StoragePipeline} used to model a gut. Digesta are created
 * when food is added. They can be drained after a certain amount of time and
 * consumed or stored in other compartments.
 * 
 * @author mey
 *
 */
public class Gut extends AbstractLimitedStoragePipeline<Energy> implements Compartment {
    private static final long serialVersionUID = 1L;

    private final SpeciesDefinition definition;
    private final Aging aging;

    public Gut(final SpeciesDefinition definition, final Growing growing, Aging aging) {
	super(new SumStorage(UnitConstants.CELLULAR_ENERGY, growing, definition));

	this.definition = definition;
	this.aging = aging;
    }

    @Override
    public Amount<Mass> toMass() {
	return getType().toMass(getAmount());
    }

    @Override
    protected AbstractLimitedStoragePipeline.DelayedStorage<Energy> createDelayedStorage(Amount<Energy> storedAmount) {
	return new Digesta(storedAmount);
    }

    @Override
    public Type getType() {
	return Type.GUT;
    }

    /**
     * Food undergoing digestion.
     * 
     * @author mey
     * 
     */
    private class Digesta extends AbstractLimitedStoragePipeline.DelayedStorage<Energy> {
	private static final long serialVersionUID = 1L;

	/** Age of fish when digestion of this digesta is finished. */
	private final Amount<Duration> digestionFinishedAge;

	/**
	 * Create new digesta with given amount of energy.
	 * 
	 * @param energy
	 *            in kJ
	 */
	public Digesta(Amount<Energy> energy) {
	    super(energy);
	    this.digestionFinishedAge = aging.getAge().plus(definition.getGutTransitDuration());
	}

	@Override
	public long getDelay(TimeUnit unit) {
	    Amount<Duration> delay = digestionFinishedAge.minus(aging.getAge());
	    return AmountUtil.toTimeUnit(delay, unit);
	}

	@Override
	public int compareTo(Delayed o) {
	    // shortcut for better performance
	    if (o instanceof Gut.Digesta) {
		return digestionFinishedAge.compareTo(((Gut.Digesta) o).digestionFinishedAge);
	    }
	    return super.compareTo(o);
	}

    }

    /**
     * Stores sum of all {@link Digesta}s currently in gut and specifies limits.
     * 
     * @author mey
     *
     */
    private static class SumStorage extends ConfigurableStorage<Energy> {
	private static final long serialVersionUID = 1L;

	private static final int UPPER_LIMIT_MG_PER_BIOMASS_VALUE = 17;
	/**
	 * Amount of food per biomass for deriving upper limit.
	 * 
	 * @see #getUpperLimit()
	 */
	private static final Amount<Dimensionless> UPPER_LIMIT_FOOD_PER_BIOMASS = Amount.valueOf(
		UPPER_LIMIT_MG_PER_BIOMASS_VALUE,
		MILLI(GRAM).divide(UnitConstants.BIOMASS).asType(Dimensionless.class));

	private final Growing growing;
	private final SpeciesDefinition definition;

	private SumStorage(Unit<Energy> unit, Growing growing, SpeciesDefinition definition) {
	    super(unit);
	    this.growing = growing;
	    this.definition = definition;
	}

	/**
	 * Upper limit depending on biomass:
	 * 
	 * <pre>
	 * upper_limit_kJ = {@value #UPPER_LIMIT_MG_PER_BIOMASS_VALUE} [mg/g, food dry weight per biomass]
	 * 	&sdot; {@code energyContentFood} [kJ/g] &sdot; biomass [g]
	 * </pre>
	 */
	@Override
	protected Amount<Energy> getUpperLimit() {
	    return UPPER_LIMIT_FOOD_PER_BIOMASS.times(definition.getEnergyContentFood()).times(growing.getBiomass())
		    .to(UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected double getFactorOut() {
	    // energy is lost while digesting
	    return definition.getGutFactorOut();
	}
    }
}