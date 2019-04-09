package de.zmt.output;

import java.util.*;
import java.util.stream.Collectors;

import javax.measure.quantity.Duration;

import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
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

    private static final int PARTITIONS_COUNT = 11;
    /**
     * Formats min / max values of interval with 2 digits after fractions.
     */
    private static final String HEADER_FORMAT_STRING = "age_" + UnitConstants.AGE_GUI + "_%.2f-%.2f";

    /** Minimum age that can be collected */
    private final Amount<Duration> minAge;
    /** Intervals stored as maximum amounts for each partition */
    private final List<Amount<Duration>> intervals = new ArrayList<>(PARTITIONS_COUNT*3);
    /** times 3 because 3 phases*/
    private final List<String> headers = new ArrayList<>(PARTITIONS_COUNT*3);
    private final List<Integer> values = new ArrayList<>(PARTITIONS_COUNT*3);
    private final List<String> PHASES = Arrays.asList("Juvenile","Initial","Terminal");

    /**
     * @param minAge
     *            lowest value that can be collected for this class
     * @param maxAge
     *            highest value that can be collected for this class
     */
    private AgeData(Amount<Duration> minAge, Amount<Duration> maxAge) {
        this.minAge = minAge;
        //Amount<Duration> range = maxAge.minus(minAge);
        Amount<Duration> interval = maxAge.divide(PARTITIONS_COUNT);

        Amount<Duration> intervalMin = Amount.valueOf(0, minAge.getUnit());
            for (int i = 0; i < PARTITIONS_COUNT; i++) {
                Amount<Duration> intervalMax = minAge.plus(interval.times(i + 1)).minus(minAge);
                String intervalString = String.format(HEADER_FORMAT_STRING, intervalMin.doubleValue(UnitConstants.AGE_GUI),
                        intervalMax.doubleValue(UnitConstants.AGE_GUI));

                for(int k=0; k<3; ++k) {
                    intervals.add(intervalMax);
                    headers.add(intervalString+"_"+PHASES.get(k));
                    values.add(obtainInitialValue());
                }

                // current interval's maximum is next one's minimum
                intervalMin = intervalMax;
            }
    }

    /**
     * Increase count for partition associated with {@code age}.
     * 
     * @param age
     */
    public void increase(Amount<Duration> age, LifeCycling.Phase phase) {
        int intervalIndex = findIntervalIndex(age, phase);
        int count = values.get(intervalIndex);
        values.set(intervalIndex, count + 1);
    }

    /**
     * 
     * @param age
     * @return index of partition that {@code age} fits into.
     */
    private int findIntervalIndex(Amount<Duration> age, LifeCycling.Phase phase) {
        if (age.isLessThan(minAge)) {
            throw new IllegalArgumentException(age + " is lower than minimum.");
        }

        ListIterator<Amount<Duration>> iterator = intervals.listIterator();
        Amount<Duration> intervalMax;
        int index = 0;
        do {
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(age + " exceeds maximum.");
            }
            index = iterator.nextIndex();
            intervalMax = iterator.next();

        } while (age.isGreaterThan(intervalMax));

        //each interval occurs 3 times, once for each phase
        //index will be the last occurence before next ageclass and therefore terminal phase
        //(juvenile, initial, terminal)
        if(phase == LifeCycling.Phase.INITIAL)
            index += 1;
        else if(phase == LifeCycling.Phase.TERMINAL)
            index += 2;



        return index;
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
            LifeCycling lifeCycling = agent.get(LifeCycling.class);

            if (definition == null || aging == null || lifeCycling == null) {
                return;
            }

            AgeData data = categoryCollectable.getCollectable(definition);
            data.increase(aging.getAge(), lifeCycling.getPhase());
        }
    }

}