package de.zmt.output;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.output.collectable.MultiCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.storage.Compartment;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import org.jscience.physics.amount.Amount;
import sim.engine.SimState;
import sim.util.Int2D;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.measure.quantity.Duration;


public class LifeCyclingData implements MultiCollectable<Object> {

    private static Map<Entity,LifeData> phaseLog = new HashMap<>();
    private static Map<Entity,UUID> ids = new HashMap<>();
    private static HabitatMap habitatMap;
    private static FoodMap foodMap;

    /**
     * formats a number without separator(1,000,000 to 1000000)
     * and with 2 digits in the decimal space(1.79 to 1.8)
     * the 0 makes it so that the digit will always be displayed even if it's 0
     * used for floating points
     */
    private static final String FORMAT_PATTERN = "##0.0###########";

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

    static class Headers {
        private static final String ID = "ID";
        //expressions must be constant, therefore unit is hardcoded instead of "UnitConstants.<unit>.toString()"
        private static final String AGE = "AGE(years)";
        private static final String LENGTH = "Length(cm)";
        private static final String BIOMASS = "Biomass(g)";
        private static final String ENERGY = "Energy(kJ)";
        private static final String INGESTED_ENERGY = "Ingested_Energy(kJ)";
        private static final String NET_ENERGY = "Netenergy(kJ)";
        private static final String CONSUMED_ENERGY = "Consumed_Energy(kJ)";
        private static final String IS_HUNGRY = "isHungry";
        private static final String SEX = "Sex";
        private static final String PHASE = "Phase";
        private static final String DEATH_CAUSE = "Cause_of_Death";
        private static final String HABITAT = "Habitat";
        private static final String FOOD_VALUE = "Food_Value(kJ)";
        private static final String POS_X = "Pos_X";
        private static final String POS_Y = "Pos_Y";
        private static final String REPRO_STORAGE = "Repro_Storage(kJ)";
        private static final String REPRODUCTIONS = "Reproductions";
        private static final String GUT = "Gut(kJ)";
        private static final String PROTEIN = "Protein(kJ)";
        private static final String FAT = "Fat(kJ)";
        private static final String EXCESS = "Excess(kJ)";
        private static final String SHORTTERM = "Shorrterm(kJ)";

        private Headers() {
        }

        /** {@link List} containing all headers in order. */
        public static final List<String> LIST = Stream.of(SEX, PHASE, AGE, LENGTH, BIOMASS, ENERGY, REPRODUCTIONS,
                REPRO_STORAGE, GUT, PROTEIN, FAT, EXCESS, SHORTTERM, INGESTED_ENERGY, NET_ENERGY, CONSUMED_ENERGY,
                IS_HUNGRY, DEATH_CAUSE, HABITAT, FOOD_VALUE, POS_X, POS_Y, ID)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private static class LifeData {
        /*
         * using double instead of amount
         * because double will be written only as a number
         * without a unit(year, cm, g etc.) every time
         */
        public String sex;
        public Amount<Duration> age;
        public double length;
        public double biomass;
        public double energy;
        public double ingestedEnergy;
        public double netEnergy;
        public double consumedEnergy;
        public boolean isHungry;
        public LifeCycling.Phase phase;
        public LifeCycling.CauseOfDeath cause;
        public Habitat habitat;
        public double foodValue;
        public int pos_X;
        public int pos_Y;
        public double reproductionStorage;
        public double gut;
        public double protein;
        public double fat;
        public double excess;
        public double shortterm;
        public int reproductions;
        public UUID id;

        public LifeData(UUID id, Amount<Duration> age, double length, double biomass, double energy,
                        double reproductionStorage, int reproductions,double gut, double protein,
                        double fat, double  excess, double  shortterm, double ingestedEnergy, double netEnergy,
                        double consumedEnergy, boolean isHungry, String sex, LifeCycling.Phase phase,
                        LifeCycling.CauseOfDeath cause, Habitat habitat, double foodValue, int pos_X, int pos_Y){
                this.id = id;
                this.age = age;
                this.length = length;
                this.biomass = biomass;
                this.energy = energy;
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
                this.isHungry = isHungry;
                this.sex = sex;
                this.phase = phase;
                this.cause = cause;
                this.habitat = habitat;
                this.foodValue = foodValue;
                this.pos_X = pos_X;
                this.pos_Y = pos_Y;
            }
        }

    public LifeCyclingData(HabitatMap habitatMap, FoodMap foodMap){
            super();
            LifeCyclingData.habitatMap = habitatMap;
            LifeCyclingData.foodMap = foodMap;
        }

        public static StrategyCollector<LifeCyclingData> createCollector(HabitatMap habitatMap, FoodMap foodMap) {
            df.applyPattern(FORMAT_PATTERN);
            intf.applyPattern(INTEGER_PATTERN);
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
                        growing.getLength().getEstimatedValue(),
                        growing.getBiomass().getEstimatedValue(),
                        growing.getEnergy().getEstimatedValue(),
                        compartments.getStorageAmount(Compartment.Type.REPRODUCTION).getEstimatedValue(),
                        compartments.getReproductionsSinceLastUpdate(),
                        compartments.getStorageAmount(Compartment.Type.GUT).getEstimatedValue(),
                        compartments.getStorageAmount(Compartment.Type.PROTEIN).getEstimatedValue(),
                        compartments.getStorageAmount(Compartment.Type.FAT).getEstimatedValue(),
                        compartments.getStorageAmount(Compartment.Type.EXCESS).getEstimatedValue(),
                        compartments.getStorageAmount(Compartment.Type.SHORTTERM).getEstimatedValue(),
                        metabolizing.getIngestedEnergy().getEstimatedValue(),
                        metabolizing.getNetEnergyIngested().getEstimatedValue(),
                        metabolizing.getConsumedEnergy().getEstimatedValue(),
                        compartments.isHungry(),
                        lifeCycling.getSex(),
                        lifeCycling.getPhase(),
                        lifeCycling.getCauseOfDeath(),
                        LifeCyclingData.habitatMap.obtainHabitat(position),
                        LifeCyclingData.foodMap.getFoodDensity(position.x,position.y).getEstimatedValue(),
                        position.x,
                        position.y));
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
                    return change.id.toString();
                case Headers.SEX:
                    return change.sex;
                case Headers.PHASE:
                    return change.phase.toString();
                case Headers.DEATH_CAUSE:
                    return change.cause.toString();
                case Headers.AGE:
                    return df.format(change.age.doubleValue(UnitConstants.AGE_GUI));
                case Headers.LENGTH:
                    return df.format(change.length);
                case Headers.BIOMASS:
                    return df.format(change.biomass);
                case Headers.ENERGY:
                    return df.format(change.energy);
                case Headers.INGESTED_ENERGY:
                    return df.format(change.ingestedEnergy);
                case Headers.NET_ENERGY:
                    return df.format(change.netEnergy);
                case Headers.CONSUMED_ENERGY:
                    return df.format(change.consumedEnergy);
                case Headers.HABITAT:
                    return change.habitat.toString();
                case Headers.FOOD_VALUE:
                    return df.format(change.foodValue);
                case Headers.POS_X:
                    return intf.format(change.pos_X);
                case Headers.POS_Y:
                    return  intf.format(change.pos_Y);
                case Headers.REPRO_STORAGE:
                    return df.format(change.reproductionStorage);
                case Headers.REPRODUCTIONS:
                    return intf.format(change.reproductions);
                case Headers.GUT:
                    return df.format(change.gut);
                case Headers.PROTEIN:
                    return df.format(change.protein);
                case Headers.FAT:
                    return df.format(change.fat);
                case Headers.EXCESS:
                    return df.format(change.excess);
                case Headers.SHORTTERM:
                    return df.format(change.shortterm);
                case Headers.IS_HUNGRY:
                    return change.isHungry;
                    default:
                    return "N/A";
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
