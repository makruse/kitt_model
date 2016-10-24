package de.zmt.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.output.collectable.AbstractCollectable;
import de.zmt.output.collectable.CategoryCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.strategy.CollectStrategy;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.UnitConstants;

/**
 * Agents are sorted into partitions ranging from minimum to maximum age.
 * 
 * @author mey
 *
 */
class AgeData extends AbstractCollectable<Integer> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link StrategyCollector} for collecting age data.
     * 
     * @param definitions
     *            the set of species definitions
     * @return the {@link StrategyCollector} for collecting age data
     */
    public static StrategyCollector<?> createCollector(Collection<? extends SpeciesDefinition> definitions) {
        return StrategyCollector.create(new MyCategoryCollectable(definitions), new MyCollectStrategy());
    }

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
    private final List<Integer> values = new ArrayList<>(PARTITIONS_COUNT);

    /**
     * @param minAge
     *            lowest value that can be collected for this class
     * @param maxAge
     *            highest value that can be collected for this class
     */
    private AgeData(Amount<Duration> minAge, Amount<Duration> maxAge) {
        this.minAge = minAge;
        Amount<Duration> range = maxAge.minus(minAge);
        Amount<Duration> interval = range.divide(PARTITIONS_COUNT);

        Amount<Duration> intervalMin = minAge;
        for (int i = 0; i < PARTITIONS_COUNT; i++) {
            Amount<Duration> intervalMax = minAge.plus(interval.times(i + 1));
            String intervalString = String.format(HEADER_FORMAT_STRING, intervalMin.doubleValue(UnitConstants.AGE_GUI),
                    intervalMax.doubleValue(UnitConstants.AGE_GUI));

            intervals.add(intervalMax);
            headers.add(intervalString);
            values.add(obtainInitialValue());

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
        int count = values.get(intervalIndex);
        values.set(intervalIndex, count + 1);
    }

    /**
     * 
     * @param age
     * @return index of partition that {@code age} fits into.
     */
    private int findIntervalIndex(Amount<Duration> age) {
        if (age.isLessThan(minAge)) {
            throw new IllegalArgumentException(age + " is lower than minimum.");
        }

        ListIterator<Amount<Duration>> iterator = intervals.listIterator();
        Amount<Duration> intervalMax;
        do {
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(age + " exceeds maximum.");
            }
            intervalMax = iterator.next();

        } while (age.isGreaterThan(intervalMax));

        return iterator.previousIndex();
    }

    @Override
    protected Integer obtainInitialValue() {
        return 0;
    }

    @Override
    public List<String> obtainHeaders() {
        return headers;
    }

    @Override
    public List<Integer> obtainValues() {
        return values;
    }

    /**
     * {@link CategoryCollectable} using {@link SpeciesDefinition} as categories
     * each with an {@link AgeData} object.
     * 
     * @author mey
     *
     */
    private static class MyCategoryCollectable extends CategoryCollectable<SpeciesDefinition, AgeData, Integer> {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new {@link MyCategoryCollectable}.
         * 
         * @param definitions
         *            the {@link SpeciesDefinition} objects acting as categories
         */
        public MyCategoryCollectable(Collection<? extends SpeciesDefinition> definitions) {
            super(definitions.stream().collect(Collectors.toMap(definition -> definition,
                    definition -> new AgeData(definition.getPostSettlementAge(), definition.getOverallMaxAge()))));
        }
    }

    /**
     * {@link CollectStrategy} updating {@link AgeData} for every agent.
     * 
     * @author mey
     *
     */
    private static class MyCollectStrategy extends ClearingBeforeStrategy<MyCategoryCollectable> {
        private static final long serialVersionUID = 1L;

        @Override
        protected void collect(AgentCollectMessage message, MyCategoryCollectable categoryCollectable) {
            Entity agent = message.getSimObject();
            SpeciesDefinition definition = agent.get(SpeciesDefinition.class);
            Aging aging = agent.get(Aging.class);

            if (definition == null || aging == null) {
                return;
            }

            AgeData data = categoryCollectable.getCollectable(definition);
            data.increase(aging.getAge());
        }
    }

}