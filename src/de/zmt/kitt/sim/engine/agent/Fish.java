package de.zmt.kitt.sim.engine.agent;

import java.util.*;
import java.util.logging.Logger;

import org.joda.time.*;

import sim.engine.*;
import sim.portrayal.Oriented2D;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.TimeOfDay;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.SpeciesDefinition;
import de.zmt.kitt.util.*;
import ec.util.MersenneTwisterFast;

/**
 * Fish implements the behavior of the fish<br />
 * 
 * @author oth
 * @author cmeyer
 */
public class Fish extends Agent implements Proxiable, Oriented2D {
    private static final double FEMALE_PROBABILITY = 0.5;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Fish.class.getName());

    private static final long serialVersionUID = 1L;

    /** Maximum size of position history */
    public static final int POS_HISTORY_MAX_SIZE = 10;

    /** Minimum repro fraction (of total body weight) */
    private static final double MIN_REPRO_FRACTION = 0.1;
    /** Maximum repro fraction (of total body weight) */
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

    // GROWTH AND AGE
    /** current bio mass of the fish (g wet weight) */
    private double biomass;
    /** store weekly bio mass update (g wet weight) */
    private double oldBiomassWeekly = 0;
    /** current size of fish as standard length (cm) */
    private double size;
    /** Age {@link Duration} */
    private Duration age;
    /** Date of fish birth in milliseconds */
    private Instant birthInstant;
    /** Fish growth stage indicating its ability to reproduce */
    private GrowthStage growthStage;

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

    @Override
    public void step(final SimState state) {
	Sim sim = (Sim) state;
	double dt = Environment.MINUTES_PER_STEP;
	long steps = sim.schedule.getSteps();

	switch (lifeState) {
	case INSTANTIATED:
	    // birth
	    this.birthInstant = environment.getTimeInstant();
	    lifeState = LifeState.ALIVE;
	    growthStage = GrowthStage.JUVENILE;
	    break;
	case ALIVE:
	    // DAILY UPDATES:
	    if (environment.isFirstStepInDay()) {
		//
		// PRODUCTION I (growth): update of BIOMASS (g wet weight) +
		// weekly update of SIZE (cm);
		grow(sim.random);

		// reset intake for the new day
		intakeForCurrentDay = 0;

		// PRODUCTION II (reproduction)
		/*
		 * spawn if lifeStage=female && repro zw. 20-30% of biomass &&
		 * currentEohneRepro >= 75% of expectedEohneRepro at age (nach
		 * Wootton 1985)
		 */
		// C. sordidus spawns on a daily basis without clear seasonal
		// pattern (McILwain 2009)
		// TODO Wahrscheinlichkeit von nur 5-10%, damit
		// nicht alle bei genau 20% reproduzieren, aber innerhalb der
		// nächsten Tagen wenn über 20%
		if (growthStage == GrowthStage.FEMALE
			&& (reproFraction >= (biomass * 0.2 * ENERGY_PER_GRAM_REPRO))) {
		    reproduce(sim.schedule);
		}

	    }

	    move(sim.random, dt, environment.getCurrentTimeOfDay());

	    if (hungry == true) {
		// CONSUMPTION (C)
		feed(dt);
	    }

	    // ENERGY BUDGET (RESPIRATION, R)
	    updateEnergy(sim.random, steps, dt);

	    break;
	case DEAD:

	    break;
	}

    }

    /**
     * Initiate velocity calculation and integration, making the fish move to a
     * new position. Position history is updated and activity costs are
     * calculated as well.
     * 
     * @param random
     * @param dt
     *            minutes passed between last and current step
     * @param timeOfDay
     */
    private void move(MersenneTwisterFast random, double dt, TimeOfDay timeOfDay) {
	velocity = calcVelocity(random, timeOfDay);
	integrateVelocity(dt);

	memory.increase(pos);
	posHistory.offer(pos);
	if (posHistory.size() >= POS_HISTORY_MAX_SIZE) {
	    posHistory.poll();
	}

	activityCosts = FormulaeUtil.netActivityCost(velocity.length(), dt);
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
	    TimeOfDay dielCycle) {
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
     * 
     * @param dt
     *            time passed in minutes between current and last step
     * @param velocity
     */
    private void integrateVelocity(double dt) {
	// multiply velocity with timeScale (minutes / step) and add it to pos
	MutableDouble2D newPosition = new MutableDouble2D(pos.add(velocity
		.multiply(dt)));

	// reflect on vertical border - invert horizontal velocity
	if (newPosition.x >= environment.getWidth() || newPosition.x < 0) {
	    newPosition.x = pos.x - velocity.x;
	}
	// reflect on horizontal border - invert vertical velocity
	if (newPosition.y >= environment.getHeight() || newPosition.y < 0) {
	    newPosition.y = pos.y - velocity.y;
	}

	// stay away from main land // TODO reflect by using normals
	if (environment.getHabitatOnPosition(new Double2D(newPosition)) == Habitat.MAINLAND) {
	    newPosition = new MutableDouble2D(pos);
	}

	pos = new Double2D(newPosition);
	environment.getFishField().setObjectLocation(this, pos);
    }

    // ////////////////CONSUMPTION///////////////////////////////////////
    // includes loss due to excretion/egestion/SDA)
    // food in g dry weight and fish in g wet weight!!
    private void feed(double dt) {
	double availableFood = environment.getFoodOnPosition(pos);
	// daily consumption rate = g food dry weight/g fish wet weight/day
	// multiplied with individual fish biomass and divided by time
	// resolution
	// only 12 of 24 h are considered relevant for food intake, so it is
	// divided by 12 not 24!
	double foodIntakePerStep = (speciesDefinition.getConsumptionRate()
		* biomass / 12 / 60)
		* dt;
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
		    .getGutTransitTime() / dt;

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

    private void updateEnergy(MersenneTwisterFast random, long steps, double dt) {
	// METABOLISM (RESPIRATION)
	double restingMetabolicRatePerTimestep = (RESTING_METABOLIC_RATE_A * Math
		.pow(biomass, RESTING_METABOLIC_RATE_B)) * 0.434 * dt / 60;
	// total energy consumption (RMR + activities)
	double energyConsumption = restingMetabolicRatePerTimestep
		+ activityCosts;
	// age = initial age plus duration from birth to now
	age = SpeciesDefinition.INITIAL_AGE.plus(new Duration(birthInstant,
		environment.getTimeInstant()));
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
	    if ((growthStage == GrowthStage.FEMALE)
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
		.getExpectedEnergyWithoutRepro().interpolate(age.getMillis());
	// daily: compare current growth with expected growth at age from vBGF +
	// ggf adjust virtual age + die of starvation, maxAge, naturalMortality
	if (environment.isFirstStepInDay()) {
	    if (currentEnergyWithoutRepro < 0.6 * expectedEnergyWithoutRepro) {
		logger.fine(this + " starved to death.");
		this.die();
	    } else if (age.compareTo(speciesDefinition.getMaxAge()) > 0) {
		logger.fine(this + " died of maximum Age");
		this.die();
	    } else if ((speciesDefinition.getMortalityRatePerYears() / ((60 / dt * 24) * 365)) > random
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
    private void grow(MersenneTwisterFast random) {

	// update fish biomass (g wet weight)
	// conversion factor for shortterm and gut same as for tissue
	biomass = (bodyFat * CONVERSION_RATE_FAT)
		+ (bodyTissue + shorttermStorage + currentGutContent)
		* CONVERSION_RATE_TISSUE
		+ (reproFraction * CONVERSION_RATE_REPRO);

	// WEEKLY: update fish size (SL in cm)
	if (environment.isFirstStepInWeek()) {
	    if (biomass > oldBiomassWeekly) {
		// W(g WW)=A*L(SL in cm)^B -> L=(W/A)^1/B
		double exp = 1 / speciesDefinition.getLengthMassCoeffB();
		double base = biomass / speciesDefinition.getLengthMassCoeffA();
		size = Math.pow(base, exp);
	    }
	    oldBiomassWeekly = biomass;
	}

	// Change of lifeStage if size appropriate
	if (growthStage == GrowthStage.JUVENILE
		&& size > speciesDefinition.getReproSize()) {
	    growthStage = random.nextBoolean(FEMALE_PROBABILITY) ? GrowthStage.FEMALE
		    : GrowthStage.MALE;
	}
    }

    /**
     * Create offspring.
     * 
     * @param schedule
     */
    private void reproduce(Schedule schedule) {
	// b=Anzahl offspring, damit jeder offspring (wenn mehr als 2)
	// initialisiert werden
	// DELAY factor von der Größe post-settlement age (zb 0.33 yrs
	// einfügen!!)
	for (int b = 0; b < speciesDefinition.getNumOffspring(); b++) {
	    // set biomass of offspring to initialSize/initialBiomass, VORSICHT
	    // wenn INITIAL VALUES GEändert werden!!
	    Fish offSpring = new Fish(oldpos, environment, speciesDefinition);

	    // schedule the new born to the next timeslot for initialization
	    Stoppable stoppable = schedule.scheduleRepeating(offSpring);
	    offSpring.setStoppable(stoppable);
	}
	// biomass loss of parent fish due to reproduction effort:
	// CHECK STIMMT DAS SO???
	double reproFractionOld = reproFraction;
	// all E taken from reproFraction! set ReproFraction back to minRepro
	// (following Wootton 1985)
	reproFraction = biomass * MIN_REPRO_FRACTION * ENERGY_PER_GRAM_REPRO;
	// substract loss in reproFraction from biomass
	// REIHENFOLGE zur berechnung von minRepro aus biomass nicht ganz
	// korrekt, CHECK MIT HAUKE!!
	double diffRepro = reproFractionOld - reproFraction;
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
	return new MyPropertiesProxy();
    }

    @Override
    public double orientation2D() {
	return velocity.angle();
    }

    public static enum GrowthStage {
	JUVENILE, FEMALE, MALE
    };

    /** Proxy class to define the properties displayed when inspected. */
    public class MyPropertiesProxy {
	// TODO output in meaningful format
	public Double2D getVelocity() {
	    return velocity;
	}

	public String getAge() {
	    return TimeUtil.FORMATTER.print(new Period(
		    Environment.START_INSTANT, age));
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

	public GrowthStage getGrowthStage() {
	    return growthStage;
	}

	public double getNetActivityCosts() {
	    return activityCosts;
	}
    }
}
