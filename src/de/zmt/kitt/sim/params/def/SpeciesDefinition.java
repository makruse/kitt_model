package de.zmt.kitt.sim.params.def;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.engine.params.def.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.EnergyDensity;

/**
 * Parameters for defining a species.
 * 
 * @author cmeyer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition extends AbstractParameterDefinition implements
	OptionalParameterDefinition, ProvidesInspector, Proxiable {
    /** Initial age for fish when entering the simulation */
    // same unit as step duration to keep amount exact
    private static final Amount<Duration> INITIAL_AGE = Amount
	    .valueOf(120, DAY)
	    .to(EnvironmentDefinition.STEP_DURATION.getUnit());
    private static Unit<Duration> AGE_DISPLAY_UNIT = YEAR;

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
	    METERS_PER_SECOND).to(AmountUtil.VELOCITY_UNIT);
    /** Travel speed factor on fish size in m/s while resting. */
    private Amount<Velocity> speedResting = Amount.valueOf(0.05,
	    METERS_PER_SECOND).to(AmountUtil.VELOCITY_UNIT);
    /** Standard deviation of travel speed as a fraction. */
    private double speedDeviation = 0.2;
    /** Fish is attracted towards foraging / resting center */
    private boolean attractionEnabled = false;

    /**
     * Maximum amount of food the fish can consume per biomass within a time
     * span:<br>
     * {@code g dry weight / g biomass / s}.
     */
    // TODO arbitrary value. get real one.
    private Amount<Frequency> maxConsumptionRate = Amount.valueOf(0.1,
	    AmountUtil.PER_HOUR);
    /** @see #consumptionRate */
    @XmlTransient
    private double maxConsumptionPerStep;

    /**
     * energy content of food (kJ/g dry weight food)<br>
     * Bruggemann et al. 1994
     */
    private Amount<EnergyDensity> energyDensityFood = Amount.valueOf(17.5,
	    AmountUtil.ENERGY_DENSITY_UNIT);
    /**
     * food transit time through gut in minutes<br>
     * Polunin et al. 1995
     */
    private Amount<Duration> gutTransitDuration = Amount.valueOf(54, MINUTE)
	    .to(AmountUtil.DURATION_UNIT);

    // DEATH
    /** McIlwain 2009 */
    private Amount<Frequency> mortalityRisk = Amount.valueOf(0.519,
	    AmountUtil.PER_YEAR);
    /**
     * Maximum age {@link Duration}<br>
     * El-Sayed Ali et al. 2011
     */
    private Amount<Duration> maxAge = Amount.valueOf(18.75, YEAR).to(
	    AmountUtil.DURATION_UNIT);
    /**
     * Loss factor applied on raw food energy during digestion including
     * subtraction of assimilation, digestion, excretion, specific dynamic
     * actions.
     * <p>
     * for herbivores (Brett & Groves 1979) estimation of size-at-age with
     * vonBertalanffyGrowthFunction (vBGF) parameters of the vBGF to calculate
     * length at age t: <br>
     * L(t)= L*( 1- e^(-K*(t-t(0)))
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
	    AmountUtil.SHORT_LENGTH_UNIT);

    private Amount<Mass> lengthMassCoeff = Amount.valueOf(0.0319, GRAM).to(
	    AmountUtil.MASS_UNIT);
    /**
     * Coefficient defining slope in length-weight relationship.<br>
     * {@code W(g wet weight)=A*L(SL in cm)^B}
     * <p>
     * El-Sayed Ali et al. 2011
     */
    private double lengthMassExponent = 2.928;
    /** Length of fish at birth */
    private Amount<Length> birthLength = Amount.valueOf(6.7, CENTIMETER).to(
	    AmountUtil.SHORT_LENGTH_UNIT);
    /** Length that the fish will grow during its lifetime */
    private Amount<Length> growthLength = Amount.valueOf(32.4, CENTIMETER).to(
	    AmountUtil.SHORT_LENGTH_UNIT);
    /** growth coefficient K */
    private double growthCoeff = 0.15;

    // DERIVED VALUES - not set by the user
    @XmlTransient
    private Amount<Length> initialLength; //
    @XmlTransient
    private Amount<Mass> initialBiomass;
    /** Curve of expected energy at age steps */

    @XmlTransient
    private SimpleInspector inspector;

    public SpeciesDefinition() {
	computeDerivedValues();
    }

    private void computeDerivedValues() {
	computeInitialLength();
	computeInitialBiomass();
	computeMaxConsumptionPerStep();
    }

    /**
     * Compute initial length from {@link #INITIAL_AGE}.
     */
    private void computeInitialLength() {
	if (isUnmarshalling()) {
	    return;
	}

	initialLength = FormulaUtil.expectedLength(growthLength, growthCoeff,
		INITIAL_AGE, birthLength);

	if (inspector != null) {
	    inspector.updateInspector();
	}
    }

    /**
     * Compute initial biomass from initial length.
     */
    private void computeInitialBiomass() {
	if (isUnmarshalling()) {
	    return;
	}

	initialBiomass = FormulaUtil.expectedMass(lengthMassCoeff,
		initialLength, lengthMassExponent);

	if (inspector != null) {
	    inspector.updateInspector();
	}
    }

    private void computeMaxConsumptionPerStep() {
	maxConsumptionPerStep = maxConsumptionRate
		.times(EnvironmentDefinition.STEP_DURATION).to(Unit.ONE)
		.getEstimatedValue();
    }

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

    public Amount<Length> getInitialLength() {
	return initialLength;
    }

    public Amount<Mass> getInitialBiomass() {
	return initialBiomass;
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

    public Amount<?> getEnergyDensityFood() {
	return energyDensityFood;
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

    public Amount<Length> getGrowthLength() {
	return growthLength;
    }

    public double getGrowthCoeff() {
	return growthCoeff;
    }

    @Override
    public String getTitle() {
	return "Species:" + speciesName;
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);
	computeDerivedValues();
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	// return simple inspector that we can update
	inspector = new SimpleInspector(this, state, name);
	return inspector;
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
	    SpeciesDefinition.this.speedForaging = AmountUtil
		    .parseVelocity(speedForaging);
	}

	public String getSpeedResting() {
	    return speedResting.toString();
	}

	public void setSpeedResting(String speedResting) {
	    SpeciesDefinition.this.speedResting = AmountUtil
		    .parseVelocity(speedResting);
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
	    return SpeciesDefinition.INITIAL_AGE.to(AGE_DISPLAY_UNIT)
		    .toString();
	}

	public String getInitialLength() {
	    return initialLength.toString();
	}

	public String getInitialBiomass() {
	    return initialBiomass.toString();
	}

	public String getMaxConsumptionRate() {
	    return maxConsumptionRate.toString();
	}

	public void setMaxConsumptionRate(String consumptionRateString) {
	    SpeciesDefinition.this.maxConsumptionRate = AmountUtil.parseAmount(
		    consumptionRateString, AmountUtil.PER_DAY);
	    // unit: g dry weight / g biomass = 1
	    computeMaxConsumptionPerStep();
	}

	public String getEnergyContentFood() {
	    return energyDensityFood.toString();
	}

	public void setEnergyContentFood(String energyDensityFoodString) {
	    SpeciesDefinition.this.energyDensityFood = AmountUtil
		    .parseEnergyDensity(energyDensityFoodString);
	}

	public String getGutTransitDuration() {
	    return gutTransitDuration.toString();
	}

	public void setGutTransitDuration(String gutTransitDurationString) {
	    SpeciesDefinition.this.gutTransitDuration = AmountUtil
		    .parseDuration(gutTransitDurationString);
	}

	public double getMortalityRisk() {
	    return mortalityRisk.doubleValue(AmountUtil.PER_YEAR);
	}

	public void setMortalityRisk(double mortalityRisk) {
	    SpeciesDefinition.this.mortalityRisk = Amount.valueOf(
		    mortalityRisk, AmountUtil.PER_YEAR);
	}

	public String nameMortalityRisk() {
	    return "mortalityRisk_" + AmountUtil.PER_YEAR;
	}

	public Object domMortalityRisk() {
	    return new Interval(0d, 1d);
	}

	public String getMaxAge() {
	    return maxAge.to(AGE_DISPLAY_UNIT).toString();
	}

	public void setMaxAge(String maxAgeString) {
	    maxAge = AmountUtil.parseDuration(maxAgeString);
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
	    SpeciesDefinition.this.adultLength = AmountUtil
		    .parseShortLength(adultLengthString);
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
	    SpeciesDefinition.this.lengthMassCoeff = AmountUtil
		    .parseMass(lengthMassCoeffString);
	}

	public double getLengthMassExponent() {
	    return lengthMassExponent;
	}

	public void setLengthMassExponent(double lengthMassCoeff) {
	    SpeciesDefinition.this.lengthMassExponent = lengthMassCoeff;
	    computeInitialBiomass();
	}

	public String getBirthLength() {
	    return birthLength.toString();
	}

	public void setBirthLength(String birthLengthString) {
	    SpeciesDefinition.this.birthLength = AmountUtil
		    .parseShortLength(birthLengthString);
	}

	public String getGrowthLength() {
	    return growthLength.toString();
	}

	public void setGrowthLength(String growthLengthString) {
	    SpeciesDefinition.this.growthLength = AmountUtil
		    .parseShortLength(growthLengthString);
	}

	public double getGrowthCoeff() {
	    return growthCoeff;
	}

	public void setGrowthCoeff(double growthCoeff) {
	    SpeciesDefinition.this.growthCoeff = growthCoeff;
	    computeDerivedValues();
	}

    }
}