package de.zmt.kitt.test;

import java.util.LinkedList;
import java.util.Queue;

import de.zmt.kitt.sim.EnvironmentDefinition;
import de.zmt.kitt.sim.LifeStage;
import de.zmt.kitt.sim.SpeciesDefinition;
import ec.util.MersenneTwisterFast;

public class EnergyBudget {
	
	
	SpeciesDefinition spec;
	EnvironmentDefinition env;
	MersenneTwisterFast rand;
	
	double biomass=0.0;
	
	double intakeForCurrentDay=0.0;
	/** g dry weight food per fish */
	protected double foodIntake=0;
	/** energy intake (kJ) */ 
	protected double energyIntake=0;
	/** energy consumption at zero speed (kJ) */
	protected double restingMetabolicRatePerTimestep=0;
	/** energy demand of fish (difference between expectedE and currentE at age in kJ) */
	/** current energy value of fish (all body compartments) in kJ */
	protected double currentEnergyTotal=0;
	/** current energy value of fish (all except Repro) in kJ */
	protected double currentEnergyWithoutRepro=0;
	/** expected energy value of fish based on vBGF in kJ */
	protected double expectedEnergyWithoutRepro=0;

	// BODY COMPARTMENTS
	/** holds the latest incomes of food energy in gut (kJ) */
	protected Queue<Double> gutStorageQueue = new LinkedList<Double>() ;
	/** shortterm storage of energy (kJ) */
	protected double shorttermStorage=0.0;
	/** energy in gut (kJ)*/
	protected double currentGutContent=0;
	/** current amount of body bodyFat of fish (kJ) */ 
	protected double bodyFat=0;
	/** current amount of body tissue (kJ) */
	protected double bodyTissue=0;
	/** current amount of repro fraction (kJ) */
	protected double reproFraction=0;
	/** min capacity of repro fraction for spawning: 10% of total body weight */
	protected double minReproFraction=0.1; 
	/** max capacity of repro fraction: 30% of total body weight */
	protected double maxReproFraction=0.3; 
	
	// ENERGY METABOLISM
	/** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
	static public final double restingMetabolicRateA=0.0072;
	/** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
	static public final double restingMetabolicRateB=0.79;
	/** Verlustfaktor bei flow shortterm storage <=> bodyFat */
	static public final double VerlustfaktorFatToEnergy=0.87; 
	/** Verlustfaktor bei flow shortterm storage <=> bodyTissue */
	static public final double VerlustfaktorTissueToEnergy=0.90;
	/** Verlustfaktor bei flow shortterm storage <=> reproFraction */
	static public final double VerlustfaktorReproToEnergy=0.87; 
	// energy-biomass conversions
	/** metabolizable energy (kJ) from 1 g bodyFat */
	static public final double energyPerGramFat=36.3; 
	/** metabolizable energy (kJ) from 1 g bodyTissue */
	static public final double energyPerGramTissue=6.5;  
	/** metabolizable energy (kJ) from 1 g reproFraction (ovaries) */
	static public final double energyPerGramRepro=23.5; 
	/** 1 kJ fat*conversionRate = fat in g */
	static public final double conversionRateFat=0.028; 
	/** 1 kJ bodyTissue*conversionRate = body tissue in g */
	static public final double conversionRateTissue=0.154;
	/** 1 kJ repro*conversionRate = repro in g */
	static public final double conversionRateRepro=0.043;
	
	public double virtualAgeDifference=0.0;
	public double accumulatedBiomassForWeek=0.0;
	public boolean isHungry=true;

	
	
	public EnergyBudget(SpeciesDefinition spec, EnvironmentDefinition env, double initialBiomass, MersenneTwisterFast rand){
		this.spec=spec;
		this.env=env;
		this.biomass=initialBiomass;
		this.rand=rand;
	}

	
	//////////////////CONSUMPTION///////////////////////////////////////
	// includes loss due to excretion/egestion/SDA)
	// food in g dry weight and fish in g wet weight!!
	// returns indeed foodamound taken
	public double feed(double availableFood) {	
						
		// daily consumption rate = g food dry weight/g fish wet weight*day
		// multiplied with individual fish biomass and divided by time resolution 
		// only 12 of 24 h are considered relevant for food intake, daher divided by 12 not 24!
		double consumptionRatePerTimeStep=(spec.consumptionRate*biomass/12.0/60.0)* env.timeResolutionMinutes;
		// food intake in g food dry weight
		double foodIntake=consumptionRatePerTimeStep;
		// even if fish could consume more, just take the available food on grid
		foodIntake=(foodIntake > availableFood) ? availableFood : foodIntake;

		// energy intake (kJ) = amount of food ingested (g dry weight)*energy content of food (kJ/g food dry weight)
		double energyIntake=foodIntake*spec.energyContentFood;
		// in g algal dry weight
		intakeForCurrentDay+=foodIntake;
		if(intakeForCurrentDay >= spec.maxDailyFoodRationA*biomass+spec.maxDailyFoodRationB){
			isHungry=false;
		}
		
		if((energyIntake <= ((spec.maxDailyFoodRationA*biomass + spec.maxDailyFoodRationB)*spec.energyContentFood) ) && (energyIntake>0.0) )
		 {				
			// after queueSize steps the energyIntake flows to the shortterm
			double delayForStorageInSteps= spec.gutTransitTime/env.timeResolutionMinutes;
			
			gutStorageQueue.offer(energyIntake);
			// wenn transit time (entspricht queue size) reached => E geht in shortterm storage		
			if(gutStorageQueue.size() >= delayForStorageInSteps){
				// gutStorageQueue.poll entnimmt jeweils 1. element und löscht es damit aus queue
				shorttermStorage+=spec.netEnergy*gutStorageQueue.poll();
			}			
		}
		// update the amount of food on current foodcell
		return foodIntake;
	}
	
		
	/////////////////ENERGY BUDGET////////////////////////////////////////////////////////////////
	// returns false if fish dies due to maxAge, starvation or naturalMortality
	public boolean updateEnergy(long steps, double ageInYears, double virtualAge, LifeStage lifeStage, double netActivityCosts ){		
		
		// METABOLISM (RESPIRATION)
		restingMetabolicRatePerTimestep=(restingMetabolicRateA*Math.pow(biomass, restingMetabolicRateB))*0.434* env.timeResolutionMinutes/60;	
		// total energy consumption (RMR + activities)
		double energyConsumption=restingMetabolicRatePerTimestep+netActivityCosts;	
		
		// if not enough energy for consumption in shortterm storage
		// transfer energy to shortterm storage from bodyFat, then reproFraction, and last from bodyTissue 
		// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87, von bodyTissue=0.90 (Brett & Groves 1979)
		if(shorttermStorage < energyConsumption) {	
			// not enough in bodyFat AND reproFraction => metabolise energy from ALL 3 body compartments
			if( (bodyFat < (shorttermStorage-energyConsumption)/VerlustfaktorFatToEnergy) && (reproFraction < (energyConsumption-shorttermStorage - energyConsumption)/VerlustfaktorFatToEnergy)) {
				double energyFromProtein=(energyConsumption-shorttermStorage-(bodyFat/VerlustfaktorFatToEnergy)-(reproFraction/VerlustfaktorReproToEnergy)) / VerlustfaktorTissueToEnergy;
				shorttermStorage+=energyFromProtein*VerlustfaktorTissueToEnergy;
				bodyTissue-=energyFromProtein;				
				shorttermStorage+=bodyFat*VerlustfaktorFatToEnergy;
				bodyFat=0;
				reproFraction=0;
			}
			// transfer energy to shortterm storage from bodyFat and then from reproFraction
			// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87 
			else if(bodyFat < (shorttermStorage-energyConsumption)/VerlustfaktorFatToEnergy) {
					double energyFromRepro=(energyConsumption-shorttermStorage-(bodyFat/VerlustfaktorFatToEnergy))/VerlustfaktorTissueToEnergy;
					shorttermStorage+=energyFromRepro*VerlustfaktorReproToEnergy;
					reproFraction-=energyFromRepro;				
					shorttermStorage+=bodyFat*VerlustfaktorFatToEnergy;
					bodyFat=0;
			}
			// if not enough energy for consumption in shortterm storage but enough in bodyFat, energy diff is metabolized from bodyFat only
			else{
				double diff=energyConsumption-shorttermStorage;
				shorttermStorage+=diff;
				// vom bodyFat muss mehr abgezogen werden due to verlust beim metabolizieren
				bodyFat-=diff/VerlustfaktorFatToEnergy;
			}
		}
		// enough energy for consumption in shortterm storage
		else {
			shorttermStorage-=energyConsumption;		
		}
		
		// PRODUCTION (Growth, reproduction is calculated extra)
		// if more energy in shortterm storgae then needed for metablism and it exceeds shortterm storage maxCapcity
		// maxCapacity shortterm storage = 450*restingMetabolicRatePerTimestep (nach Hauke) CHECK!!
		if(shorttermStorage > restingMetabolicRatePerTimestep*450){
			double energySpillover=shorttermStorage-restingMetabolicRatePerTimestep*450;
			shorttermStorage-=energySpillover;
				if ((lifeStage==LifeStage.FEMALE) && (reproFraction < biomass*maxReproFraction*energyPerGramRepro)) {
					// => energy is transfered into bodycompartments with same verlustfaktor wie beim metabolisieren zu energie
					// wenn female: zu 95% zu bodyTissue, zu 3.5% zu bodyFat, 1.5% zu repro (f�r repro: following Wootton 1985)
					bodyFat+=VerlustfaktorFatToEnergy*energySpillover*0.035;
					bodyTissue+=VerlustfaktorTissueToEnergy*energySpillover*0.95;
					reproFraction+=VerlustfaktorReproToEnergy*energySpillover*0.015;
				}
				// wenn juvenile oder max capacity reproFraction f�r females erreicht
				else {
					// => energy is transfered into bodycompartments with same verlustfaktor wie beim metabolisieren zu energie
					// zu 95% zu bodyTissue, zu 5% zu bodyFat according to body composition 
					bodyFat+=VerlustfaktorFatToEnergy*energySpillover*0.05;
					bodyTissue+=VerlustfaktorTissueToEnergy*energySpillover*0.95;
				}
						
		}
		
		// adjustment of virtual age
		// comparision of overall current energy at age vs. expected energy at age (vBGF) to slow down growth (more realistic)
		// energy in gut (aufsummieren der GutContentElemente)
		currentGutContent=0;
		for(Double d:gutStorageQueue){
			currentGutContent+=d;			
		}
		// sum of energy in all body compartments	
		currentEnergyWithoutRepro=bodyTissue+bodyFat+shorttermStorage+currentGutContent;
		expectedEnergyWithoutRepro=spec.expectedEnergyWithoutRepro.interpolate( ageInYears);
		
		System.out.print(currentEnergyWithoutRepro);

		// daily: compare current growth with expected growth at age from vBGF + ggf adjust virtual age + die of starvation, maxAge, naturalMortality
		if(steps % (60/env.timeResolutionMinutes*24) == 0) { 
			double maxAge=spec.maxAgeInYrs+rand.nextGaussian();	
			
			if ((expectedEnergyWithoutRepro-currentEnergyWithoutRepro) > 10) {
			
			// das funktioniert so nicht! abfrage dreht sich im kreis!!	
				//asymLenghtsL*(1- Math.pow(Math.E,-growthCoeffB*(age+ageAtTimeZero)));		
				double diff=ageInYears-virtualAge;
				virtualAgeDifference+=diff;	
			}
			
			if(( currentEnergyWithoutRepro < 0.6*expectedEnergyWithoutRepro) 
			|| maxAge <= ageInYears 
			|| (spec.mortalityRatePerYears/365) > rand.nextDouble()){
			
				//die();
				//return false;
			}
		}
	
		// adjust isHungry, REICHT DIE ABFRAGE AN DIESER STELLE UND ALLES ABGEDECKT?
		// 1.abfrage = to limit overall growth, 2.abfrage to limit daily intake
		if (currentEnergyWithoutRepro >= 0.95*expectedEnergyWithoutRepro || intakeForCurrentDay >= (spec.maxDailyFoodRationA*biomass+spec.maxDailyFoodRationB)){   
		
			isHungry=false;
		}
		else {
			
			isHungry=true;
		}		
		return true;
	}

	
	/////////////////GROWTH////////////////////////////////////////
	// called daily to update biomass, size only weekly
	// returns new size
	public void grow(long steps) {
		
		double size=0.0;
		// update fish biomass (g wet weight)
		// conversion factor for shortterm and gut same as for tissue
		biomass=(bodyFat*conversionRateFat)+(bodyTissue+shorttermStorage+currentGutContent)*conversionRateTissue+(reproFraction*conversionRateRepro);
		
		// update fish size (SL in cm)
		if( (steps % ((60*24*7)/env.timeResolutionMinutes)) == 0) {
			if( biomass > accumulatedBiomassForWeek) {
				//W(g WW)=A*L(SL in cm)^B ->  L=(W/A)^1/B
				double exp=1/spec.lengthMassCoeffB; 
				double base=biomass/spec.lengthMassCoeffA;
				size=Math.pow(base,exp);
				//for testing
				System.out.println("biomass: " + biomass);
				System.out.println("size: " + size);			
			}		
			accumulatedBiomassForWeek=biomass;
		}		
	}
}
