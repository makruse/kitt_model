package de.zmt.output;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.output.collectable.AbstractCollectable;
import de.zmt.output.collectable.CategoryCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.strategy.CollectStrategy;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.UnitConstants;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Agents are sorted into partitions ranging from minimum to maximum age.
 * 
 * @author mey
 *
 */
class LengthData extends AbstractCollectable<String> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link StrategyCollector} for collecting age data.
     *
     * @param definitions
     *            the set of species definitions
     * @return the {@link StrategyCollector} for collecting age data
     */
    public static StrategyCollector<?> createCollector(Collection<? extends SpeciesDefinition> definitions) {
        df.applyPattern(FORMAT_PATTERN);//pattern will always be applied when creating a new collector
        intf.applyPattern(INTEGER_PATTERN);
        return StrategyCollector.create(new MyCategoryCollectable(definitions), new MyCollectStrategy());
    }

    private static final int PARTITIONS_COUNT = 9;

    /**
     * formats a number without separator(1,000,000 to 1000000)
     * and with 2 digits in the decimal space(1.79 to 1.8)
     * the 0 makes it so that the digit will always be displayed even if it's 0
     */
    private static final String FORMAT_PATTERN = "##0.0#######";

    /**
     * formats a number without separator(1,000,000 to 1000000)
     * and with 2 digits in the decimal space(1.79 to 1.8)
     * the 0 makes it so that the digit will always be displayed even if it's 0
     * used for integer
     */
    private static final String INTEGER_PATTERN = "##0";

    /**
     * defines the local as US, so a dot(.) is used for the decimal point(e.g. 1000.9 instead of 1000,9)
     */
    private static final Locale LOCALE = new Locale("en", "US");

    /**
     * decimal format for floating point
     */
    private static final DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(LOCALE);
    /**
     * Integer format
     */
    private static final DecimalFormat intf = (DecimalFormat) NumberFormat.getNumberInstance(LOCALE);

    /** Minimum age that can be collected */
    private final Amount<Length> minLength;
    /** Intervals stored as maximum amounts for each partition */
    private final List<Amount<Length>> intervals = new ArrayList<>(PARTITIONS_COUNT*3);
    /** times 3 because 3 phases*/
    private final List<String> headers = new ArrayList<>(PARTITIONS_COUNT*3);
    private final List<String> values = new ArrayList<>(PARTITIONS_COUNT*3);
    private final List<String> PHASES = Arrays.asList("JUV","IP","TP");

    /**
     * @param minLength
     *            lowest value that can be collected for this class
     * @param maxLength
     *            highest value that can be collected for this class
     */
    private LengthData(Amount<Length> minLength, Amount<Length> maxLength) {
        this.minLength = minLength;
        //Amount<Duration> range = maxAge.minus(minAge);
        Amount<Length> interval = Amount.valueOf(2.5f, minLength.getUnit());

        Amount<Length> intervalMin = Amount.valueOf(0, minLength.getUnit());
            for (int i = 0; i < PARTITIONS_COUNT; i++) {
                Amount<Length> intervalMax = minLength.plus(interval.times(i));
                String intervalString = df.format(intervalMin.doubleValue(UnitConstants.BODY_LENGTH))
                        + "-" + df.format(intervalMax.doubleValue(UnitConstants.BODY_LENGTH))
                        + UnitConstants.BODY_LENGTH.toString();



                //interval system is build for a max value so we just set a super high value
                if(i == PARTITIONS_COUNT-1)
                    intervalMax = Amount.valueOf(100, UnitConstants.BODY_LENGTH);

                for(int k=0; k<3; ++k) {
                    intervals.add(intervalMax);
                    headers.add(PHASES.get(k)+"_"+intervalString);
                    values.add(obtainInitialValue());
                }

                // current interval's maximum is next one's minimum
                intervalMin = intervalMax;
            }
    }

    /**
     * Increase count for partition associated with {@code age}.
     * 
     * @param length
     */
    public void increase(Amount<Length> length, LifeCycling.Phase phase) {
        int intervalIndex = findIntervalIndex(length, phase);
        int count = Integer.parseInt(values.get(intervalIndex));
        values.set(intervalIndex, intf.format(count + 1));
    }

    /**
     * 
     * @param length
     * @return index of partition that {@code age} fits into.
     */
    private int findIntervalIndex(Amount<Length> length, LifeCycling.Phase phase) {

        ListIterator<Amount<Length>> iterator = intervals.listIterator();
        int index = 0;
        Amount<Length> intervalMax;
        do {
            index = iterator.nextIndex();
            intervalMax = iterator.next();
        } while (length.isGreaterThan(intervalMax));

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
    protected String obtainInitialValue() {
        return "0";
    }

    @Override
    public List<String> obtainHeaders() {
        return headers;
    }

    @Override
    public List<String> obtainValues() {
        return values;
    }

    /**
     * {@link CategoryCollectable} using {@link SpeciesDefinition} as categories
     * each with an {@link LengthData} object.
     * 
     * @author mey
     *
     */
    private static class MyCategoryCollectable extends CategoryCollectable<SpeciesDefinition, LengthData, String> {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new {@link MyCategoryCollectable}.
         * 
         * @param definitions
         *            the {@link SpeciesDefinition} objects acting as categories
         */
        public MyCategoryCollectable(Collection<? extends SpeciesDefinition> definitions) {
            super(definitions.stream().collect(Collectors.toMap(definition -> definition,
                    definition -> new LengthData(Amount.valueOf(8,UnitConstants.BODY_LENGTH),
                            Amount.valueOf(25.5f,UnitConstants.BODY_LENGTH)))));
        }
    }

    /**
     * {@link CollectStrategy} updating {@link LengthData} for every agent.
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
            Growing growing = agent.get(Growing.class);
            LifeCycling lifeCycling = agent.get(LifeCycling.class);

            if (definition == null || growing == null || lifeCycling == null) {
                return;
            }

            LengthData data = categoryCollectable.getCollectable(definition);
            data.increase(growing.getLength(), lifeCycling.getPhase());
        }
    }

}