package de.zmt.output;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.output.collectable.CategoryCollectable;
import de.zmt.output.collectable.Collectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.UnitConstants;
import sim.util.Proxiable;

/**
 * Population data for a class of agents.
 * <p>
 * Data consists of counts and accumulated mass for total, juvenile, adult
 * female / male agents.
 * 
 * @author mey
 * 
 */
class PopulationData implements Collectable<Number>, Proxiable {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link StrategyCollector} for collecting population data.
     * 
     * @param definitions
     *            the set of species definitions
     * @return the {@link StrategyCollector} for collecting population data
     */
    public static StrategyCollector<?> createCollector(Collection<? extends SpeciesDefinition> definitions) {
        return StrategyCollector.create(new MyCategoryCollectable(definitions), new MyCollectStrategy());
    }

    private static final List<String> HEADERS = Arrays.asList("total_count", "juvenile_count", "adult_female_count",
            "adult_male_count", "total_mass_" + UnitConstants.BIOMASS, "juvenile_mass_" + UnitConstants.BIOMASS,
            "adult_female_mass_" + UnitConstants.BIOMASS, "other_mass_" + UnitConstants.BIOMASS);

    private PopulationData() {
        clear();
    }

    private int totalCount;
    private int juvenileCount;
    private int adultFemaleCount;
    private int adultMaleCount;

    private double totalMass;
    private double juvenileMass;
    private double adultFemaleMass;
    private double adultMaleMass;

    @Override
    public void clear() {
        totalCount = 0;
        juvenileCount = 0;
        adultFemaleCount = 0;
        adultMaleCount = 0;

        totalMass = 0;
        juvenileMass = 0;
        adultFemaleMass = 0;
        adultMaleMass = 0;
    }

    @Override
    public String toString() {
        return "" + totalCount + " [...]";
    }

    @Override
    public Iterable<String> obtainHeaders() {
        return HEADERS;
    }

    @Override
    public Iterable<? extends Number> obtainValues() {
        return Arrays.asList(totalCount, juvenileCount, adultFemaleCount, adultMaleCount, totalMass, juvenileMass,
                adultFemaleMass, adultMaleMass);
    }

    @Override
    public int getSize() {
        return HEADERS.size();
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    private static class MyCategoryCollectable extends CategoryCollectable<SpeciesDefinition, PopulationData, Number> {
        private static final long serialVersionUID = 1L;

        public MyCategoryCollectable(Collection<? extends SpeciesDefinition> definitions) {
            super(definitions.stream()
                    .collect(Collectors.toMap(definition -> definition, definition -> new PopulationData())));
        }

    }

    private static class MyCollectStrategy extends ClearingBeforeStrategy<MyCategoryCollectable> {
        private static final long serialVersionUID = 1L;

        @Override
        protected void collect(AgentCollectMessage message, MyCategoryCollectable collectable) {
            Entity agent = message.getSimObject();

            if (!agent.has(SpeciesDefinition.class)) {
                return;
            }
            SpeciesDefinition definition = agent.get(SpeciesDefinition.class);

            PopulationData classData = collectable.getCollectable(definition);

            if (classData == null) {
                classData = new PopulationData();
            }

            classData.totalCount++;

            if (!agent.has(Growing.class) || !agent.has(LifeCycling.class)) {
                return;
            }
            Growing growing = agent.get(Growing.class);
            LifeCycling lifeCycling = agent.get(LifeCycling.class);

            Amount<Mass> biomass = growing.getBiomass();
            classData.totalMass += biomass.doubleValue(UnitConstants.BIOMASS);

            // fish is adult female
            if (lifeCycling.isAdultFemale()) {
                classData.adultFemaleCount++;
                classData.adultFemaleMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }
            // fish is juvenile
            else if (lifeCycling.getPhase() == Phase.JUVENILE) {
                classData.juvenileCount++;
                classData.juvenileMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }

            // compute count / mass of adult males
            classData.adultMaleCount = classData.totalCount - classData.juvenileCount - classData.adultFemaleCount;
            classData.adultMaleMass = classData.totalMass - classData.juvenileMass - classData.adultFemaleMass;
        }
    }

    public class MyPropertiesProxy {
        public int getTotalCount() {
            return totalCount;
        }

        public int getJuvenileCount() {
            return juvenileCount;
        }

        public int getAdultFemaleCount() {
            return adultFemaleCount;
        }

        public int getOtherCount() {
            return adultMaleCount;
        }

        public double getTotalMass() {
            return totalMass;
        }

        public double getJuvenileMass() {
            return juvenileMass;
        }

        public double getAdultFemaleMass() {
            return adultFemaleMass;
        }

        public double getOtherMass() {
            return adultMaleMass;
        }

        @Override
        public String toString() {
            return PopulationData.this.getClass().getSimpleName();
        }
    }
}