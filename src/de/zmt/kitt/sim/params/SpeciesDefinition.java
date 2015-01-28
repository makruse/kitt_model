package de.zmt.kitt.sim.params;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import sim.display.GUIState;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import de.zmt.sim_base.engine.params.ParameterDefinition;
import flanagan.interpolation.CubicSpline;

/**
 * holds the initial parameters and general properties to define a species.<br />
 * in the simulation of this kind are instantiated holding the following values
 * for the properties By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD)
 * its defined <br />
 * that all fields that are not marked as transient are written to xml file.
 * 
 * */

@XmlAccessorType(XmlAccessType.PROPERTY)
public class SpeciesDefinition extends ParameterDefinition implements
	ProvidesInspector {
    /** post-settlement age in years */
    public static final double INITIAL_AGE_YEARS = 0.33; // approx. 120 days
    /** Maximum age represented the growth curve */
    private static final double MAXIMUM_AGE = 20.0;
    /** Wet weight in g */
    private static final double WET_WEIGHT = 6.5;

    /** how many individuals should be put at the beginning of the simulation */
    private int initialNum = 1;

    // MOVEMENT
    /** Travel speed in meters per minute during day. */
    private double daySpeed = 3.5;
    /** Travel speed in meters per minute during night. */
    private double nightSpeed = 1;
    /** Standard deviation of travel speed in meters per minute */
    private double speedDeviation = 0.5;
    /** Fish is attracted towards foraging / resting center */
    private boolean attractionEnabled = false;

    // FEED
    /** daily food consumption rate (g food dry weight/g fish wet weight/day) */
    private double consumptionRate = 0.236; // Polunin et al. 1995
    /** energy content of food (kJ/g dry weight food */
    private double energyContentFood = 17.5; // nach Bruggemann et al. 1994
    /**
     * max daily food ration<br>
     * (g algal DW)=0.019*biomass+3.294 nach Brugemann et al. 1994
     */
    private double maxDailyFoodRationA = 0.019;
    /**
     * max daily food ration<br>
     * (g algal DW)=0.019*biomass+3.294 nach Brugemann et al. 1994
     */
    private double maxDailyFoodRationB = 3.294;
    /**
     * food transit time through gut in min<br>
     * Polunin et al. 1995
     */
    private double gutTransitTime = 54;

    // DEATH
    /** McIlwain 2009 */
    private double mortalityRatePerYears = 0.519;
    /** El-Sayed Ali et al. 2011 */
    private double maxAgeInYrs = 18.75;
    /**
     * usable percentage of digested energy after subtraction of assimilation,
     * digestion, excretion, specific dynamic actions
     * <p>
     * for herbivores (Brett & Groves 1979) estimation of size-at-age with
     * vonBertalanffyGrowthFunction (vBGF) parameters of the vBGF to calculate
     * length at age t: <br>
     * L(t)= L*( 1- e^(-K*(t-t(0)))
     */
    private double netEnergy = 0.43;

    // REPRODUCTION
    /** Number of offsprings per reproduction cycle */
    private int numOffspring = 1;

    // GROWTH
    /**
     * length-weight relationsship: W(g wet weight)=A*L(SL in cm)^B
     * <p>
     * El-Sayed Ali et al. 2011
     */
    private double lengthMassCoeffA = 0.0309;
    /** El-Sayed Ali et al. 2011 */
    private double lengthMassCoeffB = 2.935;
    /** name of species */
    private String speciesName = "Chlorurus sordidus";
    /** asymptotic length L */
    private double asymLengthL = 39.1;
    /** growth coefficient K */
    private double growthCoeffK = 0.15;
    /** theoretical age at zero size */
    private double ageAtTimeZero = -1.25;
    private int numPointsGrowthCurve = 50;

    // DERIVED VALUES - not set by the user
    /** initial size when born in cm (vBGF) */
    @XmlTransient
    private double initialSize; //
    /** intial biomass when born (g wet weight) based on intialSize */
    @XmlTransient
    private double initialBiomass;
    /** Curve of expected energy at age steps */
    @XmlTransient
    private CubicSpline expectedEnergyWithoutRepro; // in kJ

    private SimpleInspector inspector;

    private void updateExpectedEnergyWithoutRepro() {
	if (!isInitialized()) {
	    return;
	}

	double[] expectedEnergyAtAgeSteps = new double[numPointsGrowthCurve];
	// holds the age coordinates?? at which the fish size is precalculated
	double[] ageSteps = new double[numPointsGrowthCurve];
	for (int i = 0; i < numPointsGrowthCurve; i++) {
	    double age = MAXIMUM_AGE / numPointsGrowthCurve * i;
	    ageSteps[i] = age;

	    // vBGF: L(t)= L*( 1- e^(-K*(t-t(0)))
	    double expectedSizeAtAge = asymLengthL
		    * (1 - Math.pow(Math.E, -growthCoeffK
			    * (age - ageAtTimeZero)));

	    // length mass relationship: W=a*L^b
	    double biomass = lengthMassCoeffA
		    * Math.pow(expectedSizeAtAge, lengthMassCoeffB);

	    // 1 g fish wet weight = 6.5 kJ (only valid without repro!)
	    double energyWithoutRepro = biomass * WET_WEIGHT;
	    expectedEnergyAtAgeSteps[i] = energyWithoutRepro;
	}
	expectedEnergyWithoutRepro = new CubicSpline(ageSteps,
		expectedEnergyAtAgeSteps);
    }

    private void updateInitialSize() {
	if (!isInitialized()) {
	    return;
	}

	// to initialise size: take post-settlement age of ca. 120
	// days=0.33
	// yrs?? sp�ter anders wenn realistische population!
	// size initialized �ber vBGF at given initialAgeInYrs
	initialSize = Math.abs(asymLengthL
		* (1 - Math.pow(Math.E, -growthCoeffK
			* (INITIAL_AGE_YEARS - ageAtTimeZero))));

	if (inspector != null) {
	    inspector.updateInspector();
	}
    }

    private void updateInitialBiomass() {
	if (!isInitialized()) {
	    return;
	}

	// biomass initialized �ber weight-length-relationship at
	// calculated
	// initialSize
	initialBiomass = lengthMassCoeffA
		* Math.pow(initialSize, lengthMassCoeffB);

	if (inspector != null) {
	    inspector.updateInspector();
	}
    }

    public int getInitialNum() {
	return initialNum;
    }

    public void setInitialNum(int initialNum) {
	this.initialNum = initialNum;
    }

    public double getDaySpeed() {
	return daySpeed;
    }

    public void setDaySpeed(double migrationSpeed) {
	this.daySpeed = Math.max(0, migrationSpeed);
    }

    public double getNightSpeed() {
	return nightSpeed;
    }

    public void setNightSpeed(double restSpeed) {
	this.nightSpeed = Math.max(0, restSpeed);
    }

    public double getSpeedDeviation() {
	return speedDeviation;
    }

    public void setSpeedDeviation(double speedDeviation) {
	this.speedDeviation = Math.max(0, speedDeviation);
    }

    public boolean isAttractionEnabled() {
	return attractionEnabled;
    }

    public void setAttractionEnabled(boolean enableAttraction) {
	this.attractionEnabled = enableAttraction;
    }

    public double getInitialSize() {
	return initialSize;
    }

    public double getInitialBiomass() {
	return initialBiomass;
    }

    public double getConsumptionRate() {
	return consumptionRate;
    }

    public void setConsumptionRate(double consumptionRate) {
	this.consumptionRate = consumptionRate;
    }

    public double getEnergyContentFood() {
	return energyContentFood;
    }

    public void setEnergyContentFood(double energyContentFood) {
	this.energyContentFood = energyContentFood;
    }

    public double getMaxDailyFoodRationA() {
	return maxDailyFoodRationA;
    }

    public void setMaxDailyFoodRationA(double maxDailyFoodRationA) {
	this.maxDailyFoodRationA = maxDailyFoodRationA;
    }

    public double getMaxDailyFoodRationB() {
	return maxDailyFoodRationB;
    }

    public void setMaxDailyFoodRationB(double maxDailyFoodRationB) {
	this.maxDailyFoodRationB = maxDailyFoodRationB;
    }

    public double getGutTransitTime() {
	return gutTransitTime;
    }

    public void setGutTransitTime(double gutTransitTime) {
	this.gutTransitTime = gutTransitTime;
    }

    public double getMortalityRatePerYears() {
	return mortalityRatePerYears;
    }

    public void setMortalityRatePerYears(double mortalityRatePerYears) {
	this.mortalityRatePerYears = mortalityRatePerYears;
    }

    public double getMaxAgeInYrs() {
	return maxAgeInYrs;
    }

    public void setMaxAgeInYrs(double maxAgeInYrs) {
	this.maxAgeInYrs = maxAgeInYrs;
    }

    public int getNumOffspring() {
	return numOffspring;
    }

    public void setNumOffspring(int numOffspring) {
	this.numOffspring = numOffspring;
    }

    public double getNetEnergy() {
	return netEnergy;
    }

    public void setNetEnergy(double netEnergy) {
	this.netEnergy = netEnergy;
    }

    public double getLengthMassCoeffA() {
	return lengthMassCoeffA;
    }

    public void setLengthMassCoeffA(double lengthMassCoeffA) {
	this.lengthMassCoeffA = lengthMassCoeffA;
	updateExpectedEnergyWithoutRepro();
	updateInitialBiomass();
    }

    public double getLengthMassCoeffB() {
	return lengthMassCoeffB;
    }

    public void setLengthMassCoeffB(double lengthMassCoeffB) {
	this.lengthMassCoeffB = lengthMassCoeffB;
	updateExpectedEnergyWithoutRepro();
	updateInitialBiomass();
    }

    public CubicSpline getExpectedEnergyWithoutRepro() {
	return expectedEnergyWithoutRepro;
    }

    public boolean hideExpectedEnergyWithoutRepro() {
	return true;
    }

    public String getSpeciesName() {
	return speciesName;
    }

    public void setSpeciesName(String speciesName) {
	this.speciesName = speciesName;
    }

    public double getAsymLengthL() {
	return asymLengthL;
    }

    public void setAsymLengthL(double asymLenghtsL) {
	this.asymLengthL = asymLenghtsL;
	updateExpectedEnergyWithoutRepro();
	updateInitialSize();
	updateInitialBiomass();
    }

    public double getGrowthCoeffK() {
	return growthCoeffK;
    }

    public void setGrowthCoeffK(double growthCoeffK) {
	this.growthCoeffK = growthCoeffK;
	updateExpectedEnergyWithoutRepro();
	updateInitialSize();
	updateInitialBiomass();
    }

    public double getAgeAtTimeZero() {
	return ageAtTimeZero;
    }

    public void setAgeAtTimeZero(double ageAtTimeZero) {
	this.ageAtTimeZero = ageAtTimeZero;
	updateExpectedEnergyWithoutRepro();
	updateInitialSize();
	updateInitialBiomass();
    }

    public int getNumPointsGrowthCurve() {
	return numPointsGrowthCurve;
    }

    public void setNumPointsGrowthCurve(int numPointsGrowthCurve) {
	this.numPointsGrowthCurve = Math.max(0, numPointsGrowthCurve);
	updateExpectedEnergyWithoutRepro();
    }

    @Override
    public String getTitle() {
	return "Species:" + speciesName;
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);
	updateExpectedEnergyWithoutRepro();
	updateInitialSize();
	updateInitialBiomass();
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	// return simple inspector that we can update
	inspector = new SimpleInspector(this, state, name);
	return inspector;
    }
}