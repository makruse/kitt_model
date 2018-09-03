package de.zmt.output;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.factory.FishFactory;
import de.zmt.output.collectable.Collectable;
import de.zmt.output.collectable.MultiCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.storage.Compartment;
import de.zmt.storage.ReproductionStorage;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.AreaDensity;
import org.jscience.physics.amount.Amount;
import sim.engine.SimState;
import sim.util.Int2D;

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
    private static HabitatMap habitatMap;
    private static FoodMap foodMap;

    static class Headers {
        private static final String ID = "ID";

        private static final String AGE = "AGE";
        private static final String LENGTH = "Length";
        private static final String BIOMASS = "Biomass";
        private static final String INGESTED_ENERGY = "Ingested_Energy";
        private static final String NET_ENERGY = "Netenergy";
        private static final String CONSUMED_ENERGY = "Consumed_Energy";
        private static final String SEX = "Sex";
        private static final String PHASE = "Phase";
        private static final String DEATH_CAUSE = "Cause_of_Death";
        private static final String HABITAT = "Habitat";
        private static final String FOOD_VALUE = "Food_Value";
        private static final String REPRO_STORAGE = "Repro_Storage";
        private static final String REPRODUCTIONS = "Reproductions";
        private static final String GUT = "Gut";
        private static final String PROTEIN = "Protein";
        private static final String FAT = "Fat";
        private static final String EXCESS = "Excess";
        private static final String SHORTTERM = "Shorrterm";

        private Headers() {
        }

        /** {@link List} containing all headers in order. */
        public static final List<String> LIST = Stream.of(SEX, PHASE, AGE, LENGTH, BIOMASS, REPRODUCTIONS,
                REPRO_STORAGE, GUT, PROTEIN, FAT, EXCESS, SHORTTERM, INGESTED_ENERGY, NET_ENERGY, CONSUMED_ENERGY,
                DEATH_CAUSE, HABITAT, FOOD_VALUE, ID)
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
        public Habitat habitat;
        public Amount<AreaDensity> foodValue;
        public Amount<Energy> reproductionStorage;
        public Amount<Energy> gut;
        public Amount<Energy> protein;
        public Amount<Energy> fat;
        public Amount<Energy> excess;
        public Amount<Energy> shortterm;
        public int reproductions;
        public UUID id;

        public LifeData(UUID id, Amount<Duration> age, Amount<Length> length, Amount<Mass> biomass,
                        Amount<Energy> reproductionStorage, int reproductions,Amount<Energy> gut, Amount<Energy> protein,
                        Amount<Energy> fat, Amount<Energy>  excess, Amount<Energy>  shortterm,
                        Amount<Energy> ingestedEnergy, Amount<Energy> netEnergy, Amount<Energy> consumedEnergy,
                        String sex, LifeCycling.Phase phase, LifeCycling.CauseOfDeath cause, Habitat habitat,
                        Amount<AreaDensity> foodValue){
            this.id = id;
            this.age = age;
            this.length = length;
            this.biomass = biomass;
            this.reproductionStorage = reproductionStorage;
            this.reproductions = reproductions;
            this.gut = gut;
            this.protein = protein;
            this.fat = fat;
            this.excess = excess;
            this.shortterm = shortterm;
            this.ingestedEnergy = ingestedEnergy;
            this.netEnergy = netEnergy;
            this.consumedEnergy = consumedEnergy;
            this.sex = sex;
            this.phase = phase;
            this.cause = cause;
            this.habitat = habitat;
            this.foodValue = foodValue;
        }
    }

    public LifeCyclingData(HabitatMap habitatMap, FoodMap foodMap){
        super();
        LifeCyclingData.habitatMap = habitatMap;
        LifeCyclingData.foodMap = foodMap;
    }

    public static StrategyCollector<LifeCyclingData> createCollector(HabitatMap habitatMap, FoodMap foodMap) {
        return StrategyCollector.create(new LifeCyclingData(habitatMap, foodMap), new LifeCycleCollectStrategy());
    }


    @Override
    public Iterable<String> obtainHeaders() {
        return Headers.LIST;
    }

    @Override
    public Iterable<Collection<Object>> obtainValues() {
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
            Moving moving = fish.get(Moving.class);
            Int2D position = moving.getMapPosition();
            Compartments compartments = fish.get(Compartments.class);

            LifeCyclingData.phaseLog.put(fish, new LifeData(LifeCyclingData.ids.get(fish),
                                            aging.getAge(),
                                            growing.getLength(),
                                            growing.getBiomass(),
                                            compartments.getStorageAmount(Compartment.Type.REPRODUCTION),
                                            compartments.getReproductionsSinceLastUpdate(),
                                            compartments.getStorageAmount(Compartment.Type.GUT),
                                            compartments.getStorageAmount(Compartment.Type.PROTEIN),
                                            compartments.getStorageAmount(Compartment.Type.FAT),
                                            compartments.getStorageAmount(Compartment.Type.EXCESS),
                                            compartments.getStorageAmount(Compartment.Type.SHORTTERM),
                                            metabolizing.getIngestedEnergy(),
                                            metabolizing.getNetEnergy(),
                                            metabolizing.getConsumedEnergy(),
                                            lifeCycling.getSex(),
                                            lifeCycling.getPhase(),
                                            lifeCycling.getCauseOfDeath(),
                                            LifeCyclingData.habitatMap.obtainHabitat(position),
                                            LifeCyclingData.foodMap.getFoodDensity(position.x,position.y)));
            compartments.clearReproductionSinceLastUpdate();
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
                case Headers.HABITAT:
                    return change.habitat;
                case Headers.FOOD_VALUE:
                    return change.foodValue;
                case Headers.REPRO_STORAGE:
                    return change.reproductionStorage;
                case Headers.REPRODUCTIONS:
                    return change.reproductions;
                case Headers.GUT:
                    return change.gut;
                case Headers.PROTEIN:
                    return change.protein;
                case Headers.FAT:
                    return change.fat;
                case Headers.EXCESS:
                    return change.excess;
                case Headers.SHORTTERM:
                    return change.shortterm;
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
