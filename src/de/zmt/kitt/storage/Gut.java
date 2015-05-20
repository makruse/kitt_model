package de.zmt.kitt.storage;

import static javax.measure.unit.NonSI.HOUR;

import java.util.concurrent.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Compartments.CompartmentPipeline;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.kitt.util.*;
import de.zmt.storage.ConfigurableStorage;
import de.zmt.storage.pipeline.AbstractLimitedStoragePipeline;

public class Gut extends AbstractLimitedStoragePipeline<Energy> implements
	CompartmentPipeline {
    private static final long serialVersionUID = 1L;

    private static final double GUT_MAX_CAPACITY_SMR_VALUE = 22;
    /**
     * Gut maximum storage capacity on SMR:<br>
     * {@value #GUT_MAX_CAPACITY_SMR_VALUE}h
     */
    private static final Amount<Duration> GUT_MAX_CAPACITY_SMR = Amount
	    .valueOf(GUT_MAX_CAPACITY_SMR_VALUE, HOUR);

    private final SpeciesDefinition definition;
    private final Aging aging;

    public Gut(final SpeciesDefinition definition,
	    final Metabolizing metabolizing,
	    Aging aging) {
	super(new ConfigurableStorage<Energy>(UnitConstants.CELLULAR_ENERGY) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected Amount<Energy> getUpperLimit() {
		// maximum capacity of gut
		return metabolizing.getStandardMetabolicRate().times(
			GUT_MAX_CAPACITY_SMR).to(
			amount.getUnit());
	    }

	    @Override
	    protected double getFactorIn() {
		// energy is lost while digesting
		return definition.getLossFactorDigestion();
	    }
	});

	this.definition = definition;
	this.aging = aging;
    }

    @Override
    protected DelayedStorage<Energy> createDelayedStorage(
	    Amount<Energy> storedAmount) {
	return new Digesta(storedAmount);
    }

    @Override
    public Type getType() {
	return Type.GUT;
    }

    /**
     * Food undergoing digestion.
     * 
     * @author cmeyer
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
	    this.digestionFinishedAge = aging.getAge().plus(definition
		    .getGutTransitDuration());
	}

	@Override
	public long getDelay(TimeUnit unit) {
	    Amount<Duration> delay = digestionFinishedAge.minus(aging.getAge());
	    return AmountUtil.toTimeUnit(delay, unit);
	}

	@Override
	public int compareTo(Delayed o) {
	    // shortcut for better performance
	    /*
	     * Apart from that, deserialization will fail without it because
	     * sorting of the priority queue calls getDelay (from compareTo) for
	     * its Digestables, which would need the age field in the
	     * encapsulating object of Metabolism. Metabolism has not finished
	     * deserialization at this point and the age field is not available,
	     * leading to a NullPointerException.
	     */
	    if (o instanceof Gut.Digesta) {
		return digestionFinishedAge
			.compareTo(((Gut.Digesta) o).digestionFinishedAge);
	    }
	    return super.compareTo(o);
	}

    }
}