package de.zmt.kitt.sim.params;

import javax.xml.bind.annotation.*;

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

@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition extends ParameterDefinition {
    // for carnivores 0.59, same Ref!

    /** post-settlement age in yrs */
    public static final double INITIAL_AGE_YEARS = 0.33; // approx. 120 days

    /** name of species */
    public String speciesName = "Chlorurus sordidus";

    /** how many individuals should be put at the beginning of the simulation */
    public int initialNr = 1; // check typical pop size in literature

    /** initial size when born (cm) */
    public double initialSize = 12; // �ber vBGF
    /** intial biomass when born (g wet weight) based on intialSize */
    // @XmlTransient
    public double initialBiomass = 8; // �ber weight-length relationship
    /** daily food consumption rate (g dry weight food/g fish wet weight*day */
    public double consumptionRate = 0.236; // Polunin et al. 1995
    /** energy content of food (kJ/g dry weight food */
    public double energyContentFood = 17.5; // nach Bruggemann et al. 1994
    // max daily food ration(g algal DW)=0.019*biomass+3.294 nach Brugemann et
    // al. 1994
    public double maxDailyFoodRationA = 0.019;
    public double maxDailyFoodRationB = 3.294;
    /** food transit time through gut in min */
    public double gutTransitTime = 54; // Polunin et al. 1995

    public double mortalityRatePerYears = 0.519; // McIlwain 2009
    public double maxAgeInYrs = 18.75; // El-Sayed Ali et al. 2011

    // reproduction ///////////////////////////////////////////
    public int nrOffspring = 1;

    /**
     * net energy= usable percentage of digested energy after substraction of
     * assimilation, egestion, excretion, specific dynamic actions
     */
    public double netEnergy = 0.43; // for herbivores (Brett & Groves 1979)
    // estimation of size-at-age with vonBertalanffyGrowthFunction (vBGF)
    // parameters of the vBGF to calculate length at age t: L(t)= L*( 1-
    // e^(-K*(t-t(0)))
    /** asymptotic length L */
    double asymLenghtsL = 39.1;
    /** growth coefficient K */
    double growthCoeffK = 0.15;
    /** theoretical age at zero size */
    double ageAtTimeZero = -1.25;
    /** length-weight relationsship: W(g wet weight)=A*L(SL in cm)^B */
    public double lengthMassCoeffA = 0.0309; // El-Sayed Ali et al. 2011
    public double lengthMassCoeffB = 2.935; // El-Sayed Ali et al. 2011

    private final int numPointsGrowthCurve = 50;
    @XmlTransient
    public double[] ageSteps; // holds the age coordinates?? at which the fish
			      // size is precalculated
    @XmlTransient
    private final double[] expectedEnergyAtAgeSteps; // in kJ
    @XmlTransient
    public CubicSpline expectedEnergyWithoutRepro; // in kJ

    // TODO parameter changes via gui are not reflected here
    public SpeciesDefinition() {

	// precalculation of expectedEnergy at specific ages predicted by vBGF
	// maximum age (vrs) for the precalculation
	double maximalAge = 20.0;
	expectedEnergyAtAgeSteps = new double[numPointsGrowthCurve];
	ageSteps = new double[numPointsGrowthCurve];
	for (int i = 0; i < numPointsGrowthCurve; i++) {
	    double age = maximalAge / numPointsGrowthCurve * i;
	    ageSteps[i] = age;

	    // vBGF: L(t)= L*( 1- e^(-K*(t-t(0)))
	    double expectedSizeAtAge = asymLenghtsL
		    * (1 - Math.pow(Math.E, -growthCoeffK
			    * (age - ageAtTimeZero)));

	    // length mass relationship: W=a*L^b
	    double biomass = lengthMassCoeffA
		    * Math.pow(expectedSizeAtAge, lengthMassCoeffB);

	    // 1 g fish wet weight = 6.5 kJ (only valid without repro!)
	    double energyWithoutRepro = biomass * 6.5;
	    expectedEnergyAtAgeSteps[i] = energyWithoutRepro;
	}

	// to initialise size: take post-settlement age of ca. 120 days=0.33
	// yrs?? sp�ter anders wenn realistische population!
	// size initialized �ber vBGF at given initialAgeInYrs
	initialSize = Math.abs(asymLenghtsL
		* (1 - Math.pow(Math.E, -growthCoeffK
			* (INITIAL_AGE_YEARS - ageAtTimeZero))));
	// biomass initialized �ber weight-length-relationship at calculated
	// initialSize
	initialBiomass = lengthMassCoeffA
		* Math.pow(initialSize, lengthMassCoeffB);

	expectedEnergyWithoutRepro = new CubicSpline(ageSteps,
		expectedEnergyAtAgeSteps);
    }

    @Override
    public String getTitle() {
	return "Species:" + speciesName;
    }
}