package de.zmt.kitt.sim;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.sun.j3d.utils.behaviors.interpolators.CubicSplineCurve;

import flanagan.interpolation.CubicSpline;


/**
 * holds the initial parameters and general properties to define a species.<br />
 * in the simulation of this kind are instantiated holding the following
 * values for the properties
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) its defined <br />
 * that all fields that are not marked as transient are written to xml file.
 * 
 *  */

@XmlAccessorType(XmlAccessType.FIELD)
public class SpeciesDefinition{
		
	/** holds the identifier for this species */
	public int  speciesId;
	/**  name of species   */
	public String  speciesName="Chlorurus sordidus";
	
	/** how many individuals should be put at the beginning of the simulation */	
	public int initialNr; //check typical pop size in literature
	/** environmental carrying capacity for this species */
	public int maxNr; //check carrying capacity in literature 
	/** type of activity (diurnal or nocturnal) */
	public String  activityType="diurnal";

	///**  what is the farest place that can be perceived */	
	public double perceptionRangeHabitat = 20; //unit?
	
	/**  initial size when born (cm)*/
	public double initialSize=12; //�ber vBGF
	/**  intial biomass when born (g wet weight) based on intialSize*/
	//@XmlTransient 
	public double initialBiomass=8; //�ber weight-length relationship
	/** average step size factor for moving */
	public double stepForaging=2.3;
	/** average step size factor for moving */
	public double stepMigration=3.5;
	/** average step size factor for moving */
	public double stepResting=0.01;
	/** daily food consumption rate (g dry weight food/g fish wet weight*day */
    public double consumptionRate=0.236; // Polunin et al. 1995
    /** energy content of food (kJ/g dry weight food */
    public double energyContentFood=17.5; // nach Bruggemann et al. 1994
    //max daily food ration(g algal DW)=0.019*biomass+3.294 nach Brugemann et al. 1994
    public double maxDailyFoodRationA=0.019; 
    public double maxDailyFoodRationB=3.294;
    /** food tranist time through gut in min */
    public double gutTransitTime=54; //Polunin et al. 1995
    
    //SWIMMING SPEED
    //warum x und y?? ist das nicht �ber turning angle gegeben??
	public double xSpeedMax=35, ySpeedMax=50; //
	// in cm/s=35 alle werte nach Korsemeyer et al. 2002 f�r scarus schlegeli
	/** speed in total length per sec */
	public double minSpeed=1.5;
	// in cm/s=90 
	// bei Wainwright et al. 2002 undisturbed straight routine swimming speed: bei C. sordidus: 3.55 TL/s
	/** speed in total length per sec */
	public double maxSpeed=3.5;
	// opt speed = ca. 53 cm per sec
	/** optimum speed in total length per sec */
	public double optSpeed=2.3;
	//typischer wert f�r startle response bei fischen zw. 10-20 cm l�nge nach Videler 1993
	//also 100-200 cm per sec
	/** speed in total length per sec */
	public double maxBurstSpeed=10;
	
	
	public double mortalityRatePerYears=0.519; //McIlwain 2009
	public double maxAgeInYrs=18.75; //El-Sayed Ali et al. 2011
	
	// reproduction ///////////////////////////////////////////
	/**  maturity in dependency of size ? */	
	public double maturitySizeFactor=1.0; //	
	
	/**  min repro size in cm */
	public double reproSize=12.34; // McIlawin 2009
	/**  size (standard length) in cm */
	public double minReproSize=12; // McIlawain 2009
	/**   */
	public int nrOffspring=1;
	
	/** net energy= usable percentage of digested energy 
	 * after substraction of assimilation, egestion, excretion, specific dynamic actions*/
	public double netEnergy=0.43; //for herbivores (Brett & Groves 1979)
	// for carnivores 0.59, same Ref!
	
	// ca. 120 tage
	/** post-settlement age in yrs */
	static public final double initialAgeInYrs=0.33; 
	// estimation of size-at-age with vonBertalanffyGrowhtFunction (vBGF)
	//parameters of the vBGF to calculate length at age t: L(t)= L*( 1- e^(-K*(t-t(0)))
	/** asymptotic length L*/
	double asymLenghtsL=39.1;
	/** growth coefficient K*/
	double growthCoeffK=0.15;
	/** theoretical age at zero size */
	double ageAtTimeZero=-1.25;
	/**length-weight relationsship: W(g wet weight)=A*L(SL in cm)^B */
	public double lengthMassCoeffA=0.0309; // El-Sayed Ali et al. 2011
	public double lengthMassCoeffB=2.935; // El-Sayed Ali et al. 2011
	
	int numPointsGrowthCurve=50;
	@XmlTransient public double[] ageSteps;   // holds the age coordinates?? at which the fish size is precalculated
	@XmlTransient public double[] expectedEnergyAtAgeSteps; //in kJ 
	@XmlTransient public CubicSpline expectedEnergyWithoutRepro; //in kJ
	
		
	public SpeciesDefinition(){
		
		// precalculation of expectedEnergy at specific ages predicted by vBGF		
		// maximum age (vrs) for the precalculation
		double maximalAge=20.0;
		expectedEnergyAtAgeSteps=new double[numPointsGrowthCurve];
		ageSteps= new double[numPointsGrowthCurve];
		for( int i=0; i<numPointsGrowthCurve;i++){
			double cur=i;				
			double age=maximalAge/(double)numPointsGrowthCurve*cur;
			ageSteps[i]=age;
			// vBGF: L(t)= L*( 1- e^(-K*(t-t(0)))
			double expectedSizeAtAge=asymLenghtsL*(1- Math.pow(Math.E,-growthCoeffK*(age-ageAtTimeZero)));	
			// length mass relationship: W=a*L^b 
			double biomass=lengthMassCoeffA*Math.pow(expectedSizeAtAge, lengthMassCoeffB); 
			// 1 g fish wet weight = 6.5 kJ (nur gültig, wenn OHNE repro!!)
			// 
			double energyWithoutRepro=biomass*6.5;
			expectedEnergyAtAgeSteps[i]=energyWithoutRepro;
		}
		
		//to initialise size: take post-settlement age of ca. 120 days=0.33 yrs?? sp�ter anders wenn realistische population!
		// size initialized �ber vBGF at given initialAgeInYrs
		initialSize=Math.abs(asymLenghtsL*(1- Math.pow(Math.E,-growthCoeffK*(initialAgeInYrs-ageAtTimeZero))));
		// biomass initialized �ber weight-length-relationship at calculated initialSize
		initialBiomass=lengthMassCoeffA * Math.pow(initialSize, lengthMassCoeffB); 
			
		expectedEnergyWithoutRepro=new CubicSpline(ageSteps,expectedEnergyAtAgeSteps);
		//energy at initial age for testing -> ok!
		//double energy=expectedEnergy.interpolate(initialAgeInYrs);
		//System.out.println(energy);
	}
	
	
	
	//double[][] data= new double[2][n]; // two coordinates for n points
	//0.95 5.1e-1
	//1.91 105.658
	//2.86 1.777E3
	/*
	
	//Regression regression = new Regression(xs, ys);
	//regression.exponentialSimple();
	
	Regression reg = new Regression(xs, ys);
    //reg.supressPrint();
    //reg.exponential();
    reg.exponentialPlot();
    double[] coeff = reg.getCoeff();
    System.out.println("EXPONENTIAL DISTRIBUTION");
    System.out.println("Best Estimates:");
    System.out.println("Location parameter [mu] ");
    System.out.println(coeff[0]);
    System.out.println("Scale parameter [sigma] ");
    System.out.println(coeff[1]);
    System.out.println("Scaling factor [Ao] ");
    System.out.println(coeff[2]);
    //Regression.regressionDetails(fout, reg);
	*/
	
	//double[] xs= new double[]{1.05,2.094,3.14};
	//double[] ys= new double[]{5.1e-1, 105.658, 1.78e+3};

};