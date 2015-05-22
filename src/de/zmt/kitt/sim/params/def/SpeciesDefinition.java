package de.zmt.kitt.sim.params.def;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;
import javax.xml.bind.annotation.*;

import org.jscience.physics.amount.Amount;

import sim.util.*;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.SpecificEnergy;
import de.zmt.sim.engine.params.def.*;
import de.zmt.sim.util.ParamUtil;
import ecs.Component;

/**
 * Parameters for defining a species.
 * 
 * @author cmeyer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition extends AbstractParamDefinition implements
	OptionalParamDefinition, Proxiable, Component {
    private static final long serialVersionUID = 1L;

    /** Initial age for fish when entering the simulation */
    // same unit as step duration to keep amount exact
    private static final Amount<Duration> INITIAL_AGE = Amount
	    .valueOf(120, DAY)
	    .to(EnvironmentDefinition.STEP_DURATION.getUnit());

    // FEED
    /** how many individuals should be put at the beginning of the simulation */
    private int initialNum = 1;
    /** name of species */
    @XmlAttribute
    private String speciesName = "Chlorurus sordidus";

    // MOVEMENT
    /** Travel speed factor on fish size in m/s while foraging. */
    @XmlElement
    private Amount<Velocity> speedForaging = Amount.valueOf(0.2,
	    METERS_PER_SECOND).to(UnitConstants.VELOCITY);
    /** Travel speed factor on fish size in m/s while resting. */
    private Amount<Velocity> speedResting = Amount.valueOf(0.05,
	    METERS_PER_SECOND).to(UnitConstants.VELOCITY);
    /** Standard deviation of travel speed as a fraction. */
    private double speedDeviation = 0.2;
    /** Fish is attracted towards foraging / resting center */
    private boolean attractionEnabled = false;

    /**
     * Maximum amount of food the fish can consume per biomass within a time
     * span:<br>
     * {@code g dry weight / g biomass / h}.
     */
    // TODO arbitrary value. get real one.
    private Amount<Frequency> maxConsumptionRate = Amount.valueOf(0.5,
	    UnitConstants.PER_HOUR);
    /** @see #consumptionRate */
    @XmlTransient
    private double maxConsumptionPerStep;

    /**
     * energy content of food (kJ/g dry weight food)<br>
     * Bruggemann et al. 1994
     */
    private Amount<SpecificEnergy> energyContentFood = Amount.valueOf(17.5,
	    UnitConstants.ENERGY_CONTENT_FOOD);
    /**
     * food transit time through gut in minutes<br>
     * Polunin et al. 1995
     */
    private Amount<Duration> gutTransitDuration = Amount.valueOf(54, MINUTE)
	    .to(UnitConstants.SIMULATION_TIME);

    // DEATH
    /** McIlwain 2009 */
    private Amount<Frequency> mortalityRisk = Amount.valueOf(0.519,
	    UnitConstants.PER_YEAR);
    /**
     * Maximum age {@link Duration}<br>
     * El-Sayed Ali et al. 2011
     */
    private Amount<Duration> maxAge = Amount.valueOf(18.75, YEAR).to(
	    UnitConstants.MAX_AGE);
    /**
     * Energy remaining after digestion including loss due to assimilation,
     * digestion, excretion, specific dynamic actions.
     */
    private double lossFactorDigestion = 0.43;

    // REPRODUCTION
    /** Number of offsprings per reproduction cycle */
    private int numOffspring = 1;
    /**
     * Length when fish stops being
     * {@link de.zmt.kitt.sim.engine.agent.fish.LifeStage#JUVENILE} and may
     * obtain the ability to reproduce.
     */
    private Amount<Length> adultLength = Amount.valueOf(12.34, CENTIMETER).to(
	    UnitConstants.BODY_LENGTH);

    private Amount<Mass> lengthMassCoeff = Amount.valueOf(0.0319, GRAM).to(
	    UnitConstants.BIOMASS);
    /**
     * Coefficient defining slope in length-weight relationship.<br>
     * {@code W(g wet weight)=A*L(SL in cm)^B}
     * <p>
     * El-Sayed Ali et al. 2011
     */
    private double lengthMassExponent = 2.928;
    /** Length of fish at birth */
    private Amount<Length> birthLength = Amount.valueOf(6.7, CENTIMETER).to(
	    UnitConstants.BODY_LENGTH);
    /** Length that the fish will grow during its lifetime */
    private Amount<Length> maxLength = Amount.valueOf(32.4, CENTIMETER).to(
	    UnitConstants.BODY_LENGTH);
    /** growth coefficient K */
    private double growthCoeff = 0.15;
    /** Distance of full bias towards attraction center in m */
    private Amount<Length> maxAttractionDistance = Amount.valueOf(150, METER)
	    .to(UnitConstants.MAP_DISTANCE);

    private SexChangeMode sexChangeMode = SexChangeMode.NONE;

    public int getInitialNum() {
	return initialNum;
    }

    public String getSpeciesName() {
	return speciesName;
    }

    public Amount<Velocity> getSpeedForaging() {
	return speedForaging;
    }

    public Amount<Velocity> getSpeedResting() {
	return speedResting;
    }

    public double getSpeedDeviation() {
	return speedDeviation;
    }

    public boolean isAttractionEnabled() {
	return attractionEnabled;
    }

    public static Amount<Duration> getInitialAge() {
	return INITIAL_AGE;
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

    public int getNumOffspring() {
	return numOffspring;
    }

    public Amount<Length> getAdultLength() {
	return adultLength;
    }

    public double getLossFactorDigestion() {
	return lossFactorDigestion;
    }

    public Amount<Mass> getLengthMassCoeff() {
	return lengthMassCoeff;
    }

    public double getLengthMassExponent() {
	return lengthMassExponent;
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

    public Amount<Length> getMaxAttractionDistance() {
	return maxAttractionDistance;
    }

    public SexChangeMode getSexChangeMode() {
	return sexChangeMode;
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

	public void setSpeedForaging(String speedForaging) {
	    SpeciesDefinition.this.speedForaging = AmountUtil.parseAmount(
		    speedForaging, UnitConstants.VELOCITY);
	}

	public String getSpeedResting() {
	    return speedResting.toString();
	}

	public void setSpeedResting(String speedResting) {
	    SpeciesDefinition.this.speedResting = AmountUtil.parseAmount(
		    speedResting, UnitConstants.VELOCITY);
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

	public boolean isAttractionEnabled() {
	    return attractionEnabled;
	}

	public void setAttractionEnabled(boolean enableAttraction) {
	    SpeciesDefinition.this.attractionEnabled = enableAttraction;
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
	    maxConsumptionPerStep = maxConsumptionRate
		    .times(EnvironmentDefinition.STEP_DURATION).to(Unit.ONE)
		    .getEstimatedValue();
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

	public String getAdultLength() {
	    return adultLength.toString();
	}

	public void setAdultLength(String adultLengthString) {
	    SpeciesDefinition.this.adultLength = AmountUtil.parseAmount(
		    adultLengthString, UnitConstants.BODY_LENGTH);
	}

	public double getLossFactorDigestion() {
	    return lossFactorDigestion;
	}

	public void setLossFactorDigestion(double netEnergy) {
	    SpeciesDefinition.this.lossFactorDigestion = netEnergy;
	}

	public String getLengthMassCoeff() {
	    return lengthMassCoeff.toString();
	}

	public void setLengthMassCoeff(String lengthMassCoeffString) {
	    SpeciesDefinition.this.lengthMassCoeff = AmountUtil.parseAmount(
		    lengthMassCoeffString, UnitConstants.BIOMASS);
	}

	public double getLengthMassExponent() {
	    return lengthMassExponent;
	}

	public void setLengthMassExponent(double lengthMassCoeff) {
	    SpeciesDefinition.this.lengthMassExponent = lengthMassCoeff;
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
			    UnitConstants.MAP_DISTANCE);
	}

	public int getSexChangeMode() {
	    return sexChangeMode.ordinal();
	}

	public void setSexChangeMode(int sexChangeModeOrdinal) {
	    SpeciesDefinition.this.sexChangeMode = SexChangeMode.values()[sexChangeModeOrdinal];
	}

	public String[] domSexChangeMode() {
	    return ParamUtil.obtainEnumDomain(SexChangeMode.class);
	}
    }

    public static enum SexChangeMode {
	/** Does not change sex during life time */
	NONE,
	/** Changes from male to female */
	PROTANDROUS,
	/** Changes from female to male */
	PROTOGYNOUS
    }
}