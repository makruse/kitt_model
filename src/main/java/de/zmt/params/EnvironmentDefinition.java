package de.zmt.params;

import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.NonSI.MONTH;
import static javax.measure.unit.SI.SECOND;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;

import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.environment.FoodMap.FindFoodConverter;
import de.zmt.ecs.component.environment.MapToWorldConverter;
import de.zmt.util.AmountUtil;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.AreaDensity;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Int2DCache;
import sim.util.Proxiable;

/**
 * Parameters for the environment.
 * 
 * @author mey
 *
 */
public class EnvironmentDefinition extends BaseParamDefinition
        implements Proxiable, Component, FindFoodConverter, MapToWorldConverter {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EnvironmentDefinition.class.getName());

    /** Time instant the simulation starts (2000-01-01 08:00) */
    public static final TemporalAccessor START_TEMPORAL = LocalDateTime.of(2000, 1, 1, 8, 0);
    /** Name of resources directory. Habitat map images are loaded from here. */
    private static final String RESOURCES_DIR = "resources" + File.separator;

    /** Seed value for random number generation. */
    private long seed = 23;
    /**
     * Path to habitat map image. Only valid colors are those returned from
     * {@link Habitat#getColor()}.
     */
    private String mapImagePath = RESOURCES_DIR + "CR.png";
    /** Map scale: pixel per meter */

    @XStreamOmitField
    private double mapScale = 1;
    /**
     * @see #mapScale
     */
    private transient double inverseMapScale = computeInverseMapScale(mapScale);

    /** World area that spans over one pixel or grid cell in map. */
    private transient Amount<Area> pixelArea = computePixelArea(inverseMapScale);

    /** Simulation time passing every step, must be exact. */
    private static final Amount<Duration> STEP_DURATION = Amount.valueOf(1, SECOND);

    /**
     * Proportional increase of algae per time unit.
     * 
     * @see FormulaUtil#growAlgae(Amount, Amount, Amount, Amount)
     */
    private Amount<Frequency> algalGrowthRate = Amount.valueOf(0.001, UnitConstants.PER_DAY);//in %
    /**
     * The maximum number of agents allowed. Larvae will not be created if
     * beyond this count.
     */
    private int maxAgentCount = 175;

    /** Interval in simulation time for writing population data to file. */
    private Amount<Duration> outputPopulationInterval = Amount.valueOf(1, DAY).to(UnitConstants.SIMULATION_TIME);
    /** Interval in simulation time for writing age data to file. */
    private Amount<Duration> outputAgeInterval = Amount.valueOf(1, DAY).to(UnitConstants.SIMULATION_TIME);
    /** Interval in simulation time for writing length data to file. */
    private Amount<Duration> outputLengthInterval = Amount.valueOf(1, DAY).to(UnitConstants.SIMULATION_TIME);
    /** Interval in simulation time for writing stay durations to file. */
    private Amount<Duration> outputStayDurationsInterval = Amount.valueOf(1 , MONTH).to(UnitConstants.SIMULATION_TIME);
    /** Interval in simulation time for writing LifeCycle data to file. */
    private Amount<Duration> outputLifeCylceInterval = Amount.valueOf(1, DAY).to(UnitConstants.SIMULATION_TIME);

    /**
     * used for very specific simulation runs
     * if ignoreSpeciesCount is true then, the value in the speciesDefinition will be ignored,
     * instead the fish will be generated according to the AgentCount values below
     */
    private boolean ignoreSpeciesCount = false;
    private int juvAgentCount = 0;
    private int initAgentCount = 0;
    private int termAgentCount = 0;

    private boolean ageOutput = true;
    private boolean lengthOutput = true;
    private boolean populationOutput = true;
    private boolean stayOutput = true;
    private boolean lifeCyclingOutput = true;

    private static double computeInverseMapScale(double mapScale) {
        return 1 / mapScale;
    }

    private static Amount<Area> computePixelArea(double inverseMapScale) {
        return Amount.valueOf(inverseMapScale * inverseMapScale, UnitConstants.WORLD_AREA);
    }

    /**
     * @see #mapScale
     */
    @Override
    public Int2D worldToMap(Double2D worldCoordinates) {
        Double2D mapCoordinates = worldCoordinates.multiply(mapScale);
        return Int2DCache.get((int) mapCoordinates.x, (int) mapCoordinates.y);
    }

    /**
     * @see #mapScale
     */
    @Override
    public double worldToMap(Amount<Length> worldDistance) {
        return worldDistance.doubleValue(UnitConstants.WORLD_DISTANCE) * mapScale;
    }

    @Override
    public Double2D mapToWorld(Int2D mapCoordinates) {
        return new Double2D(mapCoordinates).multiply(inverseMapScale);
    }

    /**
     * @see #pixelArea
     */
    @Override
    public Amount<Mass> densityToMass(Amount<AreaDensity> density) {
        return density.times(pixelArea).to(UnitConstants.FOOD);
    }

    public long getSeed() {
        return seed;
    }

    public String getMapImagePath() {
        return mapImagePath;
    }

    public double getMapScale() {
        return mapScale;
    }

    public Amount<Duration> getStepDuration() {
        return STEP_DURATION;
    }

    public int getMaxAgentCount() {
        return maxAgentCount;
    }

    public Amount<Duration> getOutputPopulationInterval() {
        return outputPopulationInterval;
    }

    public Amount<Duration> getOutputLifeCycleInterval() {
        return outputLifeCylceInterval;
    }

    public Amount<Duration> getOutputAgeInterval() {
        return outputAgeInterval;
    }

    public Amount<Duration> getOutputLengthInterval() {
        return outputLengthInterval;
    }

    public Amount<Duration> getOutputStayDurationsInterval() {
        return outputStayDurationsInterval;
    }

    public Amount<Frequency> getAlgalGrowthRate() {
        return algalGrowthRate;
    }

    @Override
    public String getTitle() {
        return "Environment";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    // called when deserializing
    private Object readResolve() {
        inverseMapScale = computeInverseMapScale(mapScale);
        pixelArea = computePixelArea(inverseMapScale);
        return this;
    }

    public boolean ignoreSpeciesCount(){
        return ignoreSpeciesCount;
    }

    public int getJuvAgentCount(){
        return juvAgentCount;
    }

    public int getInitAgentCount(){
        return initAgentCount;
    }

    public int getTermAgentCount(){
        return termAgentCount;
    }

    public boolean ageOutput() {
        return ageOutput;
    }

    public boolean lengthOutput() {
        return lengthOutput;
    }

    public boolean populationOutput() {
        return populationOutput;
    }

    public boolean stayOutput() {
        return stayOutput;
    }

    public boolean lifeCyclingOutput() {
        return lifeCyclingOutput;
    }

    @SuppressWarnings("unused")
    public class MyPropertiesProxy {
        public long getSeed() {
            return seed;
        }

        public void setSeed(long seed) {
            EnvironmentDefinition.this.seed = seed;
        }

        public String getMapImagePath() {
            return mapImagePath;
        }

        public void setMapImagePath(String mapImagePath) {
            EnvironmentDefinition.this.mapImagePath = mapImagePath;
        }

        public double getMapScale() {
            return mapScale;
        }

        public void setMapScale(double mapScale) {
            if (mapScale != 1) {
                logger.warning("Dynamic map scale not yet implemented.");
                return;
            }
            EnvironmentDefinition.this.mapScale = mapScale;
            inverseMapScale = computeInverseMapScale(mapScale);
            pixelArea = computePixelArea(inverseMapScale);
        }

        public int getMaxAgentCount() {
            return maxAgentCount;
        }

        public void setMaxAgentCount(int maxAgentCount) {
            EnvironmentDefinition.this.maxAgentCount = maxAgentCount;
        }

        public String getOutputPopulationInterval() {
            return outputPopulationInterval.to(UnitConstants.SIMULATION_TIME).toString();
        }

        public void setOutputPopulationInterval(String outputPopulationIntervalString) {
            Amount<Duration> outputPopulationInterval = AmountUtil.parseAmount(outputPopulationIntervalString,
                    UnitConstants.SIMULATION_TIME);
            if (outputPopulationInterval.isExact() && outputPopulationInterval.getExactValue() > 0) {
                EnvironmentDefinition.this.outputPopulationInterval = outputPopulationInterval;
            }
        }

        public String getOutputAgeInterval() {
            return outputAgeInterval.to(UnitConstants.SIMULATION_TIME).toString();
        }

        public void setOutputAgeInterval(String outputAgeIntervalString) {
            Amount<Duration> outputAgeInterval = AmountUtil.parseAmount(outputAgeIntervalString,
                    UnitConstants.SIMULATION_TIME);
            if (outputAgeInterval.isExact() && outputAgeInterval.getExactValue() > 0) {
                EnvironmentDefinition.this.outputAgeInterval = outputAgeInterval;
            }
        }

        public String getOutputStayDurationsInterval() {
            return outputStayDurationsInterval.to(UnitConstants.SIMULATION_TIME).toString();
        }

        public void setOutputStayDurationsInterval(String outputStayDurationsIntervalString) {
            Amount<Duration> outputStayDurationsInterval = AmountUtil.parseAmount(outputStayDurationsIntervalString,
                    UnitConstants.SIMULATION_TIME);
            if (outputStayDurationsInterval.isExact() && outputStayDurationsInterval.getExactValue() > 0) {
                EnvironmentDefinition.this.outputStayDurationsInterval = outputStayDurationsInterval;
            }
        }

        public String getLifeCycleOutputInterval(){
            return outputLifeCylceInterval.to(UnitConstants.SIMULATION_TIME).toString();
        }

        public void setLifeCycleOutputInterval(String interval){
            Amount<Duration> lifeCycleInterval = AmountUtil.parseAmount(interval,
                    UnitConstants.SIMULATION_TIME);
            if (outputLifeCylceInterval.isExact() && outputLifeCylceInterval.getExactValue() > 0) {
                EnvironmentDefinition.this.outputLifeCylceInterval = lifeCycleInterval;
            }
        }

        public String getAlgalGrowthRate() {
            return algalGrowthRate.toString();
        }

        public void setAlgalGrowthRate(String algalGrowthRateString) {
            EnvironmentDefinition.this.algalGrowthRate = AmountUtil.parseAmount(algalGrowthRateString,
                    UnitConstants.PER_DAY);
        }

        @Override
        public String toString() {
            return EnvironmentDefinition.this.getClass().getSimpleName();
        }


        public boolean isIgnoreSpeciesCount() {
            return ignoreSpeciesCount;
        }

        public void setIgnoreSpeciesCount(boolean ignoreSpeciesCount) {
            EnvironmentDefinition.this.ignoreSpeciesCount = ignoreSpeciesCount;
        }

        public int getJuvAgentCount() {
            return juvAgentCount;
        }

        public void setJuvAgentCount(int juvAgentCount) {
            EnvironmentDefinition.this.juvAgentCount = juvAgentCount;
        }

        public int getInitAgentCount() {
            return initAgentCount;
        }

        public void setInitAgentCount(int initAgentCount) {
            EnvironmentDefinition.this.initAgentCount = initAgentCount;
        }

        public int getTermAgentCount() {
            return termAgentCount;
        }

        public void setTermAgentCount(int termAgentCount) {
            EnvironmentDefinition.this.termAgentCount = termAgentCount;
        }

        public boolean isAgeOutput() {
            return ageOutput;
        }

        public void setAgeOutput(boolean age){
            EnvironmentDefinition.this.ageOutput = age;
        }

        public boolean isLengthOutput() {
            return lengthOutput;
        }

        public void setLengthOutput(boolean lengthOutput) {
            EnvironmentDefinition.this.lengthOutput = lengthOutput;
        }

        public boolean isPopulationOutput() {
            return populationOutput;
        }

        public void setPopulationOutput(boolean populationOutput) {
            EnvironmentDefinition.this.populationOutput = populationOutput;
        }

        public boolean isStayOutput() {
            return stayOutput;
        }

        public void setStayOutput(boolean stayOutput) {
            EnvironmentDefinition.this.stayOutput = stayOutput;
        }

        public boolean isLifeCyclingOutput() {
            return lifeCyclingOutput;
        }

        public void setLifeCyclingOutput(boolean lifeCyclingOutput) {
            EnvironmentDefinition.this.lifeCyclingOutput = lifeCyclingOutput;
        }
    }
}
