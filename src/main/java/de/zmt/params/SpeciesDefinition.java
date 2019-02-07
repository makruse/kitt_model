package de.zmt.params;

import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.NonSI.YEAR;
import static javax.measure.unit.SI.CENTIMETER;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.RADIAN;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.measure.quantity.AngularVelocity;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.system.agent.move.MoveSystem.MoveMode;
import de.zmt.params.accessor.NotAutomatable;
import de.zmt.pathfinding.PathfindingMapType;
import de.zmt.storage.ConfigurableStorage;
import de.zmt.util.AmountUtil;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.ParamsUtil;
import de.zmt.util.TimeOfDay;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.LinearMassDensity;
import de.zmt.util.quantity.SpecificEnergy;
import ec.util.MersenneTwisterFast;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.ParamsInspector.InspectorRemovable;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Interval;
import sim.util.Properties;
import sim.util.Proxiable;
import sim.util.Rotation2D;
import sim.util.SimpleProperties;

/**
 * Parameters for defining a species.
 * 
 * @author mey
 * 
 */
@XStreamAlias("SpeciesDefinition")
@InspectorRemovable
public class SpeciesDefinition extends BaseParamDefinition implements Proxiable, ProvidesInspector, Component {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SpeciesDefinition.class.getName());
    private static final long serialVersionUID = 1L;

    private transient MyPropertiesProxy propertiesProxy;

    /** Number of individuals in initial population. */
    private int initialNum = 500;
    /** Name of the species. */
    @XStreamAsAttribute
    @NotAutomatable
    private String name = "Parrotfish";

    // MOVEMENT
    /**
     * Contains a speed factor on body length for each {@link BehaviorMode}.
     * 
     * @see #determineSpeed(BehaviorMode, Amount, MersenneTwisterFast)
     */
    private final SpeedFactors speedFactors = new SpeedFactors();

    /** Standard deviation of fish speed as a fraction. */
    private static final double SPEED_DEVIATION = 0.1;

    /** Maximum rotation speed. */
    private Amount<AngularVelocity> maxTurnSpeed = Amount.valueOf(5, UnitConstants.ANGULAR_VELOCITY_GUI)
            .to(UnitConstants.ANGULAR_VELOCITY);

    /** Mode which movement is based on. */
    private MoveMode moveMode = MoveMode.PERCEPTION;

    /**
     * Radius in which the agent can perceive its surroundings. The radius is
     * measured around the cell the agent resides on, e.g. a radius of 1 will
     * make the agent perceive only the adjacent cells.
     */
    private Amount<Length> perceptionRadius = Amount.valueOf(3, UnitConstants.WORLD_DISTANCE);

    /** Preferred habitats per {@link BehaviorMode}. */
    private final PreferredHabitats preferredHabitats = new PreferredHabitats();

    /** Weight factors for pathfinding. */
    private final PathfindingWeights pathfindingWeights = new PathfindingWeights();

    /**
     * Desired number of cells the agent passes during one update. If set above
     * {@code 1} cells will be skipped and not taken into account. Set to
     * {@code 0} to disable step skip optimization
     */
    private double cellPassPerUpdate = 1;

    // FEEDING
    /**
     * Maximum amount of food the fish can ingest per biomass within a time
     * span:<br>
     * {@code g dry weight / 1 g biomass / day}.
     * <p>
     * The fish feeds at this rate until sated.
     */
    private Amount<Frequency> meanIngestionRate = Amount.valueOf(0.236, UnitConstants.PER_DAY)
            .to(UnitConstants.PER_SIMULATION_TIME);

    private Amount<Frequency> maxIngestionRate = Amount.valueOf(0.4, UnitConstants.PER_DAY)
            .to(UnitConstants.PER_SIMULATION_TIME);
    /**
     * Energy content of food (kJ/g dry weight food).
     * 
     * @see "Bruggemann et al. 1994"
     */
    //TODO double check food value
    private Amount<SpecificEnergy> energyContentFood = Amount.valueOf(7.5, UnitConstants.ENERGY_CONTENT_FOOD);//17.5

    /** Radius accessible around current position for foraging. */
    private Amount<Length> accessibleForagingRadius = Amount.valueOf(1, UnitConstants.WORLD_DISTANCE);

    /** Which food the species can feed on. */
    private FeedingGuild feedingGuild = FeedingGuild.HERBIVORE;

    /** {@link ActivityPattern} of this species specifying when active. */
    private ActivityPattern activityPattern = ActivityPattern.DIURNAL;

    /** Short-term maximum storage capacity on RMR. */
    private Amount<Duration> shorttermUpperLimitRmr = Amount.valueOf(9, HOUR);

    /**
     * Excess desired storage capacity on RMR. Fish will be hungry until desired
     * excess is achieved.
     */
    private Amount<Duration> desiredExcessRmr = Amount.valueOf(1, HOUR);;

    // DEATH
    /**
     * Natural mortality risk.
     * 
     * @see "McIlwain 2009"
     */
    private Amount<Frequency> naturalMortalityRisk = Amount.valueOf(0.519, UnitConstants.PER_YEAR)
            .to(UnitConstants.PER_DAY);

    /** The predation risk factors associated with each habitat. */
    private final PredationRiskFactors predationRiskFactors = new PredationRiskFactors();

    /**
     * Average maximum age {@link Duration}. A variation of +/-
     * {@value #MAX_AGE_DEVIATION} determines the maximum life span of an agent.
     */
    private Amount<Duration> maxAgeAverage = Amount.valueOf(10, YEAR).to(UnitConstants.AGE);

    /** Maximum deviation of {@link #maxAgeAverage}. */
    private static final double MAX_AGE_DEVIATION = 0.1;

    // REPRODUCTION
    /**
     * Probability of female sex when creating fish. Only relevant if
     * {@link SexChangeMode#GONOCHORISTIC} set in their
     * {@link SpeciesDefinition}.
     */
    private static final double FEMALE_PROBABILITY = 0.7;

    /** Number of offsprings per reproduction cycle */
    private int numOffspring = 2;

    /**
     * @see SexChangeMode
     */
    private SexChangeMode sexChangeMode = SexChangeMode.PROTOGYNOUS;

    // GROWTH
    /** Default initial age for fish when entering the simulation. */
    private Amount<Duration> postSettlementAge = Amount.valueOf(120, DAY).to(UnitConstants.AGE);

    /**
     * Length when fish stops being juvenile and may obtain the ability to
     * reproduce.
     * 
     * @see Phase
     */
    private Amount<Length> initialPhaseStartLength = Amount.valueOf(12, CENTIMETER).to(UnitConstants.BODY_LENGTH);
    private Amount<Length> iP50PercentMaturityLength = Amount.valueOf(15, CENTIMETER).to(UnitConstants.BODY_LENGTH);

    /**
     * Length when sex change may occur if {@link SexChangeMode#PROTANDROUS} or
     * {@link SexChangeMode#PROTOGYNOUS}.
     */
    private Amount<Length> terminalPhaseStartLength = Amount.valueOf(17, CENTIMETER).to(UnitConstants.BODY_LENGTH);
    private Amount<Length> tP50PercentMaturityLength = Amount.valueOf(20, CENTIMETER).to(UnitConstants.BODY_LENGTH);

    /**
     * Coefficient in length-mass relationship.
     * 
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     * @see FormulaUtil#expectedLength(Amount, Amount, double)
     */
    private Amount<LinearMassDensity> lengthMassCoeff = Amount.valueOf(0.0309, GRAM.divide(CENTIMETER))
            .to(UnitConstants.MASS_PER_LENGTH);

    /**
     * Degree in length-mass relationship.
     * 
     * @see "El-Sayed Ali et al. 2011"
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private double lengthMassExponent = 2.935;

    /**
     * Stored inverse saved for performance reasons.
     * 
     * @see #lengthMassExponent
     * @see FormulaUtil#expectedLength(Amount, Amount, double)
     */
    private transient double invLengthMassExponent = 1 / lengthMassExponent;

    /**
     * A parameter of the von Bertalanffy Growth Function (VBGF), expressing the
     * mean length the fish of this species / stock would reach if they were to
     * grow for an infinitely long period. Not the largest observed size of a
     * species.
     * 
     * @see FormulaUtil#expectedLength(Amount, double, Amount, Amount)
     * @see <a href=
     *      "http://www.fishbase.de/glossary/Glossary.php?q=asymptotic+length">
     *      FishBase Glossary: Asymptotic Length</a>
     */
    private Amount<Length> asymptoticLength = Amount.valueOf(39.1, CENTIMETER).to(UnitConstants.BODY_LENGTH);

    /**
     * Curvature parameter in the von Bertalanffy Growth Function (VBGF)
     * defining steepness of growth curve, how fast the fish approaches its
     * {@link #asymptoticLength}.
     * 
     * @see FormulaUtil#expectedLength(Amount, double, Amount, Amount)
     */
    private double growthCoeff = 0.15;

    /**
     * Age at which the fish has a size of zero. Parameter for the von
     * Bertalanffy Growth Function (VBGF).
     */
    private Amount<Duration> zeroSizeAge = Amount.valueOf(-1.25, YEAR);

    public int getInitialNum() {
        return initialNum;
    }

    public String getName() {
        return name;
    }

    /**
     * Determines speed from body length and parameter factor associated with
     * given behavior mode. A proportional random deviation is added.
     * 
     * <pre>
     * speed in m/s = bodyLength [m] * speedFactor(behaviorMode) [1/s] +/- deviation
     * </pre>
     * 
     * @param behaviorMode
     *            the current behavior mode of the agent
     * @param bodyLength
     *            the body length of the agent
     * @param random
     *            the random number generator of the simulation
     * @return speed from given influences
     */
    //TODO if time and possible move logic to DesiredDirectionMovement.computeSpeed()
    public Amount<Velocity> determineSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength,
            MersenneTwisterFast random) {
        Amount<Frequency> speedFactor = speedFactors.get(behaviorMode);
        if (speedFactor == null) {
            throw new IllegalArgumentException("No speed factor set for " + behaviorMode);
        }
        // factor is zero, no need to compute anything further
        if (speedFactor.getEstimatedValue() == 0) {
            return AmountUtil.zero(UnitConstants.VELOCITY);
        }
        Amount<Velocity> averageSpeed = bodyLength.times(speedFactor).to(UnitConstants.VELOCITY);

        // random value between +speedDeviation and -speedDeviation
        double randomDeviation = nextDoubleWithNegative(random) * SPEED_DEVIATION;
        return averageSpeed.plus(averageSpeed.times(randomDeviation));
    }

    /**
     * Computes maximum rotation for the given step duration.
     * 
     * @param stepDuration
     *            the step duration to compute for
     * @return the {@link Rotation2D} object containing the maximum rotation
     */
    public Rotation2D determineMaxRotationPerStep(Amount<Duration> stepDuration) {
        double radianPerStep = maxTurnSpeed.times(stepDuration).to(RADIAN).getEstimatedValue();
        return Rotation2D.fromAngle(radianPerStep);
    }

    public MoveMode getMoveMode() {
        return moveMode;
    }

    public Amount<Length> getPerceptionRadius() {
        return perceptionRadius;
    }

    /**
     * 
     * @param mode
     *            the {@link BehaviorMode}
     * @return preferred habitat for given mode
     */
    public Set<Habitat> getPreferredHabitats(BehaviorMode mode) {
        Set<Habitat> habitats = preferredHabitats.get(mode);
        if (habitats == null) {
            throw new IllegalArgumentException("No preferred habitat for " + mode);
        }
        return habitats;
    }

    /**
     * Returns the weight factor for given {@link PathfindingMapType}.
     * 
     * @param type
     *            the type of pathfinding map
     * @return the weight factor
     */
    public double getPathfindingWeight(PathfindingMapType type) {
        return pathfindingWeights.get(type);
    }

    public double getCellPassPerUpdate() {
        return cellPassPerUpdate;
    }

    //TODO add variance to ingestionRate
    public Amount<Frequency> getMeanIngestionRate() {
        return meanIngestionRate;
    }

    public Amount<Frequency> getMaxIngestionRate(){
        return maxIngestionRate;
    }

    public Amount<SpecificEnergy> getEnergyContentFood() {
        return energyContentFood;
    }

    public Amount<Frequency> getNaturalMortalityRisk() {
        return naturalMortalityRisk;
    }

    public double getPredationRiskFactor(Habitat habitat) {
        return predationRiskFactors.get(habitat);
    }

    public double getMinPredationRiskFactor() {
        return predationRiskFactors.getMinRiskFactor();
    }

    public double getMaxPredationRiskFactor() {
        return predationRiskFactors.getMaxRiskFactor();
    }

    /**
     * @see #determineMaxAge(MersenneTwisterFast)
     * @return the overall maximum age that cannot be exceeded by any member of
     *         this species
     */
    public Amount<Duration> getOverallMaxAge() {
        return maxAgeAverage.plus(maxAgeAverage.times(MAX_AGE_DEVIATION));
    }

    /**
     * Determines maximum age for an individual of this species.
     * 
     * @param random
     *            the random number generator of the simulation
     * @return the maximum age for an individual of this species
     */
    public Amount<Duration> determineMaxAge(MersenneTwisterFast random) {
        return maxAgeAverage.plus(maxAgeAverage.times(nextDoubleWithNegative(random) * MAX_AGE_DEVIATION));
    }

    /**
     * Creates an age distribution based on the probabilities for each phase and
     * the set phase lengths.
     * 
     * @param random
     *            the random number generator for this simulation
     * @return age distribution
     */
    public AgeDistribution createAgeDistribution(MersenneTwisterFast random) {
        Amount<Duration> initialPhaseAge = FormulaUtil.expectedAge(asymptoticLength, growthCoeff, initialPhaseStartLength,
                zeroSizeAge);
        Amount<Duration> terminalPhaseAge = FormulaUtil.expectedAge(asymptoticLength, growthCoeff, terminalPhaseStartLength,
                zeroSizeAge);

        return new AgeDistribution(postSettlementAge, maxAgeAverage.minus(maxAgeAverage.times(MAX_AGE_DEVIATION)),
                initialPhaseAge, terminalPhaseAge, random);
    }

    public int getNumOffspring() {
        return numOffspring;
    }

    public Amount<Length> getNextPhase50PercentMaturityLength(Phase currentPhase){
        switch (currentPhase){
            case JUVENILE:
                return iP50PercentMaturityLength;
            case INITIAL:
                return tP50PercentMaturityLength;
                default:
                    throw new IllegalArgumentException("No endlength for next phase when " + currentPhase);
        }
    }

    /**
     * @param currentPhase
     * @return length required to enter the next phase
     */
    public Amount<Length> getNextPhaseStartLength(Phase currentPhase) {
        switch (currentPhase) {
        case JUVENILE:
            return initialPhaseStartLength;
        case INITIAL:
            return terminalPhaseStartLength;
        default:
            throw new IllegalArgumentException("No startlength for next phase when " + currentPhase);
        }
    }

    public Amount<Length> getAccessibleForagingRadius() {
        return accessibleForagingRadius;
    }

    public Amount<Duration> getGutTransitDuration() {
        return feedingGuild.getGutTransitDuration();
    }

    public double getGutFactorOut() {
        return feedingGuild.getGutFactorOut();
    }

    /**
     * @param timeOfDay
     * @return {@link BehaviorMode} of this species for given {@code timeOfDay}
     */
    public BehaviorMode getBehaviorMode(TimeOfDay timeOfDay) {
        switch (timeOfDay) {
        case SUNRISE:
        case SUNSET:
            return BehaviorMode.MIGRATING;
        case DAY:
            if (activityPattern == ActivityPattern.DIURNAL) {
                return BehaviorMode.FORAGING;
            }
        case NIGHT:
            if (activityPattern == ActivityPattern.NOCTURNAL) {
                return BehaviorMode.FORAGING;
            }
        default:
            return BehaviorMode.RESTING;
        }
    }

    public Amount<Duration> getDesiredExcessRmr() {
        return desiredExcessRmr;
    }

    public Amount<Duration> getShorttermUpperLimitRmr() {
        return shorttermUpperLimitRmr;
    }

    public Amount<Duration> getPostSettlementAge() {
        return postSettlementAge;
    }

    public Amount<LinearMassDensity> getLengthMassCoeff() {
        return lengthMassCoeff;
    }

    public double getLengthMassExponent() {
        return lengthMassExponent;
    }

    public double getInvLengthMassExponent() {
        return invLengthMassExponent;
    }

    public Amount<Length> getAsymptoticLength() {
        return asymptoticLength;
    }

    public double getGrowthCoeff() {
        return growthCoeff;
    }

    public Amount<Duration> getZeroSizeAge() {
        return zeroSizeAge;
    }

    /**
     * Determine sex at birth.
     * 
     * @param random
     *            random number generator
     * @return sex at birth
     */
    public Sex determineSex(MersenneTwisterFast random) {
        switch (sexChangeMode) {
        case GONOCHORISTIC:
            return random.nextBoolean(FEMALE_PROBABILITY) ? Sex.FEMALE : Sex.MALE;
        case PROTANDROUS:
            return Sex.MALE;
        case PROTOGYNOUS:
            return Sex.FEMALE;
        default:
            throw new IllegalArgumentException("Sex at birth for " + sexChangeMode + " is undefined.");
        }
    }

    /**
     * @return {@code true} when species changes sex over lifetime
     * @see SexChangeMode
     */
    public boolean canChangeSex() {
        return sexChangeMode == SexChangeMode.PROTANDROUS || sexChangeMode == SexChangeMode.PROTOGYNOUS;
    }

    /**
     * Returns a random number in the range [-1,1).
     * 
     * @param random
     *            the random number generator
     * @return a random number in the range [-1,1)
     */
    private static double nextDoubleWithNegative(MersenneTwisterFast random) {
        return random.nextDouble() * 2 - 1;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        MyProperties properties = new MyProperties(this);
        properties.sort(properties.makeAlphabeticalComparator());
        propertiesProxy.inspector = new SimpleInspector(properties, state, name);
        return propertiesProxy.inspector;
    }

    @Override
    public Object propertiesProxy() {
        if (propertiesProxy == null) {
            propertiesProxy = new MyPropertiesProxy();
        }
        return propertiesProxy;
    }

    // called when deserializing
    private Object readResolve() {
        invLengthMassExponent = 1 / lengthMassExponent;
        return this;
    }

    /**
     * Simulated species will pass two phases, initial and terminal, which are
     * accompanied by change of sex. What happens when entering these phases is
     * species-dependent and modeled as different modes.
     * 
     * @author mey
     * 
     */
    @XStreamAlias("SexChangeMode")
    private static enum SexChangeMode {
        /**
         * Starting with a random sex and do not changes it over lifetime.
         * 
         * @see SpeciesDefinition#FEMALE_PROBABILITY
         */
        GONOCHORISTIC,
        /**
         * Starting as male when entering the initial phase, turns out as
         * females in the terminal phase.
         */
        PROTANDROUS,
        /**
         * Female when entering the initial phase, male in terminal phase.
         */
        PROTOGYNOUS
    }

    /**
     * Species that feed on similar resources share the same feeding guild.
     * 
     * @author mey
     * 
     */
    @XStreamAlias("FeedingGuild")
    private static enum FeedingGuild {
        /** Feeds on plants. */
        HERBIVORE,
        /** Feeds on invertebrates. */
        PISCIVORE,
        /** Feeds on plants and meat. */
        OMNIVORE,
        /** Feeds on plankton. */
        PLANKTIVORE,
        /** Feeds on decomposing dead plants or animals. */
        DETRIVORE;

        /**
         * @return food transit time through gut in minutes
         */
        public Amount<Duration> getGutTransitDuration() {
            switch (this) {
                case HERBIVORE: //currently only one used
                    return HERBIVORE_GUT_TRANSIT_DURATION;
            default:
                return HERBIVORE_GUT_TRANSIT_DURATION;
            }
        }

        /**
         * Out factor for gut to calculate remaining energy after digestion
         * including loss loss due to assimilation, digestion, excretion,
         * specific dynamic actions.
         *
         * @see ConfigurableStorage
         * @return out factor for gut
         */
        //TODO rename
        public double getGutFactorOut() {
            switch (this) {
            case HERBIVORE:
                return HERBIVORE_GUT_FACTOR_OUT;
            default:
                return DEFAULT_GUT_FACTOR_OUT;
            }
        }

        /** @see "Polunin et al. 1995" */
        private static final Amount<Duration> HERBIVORE_GUT_TRANSIT_DURATION = Amount.valueOf(54, MINUTE)
                .to(UnitConstants.SIMULATION_TIME);
        /** @see "Brüggemann et al. 1994" */
        private static final double HERBIVORE_ASSIMILATION_EFFICIENCY = 0.20;
        private static final double HERBIVORE_GUT_FACTOR_OUT = 1 / HERBIVORE_ASSIMILATION_EFFICIENCY;

        /** @see "Brett &  Groves 1979" */
        private static final double DEFAULT_ASSIMILATION_EFFICIENCY = 0.59;
        private static final double DEFAULT_GUT_FACTOR_OUT = 1 / DEFAULT_ASSIMILATION_EFFICIENCY;
    }

    /**
     * Specifies the time of day members of the species are active.
     * 
     * @author mey
     *
     */
    @XStreamAlias("ActivityPattern")
    private static enum ActivityPattern {
        /** Active at daytime. */
        DIURNAL,
        /** Active at nighttime. */
        NOCTURNAL
    }

    @SuppressWarnings("unused")
    public class MyPropertiesProxy {
        // empty inspector if none set
        private Inspector inspector = new Inspector() {
            private static final long serialVersionUID = 1L;

            @Override
            public void updateInspector() {
            }
        };

        public int getInitialNum() {
            return initialNum;
        }

        public void setInitialNum(int initialNum) {
            SpeciesDefinition.this.initialNum = initialNum;
        }

        public String getName() {
            return name;
        }

        public void setName(String speciesName) {
            SpeciesDefinition.this.name = speciesName;
        }

        public SpeedFactors getSpeedFactors() {
            return speedFactors;
        }

        public double getSpeedDeviation() {
            return SPEED_DEVIATION;
        }

        public Object domSpeedDeviation() {
            return new Interval(0d, 1d);
        }

        public String getMaxTurnSpeed() {
            return maxTurnSpeed.to(UnitConstants.ANGULAR_VELOCITY_GUI).toString();
        }

        public void setMaxTurnSpeed(String maxTurnSpeedString) {
            Amount<AngularVelocity> parsedAmount = AmountUtil
                    .parseAmount(maxTurnSpeedString, UnitConstants.ANGULAR_VELOCITY_GUI)
                    .to(UnitConstants.ANGULAR_VELOCITY);
            if (parsedAmount.getEstimatedValue() < Math.PI) {
                SpeciesDefinition.this.maxTurnSpeed = parsedAmount;
            }
        }

        public int getMoveMode() {
            return moveMode.ordinal();
        }

        public void setMoveMode(int moveModeOrdinal) {
            SpeciesDefinition.this.moveMode = MoveMode.values()[moveModeOrdinal];
            inspector.updateInspector();
        }

        public Object[] domMoveMode() {
            return ParamsUtil.obtainEnumDomain(MoveMode.class);
        }

        public String getPerceptionRadius() {
            return perceptionRadius.toString();
        }

        public void setPerceptionRadius(String perceptionRangeString) {
            Amount<Length> parsedAmount = AmountUtil.parseAmount(perceptionRangeString, UnitConstants.WORLD_DISTANCE);
            if (parsedAmount.getEstimatedValue() < 1) {
                logger.warning("Perception Radius cannot be less than 1.");
                return;
            }
            SpeciesDefinition.this.perceptionRadius = parsedAmount;
        }

        public boolean hidePerceptionRadius() {
            if (moveMode == MoveMode.PERCEPTION) {
                return false;
            }
            return true;
        }

        public String getPostSettlementAge() {
            return postSettlementAge.to(DAY).toString();
        }

        public void setPostSettlementAge(String postSettlementAgeString) {
            SpeciesDefinition.this.postSettlementAge = AmountUtil.parseAmount(postSettlementAgeString,
                    UnitConstants.AGE);
        }

        public String getMeanIngestionRate() {
            return meanIngestionRate.to(UnitConstants.PER_HOUR).toString();
        }

        public void setMeanIngestionRate(String consumptionRateString) {
            // unit: g dry weight / g biomass = 1
            SpeciesDefinition.this.meanIngestionRate = AmountUtil
                    .parseAmount(consumptionRateString, UnitConstants.PER_HOUR).to(UnitConstants.PER_SIMULATION_TIME);
        }

        public String getEnergyContentFood() {
            return energyContentFood.toString();
        }

        public void setEnergyContentFood(String energyDensityFoodString) {
            SpeciesDefinition.this.energyContentFood = AmountUtil.parseAmount(energyDensityFoodString,
                    UnitConstants.ENERGY_CONTENT_FOOD);
        }

        public String getGutTransitDuration() {
            return feedingGuild.getGutTransitDuration().toString();
        }

        public double getLossFactorDigestion() {
            return feedingGuild.getGutFactorOut();
        }

        public PredationRiskFactors getPredationRiskFactors() {
            return predationRiskFactors;
        }

        public String getNaturalMortalityRisk() {
            return naturalMortalityRisk.to(UnitConstants.PER_YEAR).toString();
        }

        public void setNaturalMortalityRisk(String naturalMortalityRiskString) {
            SpeciesDefinition.this.naturalMortalityRisk = AmountUtil.parseAmount(naturalMortalityRiskString,
                    UnitConstants.PER_DAY);
        }

        public String getMaxAgeAverage() {
            return maxAgeAverage.to(UnitConstants.AGE_GUI).toString();
        }

        public void setMaxAgeAverage(String maxAgeAverageString) {
            maxAgeAverage = AmountUtil.parseAmount(maxAgeAverageString, UnitConstants.AGE);
        }

        public double getMaxAgeDeviation() {
            return MAX_AGE_DEVIATION;
        }

        public int getNumOffspring() {
            return numOffspring;
        }

        public void setNumOffspring(int numOffspring) {
            SpeciesDefinition.this.numOffspring = numOffspring;
        }

        public String getInitialPhaseStartLength() {
            return initialPhaseStartLength.toString();
        }

        public void setInitialPhaseStartLength(String initialPhaseLengthString) {
            SpeciesDefinition.this.initialPhaseStartLength = AmountUtil.parseAmount(initialPhaseLengthString,
                    UnitConstants.BODY_LENGTH);
        }

        public String getIP50PercentMaturityLength(){ return iP50PercentMaturityLength.toString(); }

        public void setIP50PercentMaturityLength(String valueString) {
            SpeciesDefinition.this.iP50PercentMaturityLength = AmountUtil.parseAmount(valueString, UnitConstants.BODY_LENGTH);
        }

        public String getTerminalPhaseStartLength() {
            return terminalPhaseStartLength.toString();
        }

        public void setTerminalPhaseStartLength(String terminalPhaseLengthString) {
            SpeciesDefinition.this.terminalPhaseStartLength = AmountUtil.parseAmount(terminalPhaseLengthString,
                    UnitConstants.BODY_LENGTH);
        }

        public String getTP50PercentMaturityLength(){ return tP50PercentMaturityLength.toString(); }

        public void setTP50PercentMaturityLength(String valueString) {
            SpeciesDefinition.this.tP50PercentMaturityLength = AmountUtil.parseAmount(valueString, UnitConstants.BODY_LENGTH);
        }

        public String getAccessibleForagingRadius() {
            return accessibleForagingRadius.toString();
        }

        public void setAccessibleForagingRadius(String accessibleForagingRadiusString) {
            SpeciesDefinition.this.accessibleForagingRadius = AmountUtil.parseAmount(accessibleForagingRadiusString,
                    UnitConstants.WORLD_DISTANCE);
        }

        public int getFeedingGuild() {
            return feedingGuild.ordinal();
        }

        public void setFeedingGuild(int feedingGuildOrdinal) {
            SpeciesDefinition.this.feedingGuild = FeedingGuild.values()[feedingGuildOrdinal];
            inspector.updateInspector();
        }

        public String[] domFeedingGuild() {
            return ParamsUtil.obtainEnumDomain(FeedingGuild.class);
        }

        public int getActivityPattern() {
            return activityPattern.ordinal();
        }

        public void setActivityPattern(int activityPatternOrdinal) {
            SpeciesDefinition.this.activityPattern = ActivityPattern.values()[activityPatternOrdinal];
        }

        public String[] domActivityPattern() {
            return ParamsUtil.obtainEnumDomain(ActivityPattern.class);
        }

        public String getShorttermUpperLimitRmr() {
            return shorttermUpperLimitRmr.toString();
        }

        public void setShorttermUpperLimitRmr(String shorttermUpperLimitRmrString) {
            SpeciesDefinition.this.shorttermUpperLimitRmr = AmountUtil.parseAmount(shorttermUpperLimitRmrString, HOUR);
        }

        public String getDesiredExcessRmr() {
            return desiredExcessRmr.toString();
        }

        public void setDesiredExcessRmr(String desiredExcessRmrString) {
            SpeciesDefinition.this.desiredExcessRmr = AmountUtil.parseAmount(desiredExcessRmrString, HOUR);
        }

        public String getLengthMassCoeff() {
            return lengthMassCoeff.toString();
        }

        public void setLengthMassCoeff(String lengthMassCoeffString) {
            SpeciesDefinition.this.lengthMassCoeff = AmountUtil.parseAmount(lengthMassCoeffString,
                    UnitConstants.MASS_PER_LENGTH);
        }

        public double getLengthMassExponent() {
            return lengthMassExponent;
        }

        public void setLengthMassExponent(double lengthMassExponent) {
            SpeciesDefinition.this.lengthMassExponent = lengthMassExponent;
            SpeciesDefinition.this.invLengthMassExponent = 1 / lengthMassExponent;
        }

        public String getAsymptoticLength() {
            return asymptoticLength.toString();
        }

        public void setAsymptoticLength(String growthLengthString) {
            SpeciesDefinition.this.asymptoticLength = AmountUtil.parseAmount(growthLengthString,
                    UnitConstants.BODY_LENGTH);
        }

        public double getGrowthCoeff() {
            return growthCoeff;
        }

        public void setGrowthCoeff(double growthCoeff) {
            SpeciesDefinition.this.growthCoeff = growthCoeff;
        }

        public String getZeroSizeAge() {
            return zeroSizeAge.to(UnitConstants.AGE_GUI).toString();
        }

        public void setZeroSizeAge(String zeroSizeAge) {
            // Used exclusively for vBGF, so we save this parameter in years.
            SpeciesDefinition.this.zeroSizeAge = AmountUtil.parseAmount(zeroSizeAge, YEAR);
        }

        public int getSexChangeMode() {
            return sexChangeMode.ordinal();
        }

        public void setSexChangeMode(int sexChangeModeOrdinal) {
            SpeciesDefinition.this.sexChangeMode = SexChangeMode.values()[sexChangeModeOrdinal];
            inspector.updateInspector();
        }

        public String[] domSexChangeMode() {
            return ParamsUtil.obtainEnumDomain(SexChangeMode.class);
        }

        public PreferredHabitats getPreferredHabitats() {
            return preferredHabitats;
        }

        public boolean hidePreferredHabitats() {
            if (moveMode == MoveMode.PERCEPTION) {
                return false;
            }
            return true;
        }

        public double getFemaleProbability() {
            return FEMALE_PROBABILITY;
        }

        public boolean hideFemaleProbability() {
            if (sexChangeMode == SexChangeMode.GONOCHORISTIC) {
                return false;
            }
            return true;
        }

        public PathfindingWeights getPathfindingWeights() {
            return pathfindingWeights;
        }

        public boolean hidePathfindingWeights() {
            if (moveMode == MoveMode.PERCEPTION) {
                return false;
            }
            return true;
        }

        public double getCellPassPerUpdate() {
            return cellPassPerUpdate;
        }

        public void setCellPassPerUpdate(double cellPassPerUpdate) {
            if (cellPassPerUpdate >= 0) {
                SpeciesDefinition.this.cellPassPerUpdate = cellPassPerUpdate;
            }
        }

        @Override
        public String toString() {
            return SpeciesDefinition.this.getClass().getSimpleName();
        }
    }

    /**
     * {@link Properties} displaying custom string representation for certain
     * properties.
     * 
     * @author mey
     *
     */
    private static class MyProperties extends SimpleProperties {
        private static final long serialVersionUID = 1L;

        private static final Set<Class<?>> TO_STRING_OVERRIDE_CLASSES = new HashSet<>(Arrays.<Class<?>> asList(
                SpeedFactors.class, PredationRiskFactors.class, PathfindingWeights.class,
                PreferredHabitats.class));

        public MyProperties(Object o) {
            super(o, true, false, true);
        }

        @Override
        public String betterToString(Object obj) {
            if (TO_STRING_OVERRIDE_CLASSES.contains(obj.getClass())) {
                return "Click on options button to view";
            }
            return super.betterToString(obj);
        }

        /**
         * Properties are volatile because some of them are dynamically hidden.
         */
        @Override
        public boolean isVolatile() {
            return true;
        }

    }
}