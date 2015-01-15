package de.zmt.kitt.sim.engine.agent;

import java.util.*;
import java.util.logging.Logger;

import sim.engine.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;

/**
 * Fish implements the behavior of the fish<br />
 * 
 * @author oth
 */
public class Fish extends Agent {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Fish.class.getName());

    private static final long serialVersionUID = 1L;

    /** Maximum size of position history */
    public static final int POS_HISTORY_MAX_SIZE = 10;

    /** min capacity of repro fraction for spawning: 10% of total body weight */
    private static final double MIN_REPRO_FRACTION = 0.1;
    /** max capacity of repro fraction: 30% of total body weight */
    private static final double MAX_REPRO_FRACTION = 0.3;

    // ENERGY METABOLISM
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    private static final double RESTING_METABOLIC_RATE_A = 0.0072;
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    private static final double RESTING_METABOLIC_RATE_B = 0.79;
    /** Verlustfaktor bei flow shortterm storage <=> bodyFat */
    private static final double LOSS_FACTOR_FAT_TO_ENERGY = 0.87;
    /** Verlustfaktor bei flow shortterm storage <=> bodyTissue */
    private static final double LOSS_FACTOR_TISSUE_TO_ENERGY = 0.90;
    /** Verlustfaktor bei flow shortterm storage <=> reproFraction */
    private static final double LOSS_FACTOR_REPRO_TO_ENERGY = 0.87;

    // energy-biomass conversions
    /** metabolizable energy (kJ) from 1 g bodyFat */
    private static final double ENERGY_PER_GRAM_FAT = 36.3;
    /** metabolizable energy (kJ) from 1 g bodyTissue */
    private static final double ENERGY_PER_GRAM_TISSUE = 6.5;
    /** metabolizable energy (kJ) from 1 g reproFraction (ovaries) */
    private static final double ENERGY_PER_GRAM_REPRO = 23.5;
    /** 1 kJ fat*conversionRate = fat in g */
    private static final double CONVERSION_RATE_FAT = 0.028;
    /** 1 kJ bodyTissue*conversionRate = body tissue in g */
    private static final double CONVERSION_RATE_TISSUE = 0.154;
    /** 1 kJ repro*conversionRate = repro in g */
    private static final double CONVERSION_RATE_REPRO = 0.043;
    // GROWTH AND AGE
    /** current bio mass of the fish (g wet weight) */
    private double biomass;
    /** store weekly bio mass update (g wet weight) */
    private double oldBiomassWeekly = 0;
    /** current size of fish as standard length (cm) */
    private double size;
    /** age in years */
    private double age;
    /** timestep of fish initialisation for age calculation */
    private double birthTimeStep;

    // BODY COMPARTMENTS
    /** holds the latest incomes of food energy in gut (kJ) */
    private final Queue<Double> gutStorageQueue = new LinkedList<Double>();
    /** shortterm storage of energy (kJ) */
    private double shorttermStorage = 0.0;
    /** energy in gut (kJ) */
    private double currentGutContent = 0;
    /** current amount of body bodyFat of fish (kJ) */
    private double bodyFat = 0;
    /** current amount of body tissue (kJ) */
    private double bodyTissue = 0;
    /** current amount of repro fraction (kJ) */
    private double reproFraction = 0;

    // FEEDING AND ENERGY BUDGET
    /** hunger state of the fish */
    private boolean hungry = true;
    /** amount of energy taken in during the current day (kJ) */
    private double intakeForCurrentDay = 0;

    // MOVING
    /** attraction center of habitat-dependent foraging area */
    private Double2D attrCenterForaging = new Double2D();
    /** attraction center of habitat-dependent resting area */
    private Double2D attrCenterResting = new Double2D();
    /** current speed of agent in x- and y-direction (unit?) */
    private double xSpeed, ySpeed; // unit?

    // REPRODUCTION
    /** each fish starts as a post-settlement juvenile */
    // nicht besonders clever, typical pop sturcture sollte start sein!!
    // dann auch initSize/biomass und lifestage anpassen!!
    /** life stage of fish (juvenile, female or male) */
    // TODO should change somewhere
    private final GrowthState growthState = GrowthState.JUVENILE;

    /**
     * Memory of fish's locations. For each cell a counter is increased when the
     * fish has shown up there.
     */
    private final Memory memory;

    /** History of the last {@value #POS_HISTORY_MAX_SIZE} positions. */
    private final Queue<Double2D> posHistory = new LinkedList<Double2D>();

    private final SpeciesDefinition speciesDefinition;

    /** references to the environment to have access e.g. to the field */
    private final Environment environment;

    /**
     * @param x
     *            initial x Position
     * @param y
     *            initial y Position
     * @param b
     *            initial biomass
     */
    public Fish(Double2D pos, Environment environment,
	    SpeciesDefinition speciesDefinition) {
	super(pos);
	/** references to the environment to have access e.g. to the field */
	this.environment = environment;

	this.biomass = speciesDefinition.initialBiomass;
	this.oldBiomassWeekly = biomass;
	this.size = speciesDefinition.initialSize;

	// allocating initialBiomass to body compartments as E values (kJ)
	// wenn auch female fehlt noch reproFraction!!
	this.bodyFat = biomass * 0.05 * ENERGY_PER_GRAM_FAT;
	this.bodyTissue = biomass * 0.95 * ENERGY_PER_GRAM_TISSUE;

	this.speciesDefinition = speciesDefinition;

	initCentersOfAttraction();

	memory = new Memory(environment.getWidth(), environment.getHeight());
    }

    /**
     * searches a randomly drawn position in suitable habitat for resting and
     * feeding
     */
    private void initCentersOfAttraction() {
	// DEFINE STARTING CENTER OF ATTRACTIONS (ONCE PER FISH LIFE)
	// find suitable point for center of attraction for foraging
	// (random, but only in preferred habitattype)
	do {
	    attrCenterForaging = environment.getRandomFieldPosition();
	    // hier abfrage nach habitat preference von spp def?
	} while (environment.getHabitatOnPosition(attrCenterForaging) != Habitat.SEAGRASS);

	// find suitable point for center of attraction for resting (depending
	// on habitat preference)
	do {
	    attrCenterResting = environment.getRandomFieldPosition();

	} while (environment.getHabitatOnPosition(attrCenterResting) != Habitat.CORALREEF);

    }

    /**
     * called every time when its taken from queue in the scheduler lifeloop of
     * the agent
     */
    @Override
    public void step(final SimState state) {
	Sim sim = (Sim) state;
	int timeResolutionMinutes = sim.getParams().environmentDefinition.timeResolutionMinutes;
	long steps = sim.schedule.getSteps();

	switch (lifeState) {
	case INSTANTIATED:
	    // birth
	    this.birthTimeStep = steps;
	    lifeState = LifeState.ALIVE;
	    break;
	case ALIVE:
	    double netActivityCosts = move(sim.random, timeResolutionMinutes);
	    // memory fading
	    // memField.multiply(0.5);

	    // biomass=biomass+ 0.2 * biomass; //wo kommt das her?
	    if (hungry == true) {
		// CONSUMPTION (C)
		feed(timeResolutionMinutes);
	    }

	    // ENERGY BUDGET (RESPIRATION, R)
	    if (updateEnergy(sim.random, steps, timeResolutionMinutes,
		    netActivityCosts) == false) {
		logger.finer(this + "died");
	    }

	    // DAILY UPDATES:
	    if (steps % (60 / timeResolutionMinutes * 24) == 0) {
		//
		// PRODUCTION I (growth): update of BIOMASS (g wet weight) +
		// weekly update of SIZE (cm);
		grow(steps, timeResolutionMinutes);

		// Change of lifeStage if size appropriate double
		// currentSize=giveSize(); // // size frame = variation? wie
		// macht
		// man das mit 50% levels? // double
		// sizeFrame=0.3*random.nextGaussian(); //warum 0.3?? //
		// if((currentSize > (speciesDefinition.reproSize-sizeFrame)) ||
		// (currentSize < (speciesDefinition.reproSize+sizeFrame))) { //
		// has a probability at this point of 1/(365days/7days) ??? //
		// if(random.nextDouble() > (1.0/(365.0/7.0))) { //
		// lifeStage=LifeStage.FEMALE; // } // } // aber nur zu 50% !!
		// if(currentSize > (speciesDefinition.reproSize)) {
		// lifeStage=LifeStage.FEMALE; }

		// PRODUCTION II (reproduction) // spawn if lifeStage=female &&
		// repro zw. 20-30% of biomass && currentEohneRepro >= 75% of
		// expectedEohneRepro at age (nach Wootton 1985) // C. sordidus
		// spawns on a daily basis without clear seasonal pattern
		// (McILwain
		// 2009) // HINZUFÜGEN: Wahrscheinlichkeit von nur 5-10%, damit
		// nicht alle bei genau 20% reproduzieren, aber innerhalb der
		// nächsten Tagen wenn über 20% if(lifeStage==LifeStage.FEMALE
		// &&
		// (reproFraction >= (biomass*0.2*energyPerGramRepro))) {
		// reproduce(); }

		// mge: needs to set the intake Back for every Day
		intakeForCurrentDay = 0;

	    }

	    break;
	case DEAD:

	    break;
	}

    }

    // calculate vector from current position to center of attraction
    private Double2D getDirectionToAttraction(Double2D currentPos,
	    Double2D pointOfAttraction) {

	double dirX = pointOfAttraction.x - currentPos.x;
	double dirY = pointOfAttraction.y - currentPos.y;
	double length = Math.sqrt((dirX * dirX) + (dirY * dirY));

	double nx = dirX / length;
	double ny = dirY / length;
	return new Double2D(nx, ny);
    }

    private double getDistance(Double2D currentPos, Double2D pointOfAttraction) {

	double dirX = pointOfAttraction.x - currentPos.x;
	double dirY = pointOfAttraction.y - currentPos.y;
	return Math.sqrt((dirX * dirX) + (dirY * dirY));
    }

    /**
     * agent's movement in one step with previously determined moveMode
     * 
     * @return energy cost in kJ
     */
    // TODO clean up
    private double move(MersenneTwisterFast random, int timeResolutionMinutes) {
	double scal = 100.0; // get the distance scaled to a value between 0 and
			     // 10 for input in tanh function
	double dist = 0.0;
	// attraction points are calced when sunrise or sunset
	Double2D attractionDir = new Double2D();

	DielCycle dielCycle = DielCycle
		.getDielCycle(environment.getHourOfDay());
	// hier stimmt abfrage noch nicht,
	// evtl über activity(gibts nocht nicht) lösen, da bei nocturnal
	// sunrise/sunset behaviour genau umgekehrt!!
	Habitat currentHabitat = environment.getHabitatOnPosition(pos);
	if (dielCycle == DielCycle.SUNRISE
		|| currentHabitat == Habitat.SEAGRASS) {
	    dist = getDistance(pos, getAttrCenterForaging());
	    attractionDir = getDirectionToAttraction(pos,
		    getAttrCenterForaging());
	} else if (dielCycle == DielCycle.SUNSET
		|| currentHabitat == Habitat.CORALREEF) {
	    dist = getDistance(pos, attrCenterResting);
	    attractionDir = getDirectionToAttraction(pos, attrCenterResting);
	}
	// probability to migrate to attraction is calulated by tanh
	double probMigration = Math.tanh(dist / scal);

	// step = cm per time step (stimmt das mit cm??) => abhängig von
	// size(bodylength), cell resolution und time resolution
	// step=6; //size/params.environmentDefinition.cellResolution *
	// speciesDefinition.stepMigration * 60 *
	// timeResolutionMinutes;

	// migrating -biased correlated random walk when sunrise and far from
	// attraction
	// TODO lots of unexplained constants (step)
	double step = 0;
	if (probMigration > 0.1) {
	    step = 6;
	    double newXSpeed = 0;
	    double newYSpeed = 0;
	    double speedup = 2.0;

	    // prob to attraction + correlated random walk:
	    newXSpeed = xSpeed + (probMigration * attractionDir.x * step)
		    + (1 - probMigration) * random.nextGaussian() * step;
	    newYSpeed = ySpeed + (probMigration * attractionDir.y * step)
		    + (1 - probMigration) * random.nextGaussian() * step;

	    Habitat targetHabitat = environment
		    .getHabitatOnPosition(new Double2D(pos.x + newXSpeed, pos.y
			    + newYSpeed));
	    if (targetHabitat == Habitat.MAINLAND) {
		newXSpeed = (probMigration * attractionDir.x
			* random.nextGaussian() * step)
			- newXSpeed * speedup;
		newYSpeed = (probMigration * attractionDir.y
			* random.nextGaussian() * step)
			- newYSpeed * speedup;
	    }
	    xSpeed = newXSpeed;
	    ySpeed = newYSpeed;
	} else {
	    double newXSpeed = 0;
	    double newYSpeed = 0;
	    // foraging at daytime when its light
	    if (dielCycle == DielCycle.DAY) {
		step = 6;
		int counter = 0;
		do {
		    newXSpeed = xSpeed * 0.2 + random.nextGaussian() * step;
		    newYSpeed = ySpeed * 0.2 + random.nextGaussian() * step;

		    Double2D posCandidate = new Double2D(pos.x + xSpeed, pos.y
			    + ySpeed);
		    if ((environment.getFoodOnPosition(posCandidate) > 1.0)
			    && (environment.getHabitatOnPosition(posCandidate) == Habitat.SEAGRASS)
			    && (environment.getHabitatOnPosition(posCandidate) != Habitat.MAINLAND)) {
			break;
		    }
		} while (counter++ < 5);
	    }
	    // resting at night when its dark
	    else if (dielCycle == DielCycle.NIGHT
		    || dielCycle == DielCycle.LATE_NIGHT) {
		step = 1;
		int counter = 0;
		do {
		    newXSpeed = xSpeed * 0.1 + random.nextGaussian() * step;
		    newYSpeed = ySpeed * 0.1 + random.nextGaussian() * step;

		    Double2D posCandidate = new Double2D(pos.x + xSpeed, pos.y
			    + ySpeed);
		    if ((environment.getHabitatOnPosition(posCandidate) == Habitat.CORALREEF)
			    && (environment.getHabitatOnPosition(posCandidate) != Habitat.MAINLAND)) {
			break;
		    }
		} while (counter++ < 5);
	    }
	    xSpeed = newXSpeed;
	    ySpeed = newYSpeed;
	}

	double speedMax = 12; // erstmal nur so
	// ca 8 cm size of fish
	if (xSpeed > speedMax)
	    xSpeed = speedMax;
	else if (xSpeed < -speedMax)
	    xSpeed = -speedMax;
	if (ySpeed > speedMax)
	    ySpeed = speedMax;
	else if (ySpeed < -speedMax)
	    ySpeed = -speedMax;

	double newX = pos.x + xSpeed;
	double newY = pos.y + ySpeed;

	if ((newX > environment.getWidth() || newX < 0)
		|| environment.getHabitatOnPosition(new Double2D(newX, newY)) == Habitat.MAINLAND) {
	    xSpeed = -xSpeed;
	    newX = pos.x + xSpeed;
	    // if vector intersects border then reflect at border
	    // Double2D reflected = Vec.reflectVector(new
	    // Double2D(xSpeed,ySpeed), new Double2D(0,1));
	}
	if ((newY > environment.getHeight() || newY < 0)
		|| environment.getHabitatOnPosition(new Double2D(newX, newY)) == Habitat.MAINLAND) {
	    ySpeed = -ySpeed;
	    newY = pos.y + ySpeed;
	    // if vector intersects border then reflect at border
	}

	pos = new Double2D(newX, newY);
	environment.getFishField().setObjectLocation(this, pos);

	memory.increase(pos);

	posHistory.offer(pos);
	if (posHistory.size() >= POS_HISTORY_MAX_SIZE) {
	    posHistory.poll();
	}

	// feeding if day
	if (dielCycle == DielCycle.DAY) {
	    double availableFood = environment.getFoodOnPosition(pos);
	    double food = availableFood - 0.3;
	    if (food < 0)
		food = 0;
	    environment.setFoodOnPosition(pos, food);
	}
	// 4. CALCULATE ACTIVITY COSTS depending on move mode ueber according
	// speed
	// net activity costs per hour (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142
	// (=>oxicaloric value!)
	// speed = step (cm per time step) in cm per sec umrechnen STIMMT SO
	// NOCH NICHT! STEP IST NICHT IN CM ODER?
	double currentSpeed = step / (60 * timeResolutionMinutes);
	// net costs per timestep = 1.193*speed pro sec^1.66*oxicaloric
	// value/60*timeResolution
	// mge : 300000 ersetzt f�r Test s --> Sterben sollt vern�nftig gehen
	// gilt so nur für parrots, gibts was allgemein gültiges?? (0.0142)
	return (1.193 * Math.pow(currentSpeed, 1.66)) * 30000 / 60
		* timeResolutionMinutes;
    }

    // ////////////////CONSUMPTION///////////////////////////////////////
    // includes loss due to excretion/egestion/SDA)
    // food in g dry weight and fish in g wet weight!!
    private void feed(int timeResolutionMinutes) {
	// get the amount of food on current patch of foodField in g dry
	// weight/m2
	double availableFood = environment.getFoodOnPosition(pos);
	// daily consumption rate = g food dry weight/g fish wet weight*day
	// multiplied with individual fish biomass and divided by time
	// resolution
	// only 12 of 24 h are considered relevant for food intake, daher
	// divided by 12 not 24!
	double consumptionRatePerTimeStep = (speciesDefinition.consumptionRate
		* biomass / 12 / 60)
		* timeResolutionMinutes;
	// food intake in g food dry weight
	double foodIntake = consumptionRatePerTimeStep;
	// even if fish could consume more, just take the available food on grid
	foodIntake = (foodIntake > availableFood) ? availableFood : foodIntake;

	// energy intake (kJ) = amount of food ingested (g dry weight)*energy
	// content of food (kJ/g food dry weight)
	double energyIntake = foodIntake * speciesDefinition.energyContentFood;
	// in g algal dry weight
	intakeForCurrentDay += foodIntake;

	if ((energyIntake <= ((speciesDefinition.maxDailyFoodRationA * biomass + speciesDefinition.maxDailyFoodRationB) * speciesDefinition.energyContentFood))
		&& (energyIntake > 0.0)) {

	    // after queueSize steps the energyIntake flows to the shortterm
	    double delayForStorageInSteps = speciesDefinition.gutTransitTime
		    / timeResolutionMinutes;

	    gutStorageQueue.offer(energyIntake);
	    // wenn transit time (entspricht queue size) reached => E geht in
	    // shortterm storage
	    if (gutStorageQueue.size() >= delayForStorageInSteps) {
		// gutStorageQueue.poll entnimmt jeweils 1. element und löscht
		// es damit aus queue
		shorttermStorage += speciesDefinition.netEnergy
			* gutStorageQueue.poll();
	    }
	}
	// update the amount of food on current food cell
	environment.setFoodOnPosition(pos, availableFood - foodIntake);
    }

    /**
     * 
     * @param sim
     * @return false if fish dies due to maxAge, starvation or naturalMortality
     */
    private boolean updateEnergy(MersenneTwisterFast random, long steps,
	    int timeResolutionMinutes, double netActivityCosts) {
	// METABOLISM (RESPIRATION)
	double restingMetabolicRatePerTimestep = (RESTING_METABOLIC_RATE_A * Math
		.pow(biomass, RESTING_METABOLIC_RATE_B))
		* 0.434
		* timeResolutionMinutes / 60;
	// total energy consumption (RMR + activities)
	double energyConsumption = restingMetabolicRatePerTimestep
		+ netActivityCosts;
	// substract birthtimestep from current timestep+add
	// TODO intialAgeInDays(=>vorher in timesteps umrechnen!)
	age = (steps + 1 - birthTimeStep + SpeciesDefinition.INITIAL_AGE_YEARS
		* 365 * 24 * timeResolutionMinutes)
		/ (60 / timeResolutionMinutes * 24 * 365);
	// mge: with the given calculations fishes die of hunger very fast.
	// Thats why
	// I divided the energyConsumption by 24 (maybe forgot the division for
	// the day in the earlyer formula ?)
	energyConsumption = energyConsumption / 24;
	// if not enough energy for consumption in shortterm storage
	// transfer energy to shortterm storage from bodyFat, then
	// reproFraction, and last from bodyTissue
	// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87, von
	// bodyTissue=0.90 (Brett & Groves 1979)
	if (shorttermStorage < energyConsumption) {
	    if ((bodyFat + reproFraction) >= ((energyConsumption - shorttermStorage) / LOSS_FACTOR_FAT_TO_ENERGY)
		    && bodyFat < (energyConsumption - shorttermStorage)) {
		// mge: because Fat+Repro is bigger then consumption, this
		// calculates how much is needed from the reproFraction
		double energyFromRepro = (energyConsumption - shorttermStorage - (bodyFat / LOSS_FACTOR_FAT_TO_ENERGY))
			/ LOSS_FACTOR_TISSUE_TO_ENERGY;
		shorttermStorage += energyFromRepro
			* LOSS_FACTOR_REPRO_TO_ENERGY;
		reproFraction -= energyFromRepro; // mge: doesnt make sense
						  // here... repro could go
						  // negativ
		shorttermStorage += bodyFat * LOSS_FACTOR_FAT_TO_ENERGY;
		bodyFat = 0;
		shorttermStorage -= energyConsumption;
	    }
	    // if not enough energy for consumption in shortterm storage but
	    // enough in bodyFat, energy diff is metabolized from bodyFat only
	    else {
		double diff = energyConsumption - shorttermStorage;
		shorttermStorage += diff;
		// vom bodyFat muss mehr abgezogen werden due to verlust beim
		// metabolizieren
		bodyFat -= diff / LOSS_FACTOR_FAT_TO_ENERGY;
	    }

	}
	// enough energy for consumption in shortterm storage
	else {
	    shorttermStorage -= energyConsumption;
	}
	// PRODUCTION (Growth, reproduction is calculated extra)
	// if more energy in shortterm storgae then needed for metablism and it
	// exceeds shortterm storage maxCapcity
	// maxCapacity shortterm storage = 450*restingMetabolicRatePerTimestep
	// (nach Hauke) CHECK!!
	if (shorttermStorage > restingMetabolicRatePerTimestep * 450) {
	    double energySpillover = shorttermStorage
		    - restingMetabolicRatePerTimestep * 450;
	    shorttermStorage -= energySpillover;
	    // mge: Cant be reached because there is no mechanism that changes
	    // the growthState.
	    if ((growthState == GrowthState.ADULT_FEMALE)
		    && (reproFraction < biomass * MAX_REPRO_FRACTION
			    * ENERGY_PER_GRAM_REPRO)) {
		// => energy is transfered into bodycompartments with same
		// verlustfaktor wie beim metabolisieren zu energie
		// wenn female: zu 95% zu bodyTissue, zu 3.5% zu bodyFat, 1.5%
		// zu repro (f�r repro: following Wootton 1985)
		bodyFat += LOSS_FACTOR_FAT_TO_ENERGY * energySpillover * 0.035;
		bodyTissue += LOSS_FACTOR_TISSUE_TO_ENERGY * energySpillover
			* 0.95;
		reproFraction += LOSS_FACTOR_REPRO_TO_ENERGY * energySpillover
			* 0.015;
	    }
	    // wenn juvenile oder max capacity reproFraction f�r females
	    // erreicht
	    // mge: following seems to work correct.
	    else {
		// => energy is transfered into bodycompartments with same
		// verlustfaktor wie beim metabolisieren zu energie
		// zu 95% zu bodyTissue, zu 5% zu bodyFat according to body
		// composition
		bodyFat += LOSS_FACTOR_FAT_TO_ENERGY * energySpillover * 0.05;
		bodyTissue += LOSS_FACTOR_TISSUE_TO_ENERGY * energySpillover
			* 0.95;
	    }
	}

	// adjustment of virtual age
	// comparision of overall current energy at age vs. expected energy at
	// age (vBGF) to slow down growth (more realistic)
	// energy in gut (aufsummieren der GutContentElemente)
	currentGutContent = 0;
	for (Double d : gutStorageQueue) {
	    currentGutContent += d;
	}
	// sum of energy in all body compartments
	double currentEnergyWithoutRepro = bodyTissue + bodyFat
		+ shorttermStorage + currentGutContent;
	double expectedEnergyWithoutRepro = speciesDefinition.expectedEnergyWithoutRepro
		.interpolate(age);
	// daily: compare current growth with expected growth at age from vBGF +
	// ggf adjust virtual age + die of starvation, maxAge, naturalMortality
	if (steps % (60 / timeResolutionMinutes * 24) == 0) {
	    double maxAge = speciesDefinition.maxAgeInYrs
		    + random.nextGaussian();
	    // mge: check if the fish dies, split up to 3 different if-clauses.
	    // So you can
	    // see why the fish died
	    if (currentEnergyWithoutRepro < 0.6 * expectedEnergyWithoutRepro) {
		logger.finer(this + " starved to death.");
		this.die();
		return false;
	    }
	    if (age > maxAge) {
		logger.finer(this + " died of maximum Age");
		this.die();
		return false;
	    }
	    if ((speciesDefinition.mortalityRatePerYears / ((60 / timeResolutionMinutes * 24) * 365)) > random
		    .nextDouble()) {
		logger.finer(this + " died of Random Mortality");
		this.die();
		return false;
	    }
	}

	// adjust isHungry, REICHT DIE ABFRAGE AN DIESER STELLE UND ALLES
	// ABGEDECKT?
	// 1.abfrage = to limit overall growth, 2.abfrage to limit daily intake
	// mge: Changed to change the hunger state only when the current state
	// is different
	if ((currentEnergyWithoutRepro >= 0.95 * expectedEnergyWithoutRepro)
		|| (intakeForCurrentDay >= (speciesDefinition.maxDailyFoodRationA
			* biomass + speciesDefinition.maxDailyFoodRationB))) {

	    hungry = false;
	} else {

	    hungry = true;
	}

	return true;
    }

    /**
     * Updates biomass daily and size every week.
     * 
     * @param steps
     * @param timeResolutionMinutes
     */
    private void grow(long steps, int timeResolutionMinutes) {

	// update fish biomass (g wet weight)
	// conversion factor for shortterm and gut same as for tissue
	biomass = (bodyFat * CONVERSION_RATE_FAT)
		+ (bodyTissue + shorttermStorage + currentGutContent)
		* CONVERSION_RATE_TISSUE
		+ (reproFraction * CONVERSION_RATE_REPRO);

	// update fish size (SL in cm)
	if (steps % ((60 * 24 * 7) / timeResolutionMinutes) == 0) {
	    if (biomass > oldBiomassWeekly) {
		// W(g WW)=A*L(SL in cm)^B -> L=(W/A)^1/B
		double exp = 1 / speciesDefinition.lengthMassCoeffB;
		double base = biomass / speciesDefinition.lengthMassCoeffA;
		size = Math.pow(base, exp);
	    }
	    oldBiomassWeekly = biomass;
	}
    }

    /**
     * Create offspring.
     * 
     * @param schedule
     */
    private void reproduce(Schedule schedule) {

	double reproFractionOld = 0.0;
	double diffRepro = 0.0;
	// b=Anzahl offspring, damit jeder offspring (wenn mehr als 2)
	// initialisiert werden
	// DELAY factor von der Größe post-settlement age (zb 0.33 yrs
	// einfügen!!)
	for (int b = 0; b < speciesDefinition.nrOffspring; b++) {
	    // set biomass of offspring to initialSize/initialBiomass, VORSICHT
	    // wenn INITIAL VALUES GEändert werden!!
	    Fish offSpring = new Fish(oldpos, environment, speciesDefinition);

	    // schedule the new born to the next timeslot for initialization
	    schedule.scheduleRepeating(offSpring);
	}
	// biomass loss of parent fish due to reproduction effort:
	// CHECK STIMMT DAS SO???
	reproFractionOld = reproFraction;
	// all E taken from reproFraction! set ReproFraction back to minRepro
	// (following Wootton 1985)
	reproFraction = biomass * MIN_REPRO_FRACTION * ENERGY_PER_GRAM_REPRO;
	// substract loss in reproFraction from biomass
	// REIHENFOLGE zur berechnung von minRepro aus biomass nicht ganz
	// korrekt, CHECK MIT HAUKE!!
	diffRepro = reproFractionOld - reproFraction;
	biomass -= diffRepro * CONVERSION_RATE_REPRO;
    }

    /**
     * is called to take agent from lifeloop agent is taken from the 'simulation
     * world' and not scheduled again due to the architecture of the scheduler
     * remaining schedules of this agent can't be deleted immediately from the
     * schedule. hence some empty step calls might occur.
     */
    private void die() {
	lifeState = LifeState.DEAD;
	environment.getFishField().remove(this);
    }

    /**
     * @return age in years
     */
    public double getAge() {
	return age;
    }

    public boolean isHungry() {
	return hungry;
    }

    public double getIntakeForCurrentDay() {
	return intakeForCurrentDay;
    }

    public Queue<Double> getGutQueueInKJ() {
	return gutStorageQueue;
    }

    public double getGutContent() {
	return currentGutContent;
    }

    public String getShorttermStorage() {
	return (shorttermStorage + " kJ");
    }

    public String getBodyFatStr() {
	return (bodyFat + " kJ");
    }

    public double getBodyFat() {
	return bodyFat;
    }

    public String getBodyTissue() {
	return (bodyTissue + " kJ");
    }

    public String getReproFraction() {
	return (reproFraction + " kJ");
    }

    public double getBiomass() {
	return biomass;
    }

    public double getSize() {
	return size;
    }

    public GrowthState getGrowthState() {
	return growthState;
    }

    public Double2D getAttrCenterForaging() {
	return attrCenterForaging;
    }

    public Double2D getAttrCenterResting() {
	return attrCenterResting;
    }

    public Memory getMemory() {
	return memory;
    }

    public Collection<Double2D> getPosHistory() {
	return Collections.unmodifiableCollection(posHistory);
    }

    @Override
    public String toString() {
	return Fish.class.getSimpleName() + "[pos=" + pos + ", age=" + age
		+ "]";
    }

}
