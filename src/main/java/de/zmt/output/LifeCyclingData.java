package de.zmt.output;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.factory.FishFactory;
import de.zmt.output.collectable.Collectable;
import de.zmt.output.collectable.MultiCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.util.UnitConstants;
import org.jscience.physics.amount.Amount;
import sim.engine.SimState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;


public class LifeCyclingData implements MultiCollectable<Object> {

    private static Map<Entity,LifeData> phaseLog = new HashMap<>();
    private static Map<Entity,UUID> ids = new HashMap<>();

    static class Headers {
        private static final String ID = "ID";

        private static final String AGE = "AGE";
        private static final String LENGTH = "Length";
        private static final String BIOMASS = "Biomass";
        private static final String INGESTED_ENERGY = "Ingested Energy";
        private static final String NET_ENERGY = "Netenergy";
        private static final String CONSUMED_ENERGY = "Consumed Energy";
        private static final String SEX = "Sex";
        private static final String PHASE = "Phase";
        private static final String DEATH_CAUSE = "Cause of Death";

        private Headers() {
        }

        /** {@link List} containing all headers in order. */
        public static final List<String> LIST = Stream.of(SEX, PHASE, AGE, LENGTH, BIOMASS, INGESTED_ENERGY,
                NET_ENERGY, CONSUMED_ENERGY, DEATH_CAUSE, ID)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private static class LifeData {
        public String sex;
        public Amount<Duration> age;
        public Amount<Length> length;
        public Amount<Mass> biomass;
        public Amount<Energy> ingestedEnergy;
        public Amount<Energy> netEnergy;
        public Amount<Energy> consumedEnergy;
        public LifeCycling.Phase phase;
        public LifeCycling.CauseOfDeath cause;
        public UUID id;

        public LifeData(UUID id, Amount<Duration> age, Amount<Length> length, Amount<Mass> biomass,
                        Amount<Energy> ingestedEnergy, Amount<Energy> netEnergy, Amount<Energy> consumedEnergy,
                        String sex, LifeCycling.Phase phase, LifeCycling.CauseOfDeath cause){
            this.id = id;
            this.age = age;
            this.length = length;
            this.biomass = biomass;
            this.ingestedEnergy = ingestedEnergy;
            this.netEnergy = netEnergy;
            this.consumedEnergy = consumedEnergy;
            this.sex = sex;
            this.phase = phase;
            this.cause = cause;
        }
    }

    public LifeCyclingData(){
        super();
    }

    public static StrategyCollector<LifeCyclingData> createCollector() {
        return StrategyCollector.create(new LifeCyclingData(), new LifeCycleCollectStrategy());
    }


    @Override
    public Iterable<String> obtainHeaders() {
        return Headers.LIST;
    }

    @Override
    public Iterable<Collection<Object>> obtainValues() {
      /*  if(phaseLog.isEmpty()) {
           Entity dummyID = new Entity();
            Amount<Duration> age = Amount.valueOf(-1,UnitConstants.AGE_GUI);
            Amount<Length> length = Amount.valueOf(-1, UnitConstants.BODY_LENGTH);
            Amount<Mass> mass = Amount.valueOf(-1, UnitConstants.BIOMASS);
            Amount<Energy> energy = Amount.valueOf(-1, UnitConstants.CELLULAR_ENERGY);
            phaseLog.put(dummyID,new LifeData(dummyID,age, length,mass,energy, energy, energy,
                    "DUMMY", LifeCycling.Phase.INITIAL, LifeCycling.CauseOfDeath.NONE));
        }*/

        Table<Integer, String, Object> values = ArrayTable
                .create(IntStream.range(0,phaseLog.values().size()).boxed()::iterator,Headers.LIST);
        int rowIndex = 0;

        for(Map.Entry pair: phaseLog.entrySet()){
            values.row(rowIndex).putAll(assembleRow((LifeData) pair.getValue()));
            rowIndex++;
        }
        clear();

        return Headers.LIST.stream().map(header -> values.column(header).values())::iterator;
    }

    @Override
    public void clear(){ phaseLog.clear(); }

    public static void registerFish(Entity fish){
        try {
            if(!LifeCyclingData.ids.containsKey(fish))
                LifeCyclingData.ids.put(fish, UUID.randomUUID());

            Growing growing = fish.get(Growing.class);
            Aging aging = fish.get(Aging.class);
            LifeCycling lifeCycling = fish.get(LifeCycling.class);
            Metabolizing metabolizing = fish.get(Metabolizing.class);

            LifeCyclingData.phaseLog.put(fish, new LifeData(LifeCyclingData.ids.get(fish),
                                            aging.getAge(),
                                            growing.getLength(),
                                            growing.getBiomass(),
                                            metabolizing.getIngestedEnergy(),
                                            metabolizing.getNetEnergy(),
                                            metabolizing.getConsumedEnergy(),
                                            lifeCycling.getSex(),
                                            lifeCycling.getPhase(),
                                            lifeCycling.getCauseOfDeath()));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            System.out.println("Fish is missing component needed for lifeData Log");
        }
    }


    private Map<String, Object> assembleRow(LifeData change){
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
                case Headers.BIOMASS:
                    return change.biomass;
                case Headers.INGESTED_ENERGY:
                    return change.ingestedEnergy;
                case Headers.NET_ENERGY:
                    return change.netEnergy;
                case Headers.CONSUMED_ENERGY:
                    return change.consumedEnergy;
                default:
                    return null;
            }
        });
    }

    private static class LifeCycleCollectStrategy extends MessageCollectStrategy<LifeCyclingData, AgentCollectMessage> {
        private static final long serialVersionUID = 1L;

        @Override
        protected void collect(AgentCollectMessage message, LifeCyclingData collectable) {
            Entity agent = message.getSimObject();

            collectable.registerFish(agent);
        }

        @Override
        protected CollectMessageFactory<AgentCollectMessage> getCollectMessageFactory() {
            return AgentCollectMessage.FACTORY;
        }

        @Override
        public void process(SimState state, LifeCyclingData collectable) {
            super.process(state, collectable);
        }
    }
}
