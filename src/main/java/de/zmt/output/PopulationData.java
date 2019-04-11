package de.zmt.output;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
class PopulationData implements Collectable<String>, Proxiable {
    private static final long serialVersionUID = 1L;


    /**
     * formats a number without separator(1,000,000 to 1000000)
     * and with 2 digits in the decimal space(1.79 to 1.8)
     * the 0 makes it so that the digit will always be displayed even if it's 0
     * used for floating points
     */
    private static final String FORMAT_PATTERN = "##0.0";

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

    /**
     * Creates a {@link StrategyCollector} for collecting population data.
     * 
     * @param definitions
     *            the set of species definitions
     * @return the {@link StrategyCollector} for collecting population data
     */
    public static StrategyCollector<?> createCollector(Collection<? extends SpeciesDefinition> definitions) {
        df.applyPattern(FORMAT_PATTERN);
        intf.applyPattern(INTEGER_PATTERN);
        return StrategyCollector.create(new MyCategoryCollectable(definitions), new MyCollectStrategy());
    }

    private static final List<String> HEADERS = Arrays.asList("TOTAL_count", "JUV_count", "IP_count",
            "TP_count", "TOTAL_mass(" + UnitConstants.BIOMASS+")", "JUV_mass(" + UnitConstants.BIOMASS+")",
            "IP_mass(" + UnitConstants.BIOMASS+")", "TP_mass(" + UnitConstants.BIOMASS+")");

    private PopulationData() {
        clear();
    }

    private int totalCount;
    private int juvenileCount;
    private int initialCount;
    private int terminalCount;

    private double totalMass;
    private double juvenileMass;
    private double initialMass;
    private double terminalMass;

    @Override
    public void clear() {
        totalCount = 0;
        juvenileCount = 0;
        initialCount = 0;
        terminalCount = 0;

        totalMass = 0;
        juvenileMass = 0;
        initialMass = 0;
        terminalMass = 0;
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
    public Iterable<String> obtainValues() {
        return Arrays.asList(intf.format(totalCount), intf.format(juvenileCount), intf.format(initialCount),
                intf.format(terminalCount), df.format(totalMass), df.format(juvenileMass),df.format(initialMass),
                df.format(terminalMass));
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    private static class MyCategoryCollectable extends CategoryCollectable<SpeciesDefinition, PopulationData, String> {
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
                classData.initialCount++;
                classData.initialMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }
            // fish is juvenile
            else if (lifeCycling.getPhase() == Phase.JUVENILE) {
                classData.juvenileCount++;
                classData.juvenileMass += biomass.doubleValue(UnitConstants.BIOMASS);
            }

            // compute count / mass of adult males
            classData.terminalCount = classData.totalCount - classData.juvenileCount - classData.initialCount;
            classData.terminalMass = classData.totalMass - classData.juvenileMass - classData.initialMass;
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
            return initialCount;
        }

        public int getAdultMaleCount() {
            return terminalCount;
        }

        public double getTotalMass() {
            return totalMass;
        }

        public double getJuvenileMass() {
            return juvenileMass;
        }

        public double getAdultFemaleMass() {
            return initialMass;
        }

        public double getAdultMaleMass() {
            return terminalMass;
        }

        @Override
        public String toString() {
            return PopulationData.this.getClass().getSimpleName();
        }
    }
}