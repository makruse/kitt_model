package de.zmt.storage;

import static javax.measure.unit.NonSI.HOUR;

import java.util.concurrent.*;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.*;
import de.zmt.util.*;
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

    public Gut(final SpeciesDefinition definition, final Metabolizing metabolizing, Aging aging) {
	super(new SumStorage(UnitConstants.CELLULAR_ENERGY, metabolizing, definition));

	this.definition = definition;
	this.aging = aging;
    }

    @Override
    public Amount<Mass> toMass() {
	return getType().toMass(getAmount());
    }

    @Override
    protected DelayedStorage<Energy> createDelayedStorage(Amount<Energy> storedAmount) {
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
    private class Digesta extends DelayedStorage<Energy> {
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
	private static final double GUT_UPPER_LIMIT_SMR_HOUR_VALUE = 22;
	/**
	 * Gut maximum storage capacity on SMR.
	 * 
	 * @see #getUpperLimit()
	 */
	private static final Amount<Duration> GUT_UPPER_LIMIT_SMR = Amount.valueOf(GUT_UPPER_LIMIT_SMR_HOUR_VALUE,
		HOUR);

	private final Metabolizing metabolizing;
	private final SpeciesDefinition definition;
	private static final long serialVersionUID = 1L;

	private SumStorage(Unit<Energy> unit, Metabolizing metabolizing, SpeciesDefinition definition) {
	    super(unit);
	    this.metabolizing = metabolizing;
	    this.definition = definition;
	}

	/**
	 * Lower limit as duration that SMR can be maintained:<br>
	 * {@value #GUT_UPPER_LIMIT_SMR_HOUR_VALUE}h &sdot; SMR
	 */
	@Override
	protected Amount<Energy> getUpperLimit() {
	    return GUT_UPPER_LIMIT_SMR.times(metabolizing.getStandardMetabolicRate()).to(amount.getUnit());
	}

	@Override
	protected double getFactorIn() {
	    // energy is lost while digesting
	    return definition.getLossFactorDigestion();
	}
    }
}