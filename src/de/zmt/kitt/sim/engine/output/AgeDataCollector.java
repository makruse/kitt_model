package de.zmt.kitt.sim.engine.output;

import java.io.Serializable;
import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import sim.util.*;
import sim.util.Properties;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.agent.fish.Fish;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.sim.engine.params.def.ParameterDefinition;

/**
 * Counts agents into different partitions based on their age.
 * 
 * @author cmeyer
 * 
 */
public class AgeDataCollector extends
	EncapsulatedClearableMap<ParameterDefinition, AgeDataCollector.AgeData>
	implements Collector {
    private static final long serialVersionUID = 1L;

    private static final int PARTITIONS_COUNT = 5;

    public Clearable getData(ParameterDefinition agentClassDef) {
	return map.get(agentClassDef);
    }

    @Override
    public void collect(Agent agent, Object message) {
	if (!(agent instanceof Fish)) {
	    return;
	}

	Fish fish = (Fish) agent;
	SpeciesDefinition definition = fish.getDefinition();
	AgeData data = map.get(definition);

	if (data == null) {
	    data = new AgeData(definition.getMaxAge());
	    map.put(definition, data);
	}

	data.increase(fish.getMetabolism().getAge());
    }

    public static class AgeData implements Serializable, Propertied, Clearable {
	private static final long serialVersionUID = 1L;

	/** Maximum values of intervals for collecting. */
	private final List<Amount<Duration>> intervals = new ArrayList<Amount<Duration>>(
		PARTITIONS_COUNT);
	private final int[] counts = new int[PARTITIONS_COUNT];

	/**
	 * @param maxAge
	 *            highest value that can be collected for this class
	 */
	private AgeData(Amount<Duration> maxAge) {
	    Amount<Duration> interval = maxAge.divide(PARTITIONS_COUNT);

	    for (int i = 1; i < PARTITIONS_COUNT; i++) {
		intervals.add(interval.times(i));
	    }
	    // maximum of last interval is max age
	    intervals.add(maxAge);
	}

	/**
	 * Increase count for partition associated with {@code age}.
	 * 
	 * @param age
	 */
	public void increase(Amount<Duration> age) {
	    counts[obtainIntervalIndex(age)]++;
	}

	/**
	 * 
	 * @param age
	 * @return Index of partition that {@code age} fits into.
	 */
	private int obtainIntervalIndex(Amount<Duration> age) {
	    ListIterator<Amount<Duration>> iterator = intervals.listIterator();

	    while (iterator.hasNext()) {
		if (age.isLessThan(iterator.next())) {
		    break;
		}
	    }

	    if (iterator.previousIndex() == 0 && age.getEstimatedValue() < 0) {
		throw new IllegalArgumentException(
			"Given age can't be negative.");
	    } else if (iterator.nextIndex() == intervals.size()) {
		throw new IllegalArgumentException("Given age exceeds maximum.");
	    }
	    return iterator.previousIndex();
	}

	/** @return List of interval maximum values for partitioning data. */
	public List<Amount<Duration>> getIntervals() {
	    return Collections.unmodifiableList(intervals);
	}

	public int getCount(int intervalIndex) {
	    return counts[intervalIndex];
	}

	/** Sets all counts to zero. */
	@Override
	public void clear() {
	    for (int i = 0; i < counts.length; i++) {
		counts[i] = 0;
	    }
	}

	@Override
	public String toString() {
	    return Arrays.toString(counts);
	}

	@Override
	public Properties properties() {
	    return new MyProperties();
	}

	/**
	 * Groups count next to interval.
	 * 
	 * @author cmeyer
	 * 
	 */
	public class MyProperties extends Properties {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public boolean isVolatile() {
		return true;
	    }

	    @Override
	    public int numProperties() {
		return counts.length;
	    }

	    @Override
	    public Object getValue(int index) {
		return counts[index];
	    }

	    @Override
	    public boolean isReadWrite(int index) {
		return false;
	    }

	    @Override
	    public String getName(int index) {
		return intervals.get(index).toString();
	    }

	    @Override
	    public Class<?> getType(int index) {
		return int.class;
	    }

	    @Override
	    protected Object _setValue(int index, Object value) {
		throw new UnsupportedOperationException("read only");
	    }

	}
    }
}