package sim.params.def;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.system.agent.MoveSystem;
import de.zmt.util.*;
import de.zmt.util.quantity.SpecificEnergy;
import sim.display.GUIState;
import sim.engine.params.def.*;
import sim.portrayal.*;
import sim.portrayal.inspector.*;
import sim.util.*;

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
    private int initialNum = 1;
    /** Name of species */
    private String speciesName = "Chlorurus sordidus";

    // MOVEMENT
    /**
     * Contains a speed factor on body length for each {@link BehaviorMode}.
     * 
     * @see #computeBaseSpeed(BehaviorMode, Amount)
     */
    @XmlJavaTypeAdapter(value = SpeedFactorsAdapter.class)
    private final EnumMap<BehaviorMode, Amount<Frequency>> speedFactors = new EnumMap<>(BehaviorMode.class);

    {
	speedFactors.put(BehaviorMode.FORAGING, Amount.valueOf(2.1, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
	speedFactors.put(BehaviorMode.MIGRATING, Amount.valueOf(2.7, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
	speedFactors.put(BehaviorMode.RESTING, Amount.valueOf(0, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
    }

    private static class SpeedFactorsAdapter
	    extends XmlAdapter<SpeedFactorsXmlType, EnumMap<BehaviorMode, Amount<Frequency>>> {

	@Override
	public EnumMap<BehaviorMode, Amount<Frequency>> unmarshal(SpeedFactorsXmlType v) throws Exception {
	    EnumMap<BehaviorMode, Amount<Frequency>> map = new EnumMap<>(BehaviorMode.class);
	    
	    for (SpeedFactorsXmlEntryType entry : v.entries) {
		map.put(entry.key, entry.value);
	    }
	    return map;
	}

	@Override
	public SpeedFactorsXmlType marshal(EnumMap<BehaviorMode, Amount<Frequency>> v) throws Exception {
	    return new SpeedFactorsXmlType(v);
	}
	
    }

    /** Standard deviation of fish speed as a fraction. */
    private static final double SPEED_DEVIATION = 0.2;
    /** Maximum speed the fish can turn with. */
    private Amount<AngularVelocity> maxTurnSpeed = Amount.valueOf(5, UnitConstants.ANGULAR_VELOCITY_GUI);
    /** Mode which movement is based on. */
    private MoveMode moveMode = MoveMode.RANDOM;
    /** Radius in which the species can perceive its surroundings. */
    private Amount<Length> perceptionRadius = Amount.valueOf(10, UnitConstants.WORLD_DISTANCE);
    /** Distance of full bias towards attraction center in m. */
    private Amount<Length> maxAttractionDistance = Amount.valueOf(150, METER).to(UnitConstants.WORLD_DISTANCE);
    /** Habitats for resting. */
    private final Set<Habitat> restingHabitats = EnumSet.of(Habitat.CORALREEF);
    /** Habitats for foraging. */
    private final Set<Habitat> foragingHabitats = EnumSet.of(Habitat.SEAGRASS, Habitat.CORALREEF);
    /** Habitats for spawning. */
    private final Set<Habitat> spawnHabitats = EnumSet.of(Habitat.CORALREEF);

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
     * @see #maxIngestionRate
     */
    @XmlTransient
    private double maxIngestionPerStep = computeMaxConsumptionRatePerStep();
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
    private static final Amount<Duration> INITIAL_AGE = Amount.valueOf(120, DAY).to(UnitConstants.AGE);
    /**
     * Length when fish stops being juvenile and may obtain the ability to
     * reproduce.
     * 
     * @see Phase
     */
    private Amount<Length> initialPhaseLength = Amount.valueOf(12.5, CENTIMETER).to(UnitConstants.BODY_LENGTH);
    /**
     * Length when sex change may occur if {@link SexChangeMode#PROTANDROUS} or
     * {@link SexChangeMode#PROTOGYNOUS}.
     */
    private Amount<Length> terminalPhaseLength = Amount.valueOf(17, CENTIMETER).to(UnitConstants.BODY_LENGTH);

    /**
     * Coefficient in length-mass relationship.
     * 
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private Amount<Mass> lengthMassCoeff = Amount.valueOf(0.0319, GRAM).to(UnitConstants.BIOMASS);
    /**
     * Degree in length-mass relationship.
     * 
     * @see "El-Sayed Ali et al. 2011"
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private double lengthMassDegree = 2.928;
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

    private double computeMaxConsumptionRatePerStep() {
	return maxIngestionRate.times(EnvironmentDefinition.STEP_DURATION).to(Unit.ONE).getEstimatedValue();
    }

    public int getInitialNum() {
	return initialNum;
    }

    public String getSpeciesName() {
	return speciesName;
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

    public Set<Habitat> getSpawnHabitats() {
	return Collections.unmodifiableSet(spawnHabitats);
    }

    /**
     * Consumption per gram biomass in one step. The value is dimensionless
     * because {@code g dry weight / g biomass = 1}.
     * 
     * @return Consumption per step in g dry weight / g biomass
     */
    public double getMaxIngestionPerStep() {
	return maxIngestionPerStep;
    }

    public Amount<SpecificEnergy> getEnergyContentFood() {
	return energyContentFood;
    }

    public Amount<Frequency> getMortalityRisk() {
	return mortalityRisk;
    }

    public Amount<Duration> getMaxAge() {
	return maxAge;
    }

    public double getFemaleProbability() {
	return FEMALE_PROBABILITY;
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

    public FeedingGuild getFeedingGuild() {
	return feedingGuild;
    }

    public ActivityPattern getActivityPattern() {
	return activityPattern;
    }

    public Amount<Duration> getInitialAge() {
	return INITIAL_AGE;
    }

    public Amount<Mass> getLengthMassCoeff() {
	return lengthMassCoeff;
    }

    public double getLengthMassDegree() {
	return lengthMassDegree;
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

    public SexChangeMode getSexChangeMode() {
	return sexChangeMode;
    }

    /**
     * @return {@code true} when species changes sex over lifetime
     * @see SexChangeMode
     */
    public boolean canChangeSex() {
	return sexChangeMode == SexChangeMode.PROTANDROUS || sexChangeMode == SexChangeMode.PROTOGYNOUS;
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);
	maxIngestionPerStep = computeMaxConsumptionRatePerStep();
    }

    @Override
    public String getTitle() {
	return speciesName;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	propertiesProxy.inspector = new SimpleInspector(this, state, name);
	return propertiesProxy.inspector;
    }

    @Override
    public Object propertiesProxy() {
	return propertiesProxy;
    }

    public class MyPropertiesProxy {
	private Inspector inspector;

	public int getInitialNum() {
	    return initialNum;
	}

	public void setInitialNum(int initialNum) {
	    SpeciesDefinition.this.initialNum = initialNum;
	}

	public String getSpeciesName() {
	    return speciesName;
	}

	public void setSpeciesName(String speciesName) {
	    SpeciesDefinition.this.speciesName = speciesName;
	}

	public String getSpeedFactorForaging() {
	    return speedFactors.get(BehaviorMode.FORAGING).toString();
	}

	public void setSpeedFactorForaging(String speedForagingString) {
	    SpeciesDefinition.this.speedFactors.put(BehaviorMode.FORAGING,
		    AmountUtil.parseAmount(speedForagingString, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
	}

	public String getSpeedFactorMigrating() {
	    return speedFactors.get(BehaviorMode.MIGRATING).toString();
	}

	public void setSpeedFactorMigrating(String speedMigratingString) {
	    SpeciesDefinition.this.speedFactors.put(BehaviorMode.MIGRATING,
		    AmountUtil.parseAmount(speedMigratingString, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
	}

	public String getSpeedFactorResting() {
	    return speedFactors.get(BehaviorMode.RESTING).toString();
	}

	public void setSpeedFactorResting(String speedRestingString) {
	    SpeciesDefinition.this.speedFactors.put(BehaviorMode.RESTING,
		    AmountUtil.parseAmount(speedRestingString, UnitConstants.BODY_LENGTH_VELOCITY_GUI));
	}

	public double getSpeedDeviation() {
	    return SPEED_DEVIATION;
	}

	public Object domSpeedDeviation() {
	    return new Interval(0d, 1d);
	}

	public double getMaxTurnSpeed() {
	    return maxTurnSpeed.doubleValue(UnitConstants.ANGULAR_VELOCITY_GUI);
	}

	public void setMaxTurnSpeed(double maxTurnSpeed) {
	    SpeciesDefinition.this.maxTurnSpeed = Amount.valueOf(maxTurnSpeed, UnitConstants.ANGULAR_VELOCITY_GUI)
		    .to(UnitConstants.ANGULAR_VELOCITY);
	}

	public String nameMaxTurnSpeed() {
	    return "MaxTurnSpeed_" + UnitConstants.ANGULAR_VELOCITY_GUI;
	}

	public Object domMaxTurnSpeed() {
	    return new Interval(0d, 180d);
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

	public String getInitialAge() {
	    return SpeciesDefinition.INITIAL_AGE.to(DAY).toString();
	}

	public String getMaxIngestionRate() {
	    return maxIngestionRate.toString();
	}

	public void setMaxIngestionRate(String consumptionRateString) {
	    // unit: g dry weight / g biomass = 1
	    SpeciesDefinition.this.maxIngestionRate = AmountUtil.parseAmount(consumptionRateString,
		    UnitConstants.PER_HOUR);
	    maxIngestionPerStep = computeMaxConsumptionRatePerStep();
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
	    return feedingGuild.getLossFactorDigestion();
	}

	public double getMortalityRisk() {
	    return mortalityRisk.doubleValue(UnitConstants.PER_YEAR);
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
		    UnitConstants.BIOMASS);
	}

	public double getLengthMassDegree() {
	    return lengthMassDegree;
	}

	public void setLengthMassDegree(double lengthMassDegree) {
	    SpeciesDefinition.this.lengthMassDegree = lengthMassDegree;
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

	public ProvidesInspector getSpawnHabitats() {
	    return new HabitatSetInspector(spawnHabitats, "Spawn Habitats");
	}

	public double getFemaleProbability() {
	    return FEMALE_PROBABILITY;
	}
    }

    /**
     * Simulated species will pass two phases, initial and terminal, which are
     * accompanied by change of sex. What happens when entering these phases is
     * species-dependent and modeled as different modes.
     * 
     * @author mey
     * 
     */
    public static enum SexChangeMode {
	/**
	 * Does not change sex during life time. Gets mature when entering the
	 * initial phase. The terminal phase will not be entered.
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
    public static enum FeedingGuild {
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
	 * Loss factor to calculate remaining energy after digestion including
	 * loss due to assimilation, digestion, excretion, specific dynamic
	 * actions.
	 * 
	 * @return loss factor on energy
	 */
	public double getLossFactorDigestion() {
	    switch (this) {
	    case HERBIVORE:
		return HERBIVORE_LOSS_FACTOR_DIGESTION;
	    default:
		return DEFAULT_LOSS_FACTOR_DIGESTION;
	    }
	}

	/** @see "Polunin et al. 1995" */
	private static final Amount<Duration> DEFAULT_GUT_TRANSIT_DURATION = Amount.valueOf(54, MINUTE)
		.to(UnitConstants.SIMULATION_TIME);
	/** @see "Brett &  Groves 1979" */
	private static final double HERBIVORE_LOSS_FACTOR_DIGESTION = 0.43;
	private static final double DEFAULT_LOSS_FACTOR_DIGESTION = 0.59;
    }

    /**
     * Move mode for a species.
     * 
     * @see MoveSystem
     * @author mey
     *
     */
    public static enum MoveMode {
	/** Pure random walk */
	RANDOM,
	/**
	 * Moves towards areas with the highest food supply in perception range.
	 * 
	 * @see SpeciesDefinition#perceptionRadius
	 */
	PERCEPTION,
	/**
	 * Moves towards attraction center.
	 * 
	 * @see SpeciesDefinition#maxAttractionDistance
	 */
	// TODO this should be based on Memorizing component
	MEMORY
    }

    /**
     * Specifies the time of day members of the species are active.
     * 
     * @author mey
     *
     */
    public static enum ActivityPattern {
	/** Active at daytime. */
	DIURNAL,
	/** Active at nighttime. */
	NOCTURNAL
    }

    private static class HabitatSetInspector implements ProvidesInspector {
        private final Set<Habitat> habitatSet;
        private final String name;
    
        public HabitatSetInspector(Set<Habitat> habitatSet, String name) {
            super();
            this.habitatSet = habitatSet;
            this.name = name;
        }
    
        @Override
        public Inspector provideInspector(GUIState state, String name) {
            return new CheckBoxInspector<>(habitatSet, Arrays.asList(Habitat.values()), state,
        	    name != null ? name : this.name);
        }
    
        @Override
        public String toString() {
            return habitatSet.toString();
        }
    }

    private static class SpeedFactorsXmlType {
	public final List<SpeedFactorsXmlEntryType> entries = new ArrayList<SpeedFactorsXmlEntryType>();
    
        @SuppressWarnings("unused") // needed by JAXB
        public SpeedFactorsXmlType() {
    
        }
    
        public SpeedFactorsXmlType(Map<BehaviorMode, Amount<Frequency>> map) {
            for (Map.Entry<BehaviorMode, Amount<Frequency>> e : map.entrySet()) {
        	entries.add(new SpeedFactorsXmlEntryType(e));
            }
        }
    }

    private static class SpeedFactorsXmlEntryType {
        @XmlElement 
	public final BehaviorMode key;
    
        @XmlElement
	public final Amount<Frequency> value;
    
        @SuppressWarnings("unused") // needed by JAXB
        public SpeedFactorsXmlEntryType() {
	    key = null;
	    value = null;
        }
    
        public SpeedFactorsXmlEntryType(Map.Entry<BehaviorMode, Amount<Frequency>> e) {
            key = e.getKey();
            value = e.getValue();
        }
    }
}