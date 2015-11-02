package sim.params.def;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.logging.Logger;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.system.agent.MoveSystem;
import de.zmt.util.*;
import de.zmt.util.quantity.SpecificEnergy;
import sim.engine.params.def.*;
import sim.util.*;

/**
 * Parameters for defining a species.
 * 
 * @author cmeyer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition extends AbstractParamDefinition implements
	OptionalParamDefinition, Proxiable, Component {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(SpeciesDefinition.class.getName());
    private static final long serialVersionUID = 1L;

    /** Number of individuals in initial population. */
    private int initialNum = 1;
    /** name of species */
    @XmlAttribute
    private String speciesName = "Chlorurus sordidus";

    // MOVEMENT
    // TODO Arbitrary speed values. Get correct ones.
    /** Basic speed of fish while foraging. */
    private Amount<Velocity> speedForaging = Amount.valueOf(0.2,
	    METERS_PER_SECOND).to(UnitConstants.VELOCITY);
    /** Basic speed of fish while resting. */
    private Amount<Velocity> speedResting = Amount.valueOf(0.05,
	    METERS_PER_SECOND).to(UnitConstants.VELOCITY);
    /** Standard deviation of fish speed as a fraction. */
    private double speedDeviation = 0.2;
    /** Mode which movement is based on. */
    private MoveMode moveMode = MoveMode.RANDOM;
    /** Radius in which the species can perceive its surroundings. */
    private Amount<Length> perceptionRadius = Amount.valueOf(10,
	    UnitConstants.WORLD_DISTANCE);
    /** Distance of full bias towards attraction center in m. */
    private Amount<Length> maxAttractionDistance = Amount.valueOf(150, METER).to(UnitConstants.WORLD_DISTANCE);
    private static final Habitat RESTING_HABITAT = Habitat.CORALREEF;
    private static final Habitat FORAGING_HABITAT = Habitat.SEAGRASS;
    private static final Habitat SPAWN_HABITAT = Habitat.CORALREEF;

    // FEEDING
    /**
     * Maximum amount of food the fish can consume per biomass within a time
     * span:<br>
     * {@code g dry weight / g biomass / h}.
     * <p>
     * The fish feeds at this rate until sated.
     */
    // TODO Arbitrary value. Get correct one.
    private Amount<Frequency> maxConsumptionRate = Amount.valueOf(0.5,
	    UnitConstants.PER_HOUR);
    /** @see #maxConsumptionRate */
    @XmlTransient
    private double maxConsumptionPerStep = computeMaxConsumptionRatePerStep();
    /**
     * Energy content of food (kJ/g dry weight food).
     * 
     * @see "Bruggemann et al. 1994"
     */
    private Amount<SpecificEnergy> energyContentFood = Amount.valueOf(17.5,
	    UnitConstants.ENERGY_CONTENT_FOOD);
    /**
     * Food transit time through gut in minutes.
     * 
     * @see "Polunin et al. 1995"
     */
    private Amount<Duration> gutTransitDuration = Amount.valueOf(54, MINUTE)
	    .to(UnitConstants.SIMULATION_TIME);
    /**
     * Energy remaining after digestion including loss due to assimilation,
     * digestion, excretion, specific dynamic actions.
     * 
     * @see "Brett &  Groves 1979"
     */
    // TODO change value automatically according to FeedingGuild
    private double lossFactorDigestion = 0.43;
    /** Radius accessible around current position for foraging. */
    private Amount<Length> accessibleForagingRadius = Amount.valueOf(1,
	    UnitConstants.WORLD_DISTANCE);
    /** Which food the species can feed on. */
    private FeedingGuild feedingGuild = FeedingGuild.HERBIVORE;

    // DEATH
    /**
     * Random mortality risk.
     * 
     * @see "McIlwain 2009"
     */
    private Amount<Frequency> mortalityRisk = Amount.valueOf(0.519,
	    UnitConstants.PER_YEAR);
    /**
     * Maximum age {@link Duration}
     * 
     * @see "El-Sayed Ali et al. 2011"
     */
    private Amount<Duration> maxAge = Amount.valueOf(18.75, YEAR).to(
	    UnitConstants.MAX_AGE);

    // REPRODUCTION
    /**
     * Probability of female sex when creating fish. Only relevant if
     * {@link SexChangeMode#NONE} set in their {@link SpeciesDefinition}.
     */
    private static final double FEMALE_PROBABILITY = 0.5;
    /** Number of offsprings per reproduction cycle */
    // TODO arbitrary value. get correct one.
    private int numOffspring = 1;
    /** @see SexChangeMode */
    private SexChangeMode sexChangeMode = SexChangeMode.PROTOGYNOUS;

    // GROWTH
    /** Default initial age for fish when entering the simulation. */
    // same unit as step duration to keep amount exact
    private static final Amount<Duration> INITIAL_AGE = Amount
            .valueOf(120, DAY)
            .to(EnvironmentDefinition.STEP_DURATION.getUnit());
    /**
     * Length when fish stops being juvenile and may obtain the ability to
     * reproduce.
     * 
     * @see Phase
     */
    private Amount<Length> initialPhaseLength = Amount
	    .valueOf(12.5, CENTIMETER).to(UnitConstants.BODY_LENGTH);
    /**
     * Length when sex change may occur if {@link SexChangeMode#PROTANDROUS} or
     * {@link SexChangeMode#PROTOGYNOUS}.
     */
    private Amount<Length> terminalPhaseLength = Amount.valueOf(17, CENTIMETER)
	    .to(UnitConstants.BODY_LENGTH);

    /**
     * Coefficient in length-mass relationship.
     * 
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private Amount<Mass> lengthMassCoeff = Amount.valueOf(0.0319, GRAM).to(
	    UnitConstants.BIOMASS);
    /**
     * Degree in length-mass relationship.
     * 
     * @see "El-Sayed Ali et al. 2011"
     * @see FormulaUtil#expectedMass(Amount, Amount, double)
     */
    private double lengthMassDegree = 2.928;
    /**
     * Length of fish at birth. <b>Not</b> at simulation start.
     * 
     * @see #INITIAL_AGE
     */
    private Amount<Length> birthLength = Amount.valueOf(6.7, CENTIMETER).to(
	    UnitConstants.BODY_LENGTH);
    /** Length that the fish will grow during its lifetime */
    private Amount<Length> maxLength = Amount.valueOf(32.4, CENTIMETER).to(
	    UnitConstants.BODY_LENGTH);
    /**
     * Growth coefficient in length-age relationship.
     * 
     * @see FormulaUtil#expectedLength(Amount, double, Amount, Amount)
     */
    private double growthCoeff = 0.15;
    private double computeMaxConsumptionRatePerStep() {
	return maxConsumptionRate.times(EnvironmentDefinition.STEP_DURATION)
		.to(Unit.ONE).getEstimatedValue();
    }

    public int getInitialNum() {
	return initialNum;
    }

    public String getSpeciesName() {
	return speciesName;
    }

    public Amount<Velocity> obtainSpeed(BehaviorMode behaviorMode) {
	return behaviorMode == BehaviorMode.FORAGING ? speedForaging
		: speedResting;
    }

    public double getSpeedDeviation() {
	return speedDeviation;
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

    public Habitat getRestingHabitat() {
        return RESTING_HABITAT;
    }

    public Habitat getForagingHabitat() {
        return FORAGING_HABITAT;
    }

    public Habitat getSpawnHabitat() {
        return SPAWN_HABITAT;
    }

    public Amount<Frequency> getMaxConsumptionRate() {
	return maxConsumptionRate;
    }

    /**
     * Consumption per gram biomass in one step. The value is dimensionless
     * because {@code g dry weight / g biomass = 1}.
     * 
     * @return Consumption per step in g dry weight / g biomass
     */
    public double getMaxConsumptionPerStep() {
	return maxConsumptionPerStep;
    }

    public Amount<SpecificEnergy> getEnergyContentFood() {
	return energyContentFood;
    }

    public Amount<Duration> getGutTransitDuration() {
	return gutTransitDuration;
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
	    throw new IllegalArgumentException("No length for next phase when "
		    + currentPhase);
	}
    }

    public double getLossFactorDigestion() {
	return lossFactorDigestion;
    }

    public Amount<Length> getAccessibleForagingRadius() {
	return accessibleForagingRadius;
    }

    public FeedingGuild getFeedingGuild() {
	return feedingGuild;
    }

    public static Amount<Duration> getInitialAge() {
        return INITIAL_AGE;
    }

    public Amount<Mass> getLengthMassCoeff() {
	return lengthMassCoeff;
    }

    public double getLengthMassDegree() {
	return lengthMassDegree;
    }

    public Amount<Length> getBirthLength() {
	return birthLength;
    }

    public Amount<Length> getMaxLength() {
	return maxLength;
    }

    public double getGrowthCoeff() {
	return growthCoeff;
    }

    public SexChangeMode getSexChangeMode() {
	return sexChangeMode;
    }

    /**
     * @return {@code true} when species changes sex over lifetime
     * @see SexChangeMode
     */
    public boolean canChangeSex() {
	return sexChangeMode == SexChangeMode.PROTANDROUS
		|| sexChangeMode == SexChangeMode.PROTOGYNOUS;
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);
	maxConsumptionPerStep = computeMaxConsumptionRatePerStep();
    }

    @Override
    public String getTitle() {
	return speciesName;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
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

	public String getSpeedForaging() {
	    return speedForaging.toString();
	}

	public void setSpeedForaging(String speedForagingString) {
	    SpeciesDefinition.this.speedForaging = AmountUtil.parseAmount(
		    speedForagingString, UnitConstants.VELOCITY);
	}

	public String getSpeedResting() {
	    return speedResting.toString();
	}

	public void setSpeedResting(String speedRestingString) {
	    SpeciesDefinition.this.speedResting = AmountUtil.parseAmount(
		    speedRestingString, UnitConstants.VELOCITY);
	}

	public double getSpeedDeviation() {
	    return speedDeviation;
	}

	public void setSpeedDeviation(double speedDeviation) {
	    SpeciesDefinition.this.speedDeviation = Math.max(0, speedDeviation);
	}

	public Object domSpeedDeviation() {
	    return new Interval(0d, 1d);
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
	    SpeciesDefinition.this.perceptionRadius = AmountUtil.parseAmount(
		    perceptionRangeString, UnitConstants.WORLD_DISTANCE);
	}

	public String getInitialAge() {
	    return SpeciesDefinition.INITIAL_AGE.to(UnitConstants.AGE)
		    .toString();
	}

	public String getMaxConsumptionRate() {
	    return maxConsumptionRate.toString();
	}

	public void setMaxConsumptionRate(String consumptionRateString) {
	    // unit: g dry weight / g biomass = 1
	    SpeciesDefinition.this.maxConsumptionRate = AmountUtil.parseAmount(
		    consumptionRateString, UnitConstants.PER_HOUR);
	    maxConsumptionPerStep = computeMaxConsumptionRatePerStep();
	}

	public String getEnergyContentFood() {
	    return energyContentFood.toString();
	}

	public void setEnergyContentFood(String energyDensityFoodString) {
	    SpeciesDefinition.this.energyContentFood = AmountUtil.parseAmount(
		    energyDensityFoodString, UnitConstants.ENERGY_CONTENT_FOOD);
	}

	public String getGutTransitDuration() {
	    return gutTransitDuration.toString();
	}

	public void setGutTransitDuration(String gutTransitDurationString) {
	    SpeciesDefinition.this.gutTransitDuration = AmountUtil.parseAmount(
		    gutTransitDurationString, UnitConstants.SIMULATION_TIME);
	}

	public double getMortalityRisk() {
	    return mortalityRisk.doubleValue(UnitConstants.PER_YEAR);
	}

	public void setMortalityRisk(double mortalityRisk) {
	    SpeciesDefinition.this.mortalityRisk = Amount.valueOf(
		    mortalityRisk, UnitConstants.PER_YEAR);
	}

	public String nameMortalityRisk() {
	    return "mortalityRisk_" + UnitConstants.PER_YEAR;
	}

	public Object domMortalityRisk() {
	    return new Interval(0d, 1d);
	}

	public String getMaxAge() {
	    return maxAge.to(UnitConstants.MAX_AGE).toString();
	}

	public void setMaxAge(String maxAgeString) {
	    maxAge = AmountUtil
		    .parseAmount(maxAgeString, UnitConstants.MAX_AGE);
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
	    SpeciesDefinition.this.initialPhaseLength = AmountUtil.parseAmount(
		    initialPhaseLengthString, UnitConstants.BODY_LENGTH);
	}

	public String getTerminalPhaseLength() {
	    return terminalPhaseLength.toString();
	}

	public void setTerminalPhaseLength(String terminalPhaseLengthString) {
	    SpeciesDefinition.this.terminalPhaseLength = AmountUtil
		    .parseAmount(terminalPhaseLengthString,
			    UnitConstants.BODY_LENGTH);
	}

	public double getLossFactorDigestion() {
	    return lossFactorDigestion;
	}

	public void setLossFactorDigestion(double netEnergy) {
	    SpeciesDefinition.this.lossFactorDigestion = netEnergy;
	}

	public String getAccessibleForagingRadius() {
	    return accessibleForagingRadius.toString();
	}

	public void setAccessibleForagingRadius(
		String accessibleForagingRadiusString) {
	    SpeciesDefinition.this.accessibleForagingRadius = AmountUtil
		    .parseAmount(accessibleForagingRadiusString,
			    UnitConstants.WORLD_DISTANCE);
	}

	public int getFeedingGuild() {
	    return feedingGuild.ordinal();
	}

	public void setFeedingGuild(int feedingGuildOrdinal) {
	    SpeciesDefinition.this.feedingGuild = FeedingGuild.values()[feedingGuildOrdinal];
	    logger.warning("Feeding guild not yet implemented.");
	}

	public String[] domFeedingGuild() {
	    return ParamsUtil.obtainEnumDomain(FeedingGuild.class);
	}

	public String getLengthMassCoeff() {
	    return lengthMassCoeff.toString();
	}

	public void setLengthMassCoeff(String lengthMassCoeffString) {
	    SpeciesDefinition.this.lengthMassCoeff = AmountUtil.parseAmount(
		    lengthMassCoeffString, UnitConstants.BIOMASS);
	}

	public double getLengthMassDegree() {
	    return lengthMassDegree;
	}

	public void setLengthMassDegree(double lengthMassDegree) {
	    SpeciesDefinition.this.lengthMassDegree = lengthMassDegree;
	}

	public String getBirthLength() {
	    return birthLength.toString();
	}

	public void setBirthLength(String birthLengthString) {
	    SpeciesDefinition.this.birthLength = AmountUtil.parseAmount(
		    birthLengthString, UnitConstants.BODY_LENGTH);
	}

	public String getMaxLength() {
	    return maxLength.toString();
	}

	public void setMaxLength(String growthLengthString) {
	    SpeciesDefinition.this.maxLength = AmountUtil.parseAmount(
		    growthLengthString, UnitConstants.BODY_LENGTH);
	}

	public double getGrowthCoeff() {
	    return growthCoeff;
	}

	public void setGrowthCoeff(double growthCoeff) {
	    SpeciesDefinition.this.growthCoeff = growthCoeff;
	}

	public String getMaxAttractionDistance() {
	    return maxAttractionDistance.toString();
	}

	public void setMaxAttractionDistance(String maxAttractionDistanceString) {
	    SpeciesDefinition.this.maxAttractionDistance = AmountUtil
		    .parseAmount(maxAttractionDistanceString,
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

	public Habitat getRestingHabitat() {
	    return RESTING_HABITAT;
	}

	public Habitat getForagingHabitat() {
	    return FORAGING_HABITAT;
	}

	public Habitat getSpawnHabitat() {
	    return SPAWN_HABITAT;
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
     * @author cmeyer
     * 
     */
    public static enum SexChangeMode {
	/**
	 * Does not change sex during life time. Gets mature when entering the
	 * initial phase. The terminal phase will not be entered.
	 */
	NONE,
	/**
	 * Starting as male when entering the initial phase, turns out as
	 * females in the terminal phase.
	 */
	PROTANDROUS,
	/** Female when entering the initial phase, male in terminal phase. */
	PROTOGYNOUS
    }

    /**
     * Species that feed on similar resources share the same feeding guild.
     * 
     * @author cmeyer
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
    }

    /**
     * Move mode for this species.
     * 
     * @see MoveSystem
     * @author cmeyer
     *
     */
    public static enum MoveMode {
	/** Pure random walk */
	RANDOM,
	/**
	 * Moves towards areas with the highest food supply in
	 * perception range.
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
    
    // TODO implement
    public static enum ActivityType {
	/** Active at daytime. */
	DIURNAL,
	/** Active at nighttime. */
	NOCTURNAL
    }
}