package de.zmt.kitt.sim.engine.agent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import sim.engine.*;
import sim.portrayal.Oriented2D;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;

/**
 * Fish implements the behavior of the fish<br />
 * 
 * @author oth
 * @author cmeyer
 */
public class Fish extends Agent implements Proxiable, Oriented2D {
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

    // preferred habitats
    private static final Habitat RESTING_HABITAT = Habitat.CORALREEF;
    private static final Habitat FORAGING_HABITAT = Habitat.SEAGRASS;

    // MOVE
    /** Distance of full bias towards attraction center in m/PI */
    private static final double MAX_ATTRACTION_DISTANCE = 150 * Math.PI;
    private static final int CENTIMETERS_PER_METER = 100;

    // GROWTH AND AGE
    /** current bio mass of the fish (g wet weight) */
    private double biomass;
    /** store weekly bio mass update (g wet weight) */
    private double oldBiomassWeekly = 0;
    /** current size of fish as standard length (cm) */
    private double size;
    /** age in years */
    private double age;
    /** time step of fish initialization for age calculation */
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
    private Double2D attrCenterForaging = null;
    /** attraction center of habitat-dependent resting area */
    private Double2D attrCenterResting = null;
    /** velocity vector of agent (m/s) */
    private Double2D velocity = new Double2D();
    /** Energy costs for activities during the last step in kJ */
    private double activityCosts;

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

    private Stoppable stoppable;

    public Fish(Double2D pos, Environment environment,
	    SpeciesDefinition speciesDefinition) {
	super(pos);
	/** references to the environment to have access e.g. to the field */
	this.environment = environment;

	this.biomass = speciesDefinition.getInitialBiomass();
	this.oldBiomassWeekly = biomass;
	this.size = speciesDefinition.getInitialSize();

	// allocating initialBiomass to body compartments as E values (kJ)
	// wenn auch female fehlt noch reproFraction!!
	this.bodyFat = biomass * 0.05 * ENERGY_PER_GRAM_FAT;
	this.bodyTissue = biomass * 0.95 * ENERGY_PER_GRAM_TISSUE;

	this.speciesDefinition = speciesDefinition;

	// DEFINE STARTING CENTER OF ATTRACTIONS
	// find attraction centers for foraging and resting
	// (random, but only in preferred habitat type)
	if (speciesDefinition.isAttractionEnabled()) {
	    attrCenterForaging = environment
		    .getRandomHabitatPosition(FORAGING_HABITAT);
	    attrCenterResting = environment
		    .getRandomHabitatPosition(RESTING_HABITAT);
	}

	memory = new Memory(environment.getWidth(), environment.getHeight());
    }

    /**
     * called every time when its taken from queue in the scheduler lifeloop of
     * the agent
     */
    @Override
    public void step(final SimState state) {
	Sim sim = (Sim) state;
	int timeScale = sim.getParams().environmentDefinition.getTimeScale();
	long steps = sim.schedule.getSteps();

	switch (lifeState) {
	case INSTANTIATED:
	    // birth
	    this.birthTimeStep = steps;
	    lifeState = LifeState.ALIVE;
	    break;
	case ALIVE:
	    move(sim.random, timeScale, sim.schedule.getSteps());

	    if (hungry == true) {
		// CONSUMPTION (C)
		feed(timeScale);
	    }

	    // ENERGY BUDGET (RESPIRATION, R)
	    updateEnergy(sim.random, steps, timeScale);

	    // DAILY UPDATES:
	    if (steps % (60 / timeScale * 24) == 0) {
		//
		// PRODUCTION I (growth): update of BIOMASS (g wet weight) +
		// weekly update of SIZE (cm);
		grow(steps, timeScale);

		// reset intake for the new day
		intakeForCurrentDay = 0;

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

	    }

	    break;
	case DEAD:

	    break;
	}

    }

    /**
     * agent's movement in one step with previously determined moveMode
     */
    private void move(MersenneTwisterFast random, int timeScale, long steps) {
	DielCycle dielCycle = DielCycle
		.getDielCycle((steps * timeScale / 60) % 24);
	velocity = calcVelocity(random, dielCycle);
	integrateVelocity(timeScale, velocity);

	memory.increase(pos);

	posHistory.offer(pos);
	if (posHistory.size() >= POS_HISTORY_MAX_SIZE) {
	    posHistory.poll();
	}

	// net activity costs per hour (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142
	// Korsmeyer et al., 2002
	double activityCostsPerHour = 1.193 * Math.pow(velocity.length()
		* CENTIMETERS_PER_METER, 1.66) / 0.0142;
	activityCosts = activityCostsPerHour / TimeUnit.HOURS.toMinutes(1)
		* timeScale;
    }

    /**
     * Calculate velocity to migrate towards attraction center with reducing
     * focus the closer the fish is, gradually changing to pure random walk
     * within the preferred area.
     * <p>
     * If {@link SpeciesDefinition#isAttractionEnabled()} is set to
     * <code>false</code> velocity for random walk will always be returned.
     * 
     * @param random
     * @param dielCycle
     * @return velocity
     */
    private Double2D calcVelocity(MersenneTwisterFast random,
	    DielCycle dielCycle) {
	double baseSpeed = dielCycle.isDay() ? speciesDefinition.getDaySpeed()
		: speciesDefinition.getNightSpeed();

	Double2D towardsAttraction;

	Double2D randomVelocity = new Double2D(random.nextGaussian(),
		random.nextGaussian()).normalize().multiply(baseSpeed);
	Double2D deviation = new Double2D(random.nextGaussian(),
		random.nextGaussian()).multiply(speciesDefinition
		.getSpeedDeviation());
	Double2D randomWalk = randomVelocity.add(deviation);

	if (speciesDefinition.isAttractionEnabled()) {
	    double distance;
	    Double2D attractionDir;

	    if (dielCycle.isForageTime()) {
		distance = pos.distance(attrCenterForaging);
		attractionDir = attrCenterForaging.subtract(pos).normalize();
	    } else {
		distance = pos.distance(attrCenterResting);
		attractionDir = attrCenterResting.subtract(pos).normalize();
	    }

	    // will to migrate towards attraction
	    // tanh function to reduce bias as the fish moves closer
	    double willToMigrate = Math
		    .tanh(distance / MAX_ATTRACTION_DISTANCE);

	    // weight directed and random walk according to willToMigrate
	    towardsAttraction = attractionDir.multiply(willToMigrate
		    * baseSpeed);
	    randomWalk = randomWalk.multiply(1 - willToMigrate);
	} else {
	    // pure random walk
	    towardsAttraction = new Double2D();
	}
	return towardsAttraction.add(randomWalk);
    }

    /**
     * Integrates velocity by adding it to position and reflect from obstacles.
     * The field is updated with the new position as well.
     */
    private void integrateVelocity(int timeScale, Double2D velocity) {
	// multiply velocity with timeScale (minutes / step) and add it to pos
	MutableDouble2D newPosition = new MutableDouble2D(pos.add(velocity
		.multiply(timeScale)));

	// reflect on vertical border - invert horizontal velocity
	if (newPosition.x >= environment.getWidth() || newPosition.x < 0) {
	    newPosition.x = pos.x - velocity.x;
	}
	// reflect on horizontal border - invert vertical velocity
	if (newPosition.y >= environment.getHeight() || newPosition.y < 0) {
	    newPosition.y = pos.y - velocity.y;
	}

	// reflect on main land using boundary normal
	if (environment.getHabitatOnPosition(new Double2D(newPosition)) == Habitat.MAINLAND) {
	    // find penetration position
	    newPosition = new MutableDouble2D(pos.subtract(velocity));
	}

	pos = new Double2D(newPosition);
	environment.getFishField().setObjectLocation(this, pos);
    }

    // ////////////////CONSUMPTION///////////////////////////////////////
    // includes loss due to excretion/egestion/SDA)
    // food in g dry weight and fish in g wet weight!!
    private void feed(int timeScale) {
	double availableFood = environment.getFoodOnPosition(pos);
	// daily consumption rate = g food dry weight/g fish wet weight/day
	// multiplied with individual fish biomass and divided by time
	// resolution
	// only 12 of 24 h are considered relevant for food intake, so it is
	// divided by 12 not 24!
	double foodIntakePerStep = (speciesDefinition.getConsumptionRate()
		* biomass / 12 / 60)
		* timeScale;
	// even if fish could consume more, just take the available food on grid
	foodIntakePerStep = (foodIntakePerStep > availableFood) ? availableFood
		: foodIntakePerStep;

	// energy intake (kJ) = amount of food ingested (g dry weight)*energy
	// content of food (kJ/g food dry weight)
	double energyIntake = foodIntakePerStep
		* speciesDefinition.getEnergyContentFood();
	// in g algal dry weight
	intakeForCurrentDay += foodIntakePerStep;

	if ((energyIntake <= ((speciesDefinition.getMaxDailyFoodRationA()
		* biomass + speciesDefinition.getMaxDailyFoodRationB()) * speciesDefinition
		.getEnergyContentFood())) && (energyIntake > 0.0)) {

	    // after queueSize steps the energyIntake flows to the shortterm
	    double delayForStorageInSteps = speciesDefinition
		    .getGutTransitTime() / timeScale;

	    gutStorageQueue.offer(energyIntake);
	    // wenn transit time (entspricht queue size) reached => E geht in
	    // shortterm storage
	    if (gutStorageQueue.size() >= delayForStorageInSteps) {
		// gutStorageQueue.poll entnimmt jeweils 1. element und löscht
		// es damit aus queue
		shorttermStorage += speciesDefinition.getNetEnergy()
			* gutStorageQueue.poll();
	    }
	}
	// update the amount of food on current food cell
	environment.setFoodOnPosition(pos, availableFood - foodIntakePerStep);
    }

    private void updateEnergy(MersenneTwisterFast random, long steps,
	    int timeScale) {
	// METABOLISM (RESPIRATION)
	double restingMetabolicRatePerTimestep = (RESTING_METABOLIC_RATE_A * Math
		.pow(biomass, RESTING_METABOLIC_RATE_B))
		* 0.434
		* timeScale
		/ 60;
	// total energy consumption (RMR + activities)
	double energyConsumption = restingMetabolicRatePerTimestep
		+ activityCosts;
	// substract birthtimestep from current timestep+add
	// TODO intialAgeInDays(=>vorher in timesteps umrechnen!)
	age = (steps - birthTimeStep + SpeciesDefinition.INITIAL_AGE_YEARS
		* 365 * 24 * timeScale)
		/ (60 / timeScale * 24 * 365);
	// mge: with the given calculations fishes die of hunger very fast.
	// Thats why
	// I divided the energyConsumption by 24 (maybe forgot the division for
	// the day in the earlyer formula ?)
	energyConsumption /= 24;
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
	double expectedEnergyWithoutRepro = speciesDefinition
		.getExpectedEnergyWithoutRepro().interpolate(age);
	// daily: compare current growth with expected growth at age from vBGF +
	// ggf adjust virtual age + die of starvation, maxAge, naturalMortality
	if (steps % (60 / timeScale * 24) == 0) {
	    double maxAge = speciesDefinition.getMaxAgeInYrs()
		    + random.nextGaussian();
	    // mge: check if the fish dies, split up to 3 different if-clauses.
	    // So you can
	    // see why the fish died
	    if (currentEnergyWithoutRepro < 0.6 * expectedEnergyWithoutRepro) {
		logger.fine(this + " starved to death.");
		this.die();
	    }
	    if (age > maxAge) {
		logger.fine(this + " died of maximum Age");
		this.die();
	    }
	    if ((speciesDefinition.getMortalityRatePerYears() / ((60 / timeScale * 24) * 365)) > random
		    .nextDouble()) {
		logger.fine(this + " died of random Mortality");
		this.die();
	    }
	}

	// adjust isHungry, REICHT DIE ABFRAGE AN DIESER STELLE UND ALLES
	// ABGEDECKT?
	// 1.abfrage = to limit overall growth, 2.abfrage to limit daily intake
	// mge: Changed to change the hunger state only when the current state
	// is different
	if ((currentEnergyWithoutRepro >= 0.95 * expectedEnergyWithoutRepro)
		|| (intakeForCurrentDay >= (speciesDefinition
			.getMaxDailyFoodRationA() * biomass + speciesDefinition
			    .getMaxDailyFoodRationB()))) {

	    hungry = false;
	} else {
	    hungry = true;
	}

    }

    /**
     * Updates biomass daily and size every week.
     * 
     * @param steps
     * @param timeScale
     */
    private void grow(long steps, int timeScale) {

	// update fish biomass (g wet weight)
	// conversion factor for shortterm and gut same as for tissue
	biomass = (bodyFat * CONVERSION_RATE_FAT)
		+ (bodyTissue + shorttermStorage + currentGutContent)
		* CONVERSION_RATE_TISSUE
		+ (reproFraction * CONVERSION_RATE_REPRO);

	// update fish size (SL in cm)
	if (steps % ((60 * 24 * 7) / timeScale) == 0) {
	    if (biomass > oldBiomassWeekly) {
		// W(g WW)=A*L(SL in cm)^B -> L=(W/A)^1/B
		double exp = 1 / speciesDefinition.getLengthMassCoeffB();
		double base = biomass / speciesDefinition.getLengthMassCoeffA();
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
	for (int b = 0; b < speciesDefinition.getNumOffspring(); b++) {
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
     * Remove object from schedule and fish field.
     */
    private void die() {
	if (stoppable != null) {
	    stoppable.stop();
	} else {
	    logger.warning(this
		    + " could not remove itself from the schedule: "
		    + "No stoppable set.");
	}
	lifeState = LifeState.DEAD;
	environment.getFishField().remove(this);
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

    public void setStoppable(Stoppable stoppable) {
	this.stoppable = stoppable;
    }

    @Override
    public String toString() {
	return Fish.class.getSimpleName() + "[pos=" + pos + ", age=" + age
		+ "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyProxy();
    }

    @Override
    public double orientation2D() {
	return velocity.angle();
    }

    /** Proxy class to define the properties displayed when inspected. */
    public class MyProxy {
	public Double2D getVelocity() {
	    return velocity;
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

	public Collection<Double> getGutStorageQueue() {
	    return Collections.unmodifiableCollection(gutStorageQueue);
	}

	public double getCurrentGutContent() {
	    return currentGutContent;
	}

	public double getShorttermStorage() {
	    return shorttermStorage;
	}

	public double getBodyFat() {
	    return bodyFat;
	}

	public double getBodyTissue() {
	    return bodyTissue;
	}

	public double getReproFraction() {
	    return reproFraction;
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

	public double getNetActivityCosts() {
	    return activityCosts;
	}
    }
}
