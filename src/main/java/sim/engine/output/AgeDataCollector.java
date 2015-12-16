package sim.engine.output;

import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.util.UnitConstants;
import sim.engine.output.AgeDataCollector.AgeData;
import sim.params.def.SpeciesDefinition;

/**
 * Collects age data for every species.
 * 
 * @see AgeData
 * @see SpeciesDefinition
 * @author mey
 * 
 */
public class AgeDataCollector extends DefinitionSeparatedCollector<SpeciesDefinition, AgeData> {
    private static final long serialVersionUID = 1L;

    public AgeDataCollector(Collection<? extends SpeciesDefinition> agentClassDefs) {
	super(agentClassDefs);
    }

    @Override
    public void beforeCollect(BeforeMessage message) {
	clear();
    }

    @Override
    public void collect(CollectMessage message) {
	Entity agent = (Entity) message.getSimObject();
	SpeciesDefinition definition = agent.get(SpeciesDefinition.class);
	Aging aging = agent.get(Aging.class);

	if (definition == null || aging == null) {
	    return;
	}

	AgeData data = getDataPerDefinition().get(definition);
	data.increase(aging.getAge());
    }

    @Override
    protected AgeData createCollectable(SpeciesDefinition definition) {
	return new AgeData(definition.getInitialAge(), definition.getMaxAge());
    }

    /**
     * Agents are sorted into {@value #PARTITIONS_COUNT} partitions ranging from
     * minimum to maximum age.
     * 
     * @author mey
     *
     */
    public static class AgeData extends AbstractCollectable<Integer> {
	private static final long serialVersionUID = 1L;

	private static final int PARTITIONS_COUNT = 5;
	/**
	 * Formats min / max values of interval with 2 digits after fractions.
	 */
	private static final String HEADER_FORMAT_STRING = "age_" + UnitConstants.AGE_GUI + "_%.2f-%.2f";

	/** Minimum age that can be collected */
	private final Amount<Duration> minAge;
	/** Intervals stored as maximum amounts for each partition */
	private final List<Amount<Duration>> intervals = new ArrayList<>(PARTITIONS_COUNT);
	private final List<String> headers = new ArrayList<>(PARTITIONS_COUNT);

	/**
	 * @param minAge
	 *            lowest value that can be collected for this class
	 * @param maxAge
	 *            highest value that can be collected for this class
	 */
	private AgeData(Amount<Duration> minAge, Amount<Duration> maxAge) {
	    super(new ArrayList<Integer>(PARTITIONS_COUNT));

	    this.minAge = minAge;
	    Amount<Duration> range = maxAge.minus(minAge);
	    Amount<Duration> interval = range.divide(PARTITIONS_COUNT);

	    Amount<Duration> intervalMin = minAge;
	    for (int i = 0; i < PARTITIONS_COUNT; i++) {
		Amount<Duration> intervalMax = minAge.plus(interval.times(i + 1));
		String intervalString = String.format(HEADER_FORMAT_STRING,
			intervalMin.doubleValue(UnitConstants.AGE_GUI), intervalMax.doubleValue(UnitConstants.AGE_GUI));

		intervals.add(intervalMax);
		headers.add(intervalString);
		data.add(0);

		// current interval's maximum is next one's minimum
		intervalMin = intervalMax;
	    }
	}

	/**
	 * Increase count for partition associated with {@code age}.
	 * 
	 * @param age
	 */
	public void increase(Amount<Duration> age) {
	    int intervalIndex = findIntervalIndex(age);
	    int count = data.get(intervalIndex);
	    data.set(intervalIndex, count + 1);
	}

	/**
	 * 
	 * @param age
	 * @return index of partition that {@code age} fits into.
	 */
	private int findIntervalIndex(Amount<Duration> age) {
	    if (age.isLessThan(minAge)) {
		throw new IllegalArgumentException("Given age is lower than minimum.");
	    }

	    ListIterator<Amount<Duration>> iterator = intervals.listIterator();
	    Amount<Duration> intervalMax;
	    do {
		intervalMax = iterator.next();

		if (!iterator.hasNext()) {
		    throw new IllegalArgumentException("Given age exceeds maximum.");
		}
	    } while (age.isGreaterThan(intervalMax));

	    return iterator.previousIndex();
	}

	@Override
	public List<String> obtainHeaders() {
	    return headers;
	}

	@Override
	protected Integer obtainInitialValue() {
	    return 0;
	}

    }
}