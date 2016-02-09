package sim.params.def;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.measure.quantity.AngularVelocity;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.system.agent.move.MoveSystem.MoveMode;
import de.zmt.storage.ConfigurableStorage;
import de.zmt.util.AmountUtil;
import de.zmt.util.DirectionUtil;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.ParamsUtil;
import de.zmt.util.TimeOfDay;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.LinearMassDensity;
import de.zmt.util.quantity.SpecificEnergy;
import ec.util.MersenneTwisterFast;
import sim.display.GUIState;
import sim.engine.params.def.AbstractParamDefinition;
import sim.engine.params.def.OptionalParamDefinition;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.CheckBoxInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Interval;
import sim.util.Properties;
import sim.util.Proxiable;
import sim.util.SimpleProperties;

/**
 * Parameters for defining a species.
 * 
 * @author mey
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition extends AbstractParamDefinition
	implements OptionalParamDefinition, Proxiable, ProvidesInspector, Component {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SpeciesDefinition.class.getName());
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private final MyPropertiesProxy propertiesProxy = new MyPropertiesProxy();

    /** Number of individuals in initial population. */
    private int initialNum = 500;
    /** Name of species */
    @XmlAttribute
    private String name = "Diurnal Herbivore";

    // MOVEMENT
    /**
     * Contains a speed factor on body length for each {@link BehaviorMode}.
     * 
     * @see #computeBaseSpeed(BehaviorMode, Amount)
     */
    @XmlJavaTypeAdapter(SpeedFactors.MyXmlAdapter.class)
    private final SpeedFactors speedFactors = new SpeedFactors();
    /** Standard deviation of fish speed as a fraction. */
    private static final double SPEED_DEVIATION = 0.1;
    /** Maximum speed the fish can turn with. */
    private Amount<AngularVelocity> maxTurnSpeed = Amount.valueOf(5, UnitConstants.ANGULAR_VELOCITY_GUI)
	    .to(UnitConstants.ANGULAR_VELOCITY);
    /** Mode which movement is based on. */
    private MoveMode moveMode = MoveMode.PERCEPTION;
    /** Radius in which the species can perceive its surroundings. */
    private Amount<Length> perceptionRadius = Amount.valueOf(10, UnitConstants.WORLD_DISTANCE);
    /** Distance of full bias towards attraction center in m. */
    private Amount<Length> maxAttractionDistance = Amount.valueOf(150, METER).to(UnitConstants.WORLD_DISTANCE);
    /** Habitats for resting. */
    private final Set<Habitat> restingHabitats = EnumSet.of(Habitat.CORALREEF);
    /** Habitats for foraging. */
    private final Set<Habitat> foragingHabitats = EnumSet.of(Habitat.SEAGRASS, Habitat.CORALREEF);

    // FEEDING
    /**
     * Maximum amount of food the fish can ingest per biomass within a time
     * span:<br>
     * {@code g dry weight / 1 g biomass / h}.
     * <p>
     * The fish feeds at this rate until sated.
     */
    // TODO Arbitrary value. Get correct one.
    private Amount<Frequency> maxIngestionRate = Amount.valueOf(0.5, UnitConstants.PER_HOUR);
    /**
     * Energy content of food (kJ/g dry weight food).
     * 
     * @see "Bruggemann et al. 1994"
     */
    private Amount<SpecificEnergy> energyContentFood = Amount.valueOf(17.5, UnitConstants.ENERGY_CONTENT_FOOD);
    /** Radius accessible around current position for foraging. */
    private Amount<Length> accessibleForagingRadius = Amount.valueOf(1, UnitConstants.WORLD_DISTANCE);
    /** Which food the species can feed on. */
    private FeedingGuild feedingGuild = FeedingGuild.HERBIVORE;
    /** {@link ActivityPattern} of this species specifying when active. */
    private ActivityPattern activityPattern = ActivityPattern.DIURNAL;

    // DEATH
    /**
     * Natural mortality risk.
     * 
     * @see "McIlwain 2009"
     */
    private Amount<Frequency> mortalityRisk = Amount.valueOf(0.519, UnitConstants.PER_YEAR);
    /** The predation risk associated with each habitat. */
    @XmlJavaTypeAdapter(PredationRisks.MyXmlAdapter.class)
    private final PredationRisks predationRisks = new PredationRisks();
    /**
     * Maximum age {@link Duration}
     * 
     * @see "El-Sayed Ali et al. 2011"
     */
    private Amount<Duration> maxAge = Amount.valueOf(18.75, YEAR).to(UnitConstants.AGE);

    // REPRODUCTION
    /**
     * Probability of female sex when creating fish. Only relevant if
     * {@link SexChangeMode#GONOCHORISTIC} set in their
     * {@link SpeciesDefinition}.
     */
    private static final double FEMALE_PROBABILITY = 0.5;
    /** Number of offsprings per reproduction cycle */
    // TODO arbitrary value. get correct one.
    private int numOffspring = 1;
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
    private Amount<Length> initialPhaseLength = Amount.valueOf(12, CENTIMETER).to(UnitConstants.BODY_LENGTH);
    /**
     * Length when sex change may occur if {@link SexChangeMode#PROTANDROUS} or
     * {@link SexChangeMode#PROTOGYNOUS}.
     */
    private Amount<Length> terminalPhaseLength = Amount.valueOf(17, CENTIMETER).to(UnitConstants.BODY_LENGTH);

    /**
     * Coefficient in length-mass relationship.
     * 
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     * @see FormulaUtil#expectedLength(Amount, Amount, double)
     */
    private Amount<LinearMassDensity> lengthMassCoeff = Amount.valueOf(0.0319, GRAM.divide(CENTIMETER))
	    .to(UnitConstants.MASS_PER_LENGTH);
    /**
     * Degree in length-mass relationship.
     * 
     * @see "El-Sayed Ali et al. 2011"
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private double lengthMassExponent = 2.928;
    /**
     * Stored inverse saved for performance reasons.
     * 
     * @see #lengthMassExponent
     * @see FormulaUtil#expectedLength(Amount, Amount, double)
     */
    @XmlTransient
    private double invLengthMassExponent = 1 / lengthMassExponent;
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
     * Computes base speed from body length and parameter factor associated with
     * given behavior mode.
     * 
     * <pre>
     * base speed in m/s = bodyLength [m] * speedFactor(behaviorMode) [s<sup>-1</sup>]
     * </pre>
     * 
     * @param behaviorMode
     * @param bodyLength
     * @return base speed
     */
    public Amount<Velocity> computeBaseSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength) {
	Amount<Frequency> speedFactor = speedFactors.get(behaviorMode);
	if (speedFactor == null) {
	    throw new IllegalArgumentException("No speed factor set for " + behaviorMode);
	}
	return bodyLength.times(speedFactor).to(UnitConstants.VELOCITY);
    }

    public double getSpeedDeviation() {
	return SPEED_DEVIATION;
    }

    public Amount<AngularVelocity> getMaxTurnSpeed() {
	return maxTurnSpeed;
    }

    public MoveMode getMoveMode() {
	return moveMode;
    }

    public Amount<Length> getPerceptionRadius() {
	return perceptionRadius;
    }

    public Amount<Length> getMaxAttractionDistance() {
	return maxAttractionDistance;
    }

    public Set<Habitat> getRestingHabitats() {
	return Collections.unmodifiableSet(restingHabitats);
    }

    public Set<Habitat> getForagingHabitats() {
	return Collections.unmodifiableSet(foragingHabitats);
    }

    public Amount<Frequency> getMaxIngestionRate() {
	return maxIngestionRate;
    }

    public Amount<SpecificEnergy> getEnergyContentFood() {
	return energyContentFood;
    }

    public Amount<Frequency> getMortalityRisk() {
	return mortalityRisk;
    }

    public Amount<Frequency> getPredationRisk(Habitat habitat) {
	return predationRisks.get(habitat);
    }

    public Amount<Frequency> getMaxPredationRisk() {
	return predationRisks.getMaxPredationRisk();
    }

    public Amount<Duration> getMaxAge() {
	return maxAge;
    }

    public int getNumOffspring() {
	return numOffspring;
    }

    /**
     * @param currentPhase
     * @return length required to enter the next phase
     */
    public Amount<Length> getNextPhaseLength(Phase currentPhase) {
	switch (currentPhase) {
	case JUVENILE:
	    return initialPhaseLength;
	case INITIAL:
	    return terminalPhaseLength;
	default:
	    throw new IllegalArgumentException("No length for next phase when " + currentPhase);
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

    public ActivityPattern getActivityPattern() {
	return activityPattern;
    }

    public BehaviorMode getBehaviorMode(TimeOfDay timeOfDay) {
	switch (activityPattern) {
	case DIURNAL:
	    switch (timeOfDay) {
	    case SUNRISE:
	    case DAY:
		return BehaviorMode.FORAGING;
	    default:
		return BehaviorMode.RESTING;
	    }
	case NOCTURNAL:
	    switch (timeOfDay) {
	    case SUNSET:
	    case NIGHT:
		return BehaviorMode.FORAGING;
	    default:
		return BehaviorMode.RESTING;
	    }
	default:
	    throw new IllegalStateException("Unknown pattern " + activityPattern);
	}
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

    @Override
    public String getTitle() {
	return name;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	propertiesProxy.inspector = new SimpleInspector(new MyProperties(this), state, name);
	return propertiesProxy.inspector;
    }

    @Override
    public Object propertiesProxy() {
	return propertiesProxy;
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);

	invLengthMassExponent = 1 / lengthMassExponent;
    }

    /**
     * Simulated species will pass two phases, initial and terminal, which are
     * accompanied by change of sex. What happens when entering these phases is
     * species-dependent and modeled as different modes.
     * 
     * @author mey
     * 
     */
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
	    default:
		return DEFAULT_GUT_TRANSIT_DURATION;
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
	public double getGutFactorOut() {
	    switch (this) {
	    case HERBIVORE:
		return HERBIVORE_GUT_FACTOR_OUT;
	    default:
		return DEFAULT_GUT_FACTOR_OUT;
	    }
	}

	/** @see "Polunin et al. 1995" */
	private static final Amount<Duration> DEFAULT_GUT_TRANSIT_DURATION = Amount.valueOf(54, MINUTE)
		.to(UnitConstants.SIMULATION_TIME);
	/** @see "Brett &  Groves 1979" */
	private static final double HERBIVORE_LOSS_FACTOR_DIGESTION = 0.43;
	private static final double HERBIVORE_GUT_FACTOR_OUT = 1 / HERBIVORE_LOSS_FACTOR_DIGESTION;

	private static final double DEFAULT_LOSS_FACTOR_DIGESTION = 0.59;
	private static final double DEFAULT_GUT_FACTOR_OUT = 1 / DEFAULT_LOSS_FACTOR_DIGESTION;
    }

    /**
     * Specifies the time of day members of the species are active.
     * 
     * @author mey
     *
     */
    private static enum ActivityPattern {
	/** Active at daytime. */
	DIURNAL,
	/** Active at nighttime. */
	NOCTURNAL
    }

    public class MyPropertiesProxy {
	private Inspector inspector;

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
	    double radians = AmountUtil.parseAmount(maxTurnSpeedString, UnitConstants.ANGULAR_VELOCITY_GUI)
		    .doubleValue(UnitConstants.ANGULAR_VELOCITY);
	    double normalized = DirectionUtil.normalizeAngle(radians);
	    SpeciesDefinition.this.maxTurnSpeed = Amount.valueOf(normalized, UnitConstants.ANGULAR_VELOCITY);
	}

	public int getMoveMode() {
	    return moveMode.ordinal();
	}

	public void setMoveMode(int moveModeOrdinal) {
	    SpeciesDefinition.this.moveMode = MoveMode.values()[moveModeOrdinal];
	}

	public Object[] domMoveMode() {
	    return ParamsUtil.obtainEnumDomain(MoveMode.class);
	}

	public String getPerceptionRadius() {
	    return perceptionRadius.toString();
	}

	public void setPerceptionRadius(String perceptionRangeString) {
	    SpeciesDefinition.this.perceptionRadius = AmountUtil.parseAmount(perceptionRangeString,
		    UnitConstants.WORLD_DISTANCE);
	}

	public String getPostSettlementAge() {
	    return postSettlementAge.to(DAY).toString();
	}

	public void setPostSettlementAge(String postSettlementAgeString) {
	    SpeciesDefinition.this.postSettlementAge = AmountUtil.parseAmount(postSettlementAgeString,
		    UnitConstants.AGE);
	}

	public String getMaxIngestionRate() {
	    return maxIngestionRate.to(UnitConstants.PER_HOUR).toString();
	}

	public void setMaxIngestionRate(String consumptionRateString) {
	    // unit: g dry weight / g biomass = 1
	    SpeciesDefinition.this.maxIngestionRate = AmountUtil
		    .parseAmount(consumptionRateString, UnitConstants.PER_HOUR).to(UnitConstants.PER_STEP);
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

	public double getMortalityRisk() {
	    return mortalityRisk.doubleValue(UnitConstants.PER_YEAR);
	}

	public PredationRisks getPredationRisks() {
	    return predationRisks;
	}

	public void setMortalityRisk(double mortalityRisk) {
	    SpeciesDefinition.this.mortalityRisk = Amount.valueOf(mortalityRisk, UnitConstants.PER_YEAR);
	}

	public String nameMortalityRisk() {
	    return "MortalityRisk_" + UnitConstants.PER_YEAR;
	}

	public Object domMortalityRisk() {
	    return new Interval(0d, 1d);
	}

	public String getMaxAge() {
	    return maxAge.to(UnitConstants.AGE_GUI).toString();
	}

	public void setMaxAge(String maxAgeString) {
	    maxAge = AmountUtil.parseAmount(maxAgeString, UnitConstants.AGE);
	}

	public int getNumOffspring() {
	    return numOffspring;
	}

	public void setNumOffspring(int numOffspring) {
	    SpeciesDefinition.this.numOffspring = numOffspring;
	}

	public String getInitialPhaseLength() {
	    return initialPhaseLength.toString();
	}

	public void setInitialPhaseLength(String initialPhaseLengthString) {
	    SpeciesDefinition.this.initialPhaseLength = AmountUtil.parseAmount(initialPhaseLengthString,
		    UnitConstants.BODY_LENGTH);
	}

	public String getTerminalPhaseLength() {
	    return terminalPhaseLength.toString();
	}

	public void setTerminalPhaseLength(String terminalPhaseLengthString) {
	    SpeciesDefinition.this.terminalPhaseLength = AmountUtil.parseAmount(terminalPhaseLengthString,
		    UnitConstants.BODY_LENGTH);
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

	public String getMaxAttractionDistance() {
	    return maxAttractionDistance.toString();
	}

	public void setMaxAttractionDistance(String maxAttractionDistanceString) {
	    SpeciesDefinition.this.maxAttractionDistance = AmountUtil.parseAmount(maxAttractionDistanceString,
		    UnitConstants.WORLD_DISTANCE);
	}

	public int getSexChangeMode() {
	    return sexChangeMode.ordinal();
	}

	public void setSexChangeMode(int sexChangeModeOrdinal) {
	    SpeciesDefinition.this.sexChangeMode = SexChangeMode.values()[sexChangeModeOrdinal];
	}

	public String[] domSexChangeMode() {
	    return ParamsUtil.obtainEnumDomain(SexChangeMode.class);
	}

	public ProvidesInspector getRestingHabitats() {
	    return new HabitatSetInspector(restingHabitats, "Resting Habitats");
	}

	public ProvidesInspector getForagingHabitats() {
	    return new HabitatSetInspector(foragingHabitats, "Foraging Habitats");
	}

	public double getFemaleProbability() {
	    return FEMALE_PROBABILITY;
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

	public MyProperties(Object o) {
	    super(o, true, false, true);
	}

	@Override
	public String betterToString(Object obj) {
	    if (obj instanceof EnumToAmountMap<?, ?>) {
		return "Click on options button to view";
	    }
	    return super.betterToString(obj);
	}
    }

    /**
     * {@link Inspector} displaying habitats with check boxes to choose from.
     * 
     * @author mey
     *
     */
    private static class HabitatSetInspector extends CheckBoxInspector.ProvidesCheckBoxInspector<Habitat> {
	public HabitatSetInspector(Set<Habitat> habitatSet, String name) {
	    super(habitatSet, Arrays.asList(Habitat.values()), name);
	}
    }
}