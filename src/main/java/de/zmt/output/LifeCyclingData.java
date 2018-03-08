package de.zmt.output;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.output.collectable.Collectable;
import de.zmt.output.collectable.MultiCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.strategy.CollectStrategy;
import de.zmt.util.UnitConstants;
import org.jscience.physics.amount.Amount;
import sim.engine.SimState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;


public class LifeCyclingData implements MultiCollectable<Object> {

    private static Map<UUID,PhaseChange> phaseLog = new HashMap<>();

    static class Headers {
        private static final String ID = "ID";

        private static final String AGE = "AGE";
        private static final String LENGTH = "Length";
        private static final String SEX = "Sex";
        private static final String PHASE = "Phase";
        private static final String DEATH_CAUSE = "Cause of Death";

        private Headers() {
        }

        /** {@link List} containing all headers in order. */
        public static final List<String> LIST = Stream.of(AGE, LENGTH, SEX, PHASE, DEATH_CAUSE, ID)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private static class PhaseChange{
        public String sex;
        public Amount<Duration> age;
        public Amount<Length> length;
        public LifeCycling.Phase phase;
        public LifeCycling.CauseOfDeath cause;
        public UUID id;

        public PhaseChange(UUID id, Amount<Duration> age, Amount<Length> length,
                           String sex, LifeCycling.Phase phase, LifeCycling.CauseOfDeath cause){
            this.id = id;
            this.age = age;
            this.length = length;
            this.sex = sex;
            this.phase = phase;
            this.cause = cause;
        }
    }

    public LifeCyclingData(){
        super();
    }

    public static StrategyCollector<LifeCyclingData> createCollector() {
        return StrategyCollector.create(new LifeCyclingData(),
                new CollectStrategy() {
                    @Override
                    public void process(SimState state, Collectable collectable) {
                        //don't do anything
                    }
                });
    }


    @Override
    public Iterable<String> obtainHeaders() {
        return Headers.LIST;
    }

    @Override
    public Iterable<Collection<Object>> obtainValues() {
        if(phaseLog.isEmpty()) {
            UUID dummyID = new UUID(0,0);
            Amount<Duration> age = Amount.valueOf(-1,UnitConstants.AGE_GUI);
            Amount<Length> length = Amount.valueOf(-1, UnitConstants.BODY_LENGTH);
            phaseLog.put(dummyID,new PhaseChange(dummyID,age, length,"DUMMY", LifeCycling.Phase.INITIAL,
                                LifeCycling.CauseOfDeath.NONE));
        }

        Table<Integer, String, Object> values = ArrayTable
                .create(IntStream.range(0,phaseLog.values().size()).boxed()::iterator,Headers.LIST);
        int rowIndex = 0;

        for(Map.Entry pair: phaseLog.entrySet()){
            values.row(rowIndex).putAll(assembleRow((PhaseChange) pair.getValue()));
            rowIndex++;
        }
        clear();

        return Headers.LIST.stream().map(header -> values.column(header).values())::iterator;
    }

    @Override
    public void clear(){ phaseLog.clear(); }

    public static void registerPhaseChange(UUID id, Amount<Duration> age, Amount<Length> length,
                                           String sex, LifeCycling.Phase phase, LifeCycling.CauseOfDeath cause){
        LifeCyclingData.phaseLog.put(id, new PhaseChange(id, age, length, sex, phase, cause));
    }

    public static void registerPhaseChange(UUID id, Amount<Duration> age, Amount<Length> length,
                                           String sex, LifeCycling.Phase phase){
        LifeCyclingData.phaseLog.put(id, new PhaseChange(id, age, length, sex, phase, LifeCycling.CauseOfDeath.NONE));
    }

    private Map<String, Object> assembleRow(PhaseChange change){
        return Maps.asMap(new HashSet<>(Headers.LIST), header -> {
            switch (header) {
                case Headers.ID:
                    return change.id;
                case Headers.SEX:
                    return change.sex;
                case Headers.PHASE:
                    return change.phase;
                case Headers.DEATH_CAUSE:
                    return change.cause;
                case Headers.AGE:
                        return change.age.to(UnitConstants.AGE_GUI);
                case Headers.LENGTH:
                        return change.length;
                default:
                    return null;
            }
        });
    }
}
