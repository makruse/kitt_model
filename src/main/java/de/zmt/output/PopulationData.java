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
 * Data consists of counts and accumulated mass for total, juvenile,
 * reproductive agents.
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

    private static final List<String> HEADERS = Arrays.asList("total_count", "juvenile_count", "reproductive_count",
            "total_mass_" + UnitConstants.BIOMASS, "juvenile_mass_" + UnitConstants.BIOMASS,
            "reproductive_mass_" + UnitConstants.BIOMASS);

    private PopulationData() {
        clear();
    }

    private int totalCount;
    private int juvenileCount;
    private int reproductiveCount;

    private double totalMass;
    private double juvenileMass;
    private double reproductiveMass;

    @Override
    public void clear() {
        totalCount = 0;
        juvenileCount = 0;
        reproductiveCount = 0;

        totalMass = 0;
        juvenileMass = 0;
        reproductiveMass = 0;
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
        return Arrays.asList(totalCount, juvenileCount, reproductiveCount, totalMass, juvenileMass, reproductiveMass);
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

            // fish is reproductive
            if (lifeCycling.isReproductive()) {
                classData.reproductiveCount++;
                classData.reproductiveMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }
            // fish is juvenile
            else if (lifeCycling.getPhase() == Phase.JUVENILE) {
                classData.juvenileCount++;
                classData.juvenileMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }
        }
    }

    public class MyPropertiesProxy {
        public int getTotalCount() {
            return totalCount;
        }

        public int getJuvenileCount() {
            return juvenileCount;
        }

        public int getReproductiveCount() {
            return reproductiveCount;
        }

        public double getTotalMass() {
            return totalMass;
        }

        public double getJuvenileMass() {
            return juvenileMass;
        }

        public double getReproductiveMass() {
            return reproductiveMass;
        }

        @Override
        public String toString() {
            return PopulationData.this.getClass().getSimpleName();
        }
    }
}