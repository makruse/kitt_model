package de.zmt.kitt.sim.engine.agent;

import java.util.*;
import java.util.logging.Logger;

import sim.engine.*;
import sim.field.grid.IntGrid2D;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.*;
import ec.util.MersenneTwisterFast;

/**
 * Fish implements the behaviour of the fish<br />
 * 
 * @author oth
 */
public class Fish extends Agent {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Fish.class.getName());
    //mge: stuff to have a more precise logging:
    private int finestPrintFreq = 10;
    private boolean finestPrintOnUpdateEnergyOn = false; 
    
    private static final long serialVersionUID = 1L;
    
    //mge: the frequency how often the FinestPrints get printed (at least for the updateEnergy)


    /** reference to agent specification - which species */
    private final SpeciesDefinition speciesDefinition;

    // GROWTH AND AGE
    /** current biomass of the fish (g wet weight) */
    private double biomass;
    /** store weekly biomass update (g wet weight) */
    protected double oldBiomassWeekly = 0;
    /** current size of fish as standard length (cm) */
    protected double size;
    /**
     * calculated age (years) depending on actual energy intake based on vBGF
     * (daily update in the evening)
     */
    protected double virtualAgeDifference = 0;
    /** timestep of fish initialisation for age calculation */
    protected double birthTimeStep;

    // BODY COMPARTMENTS
    /** holds the latest incomes of food energy in gut (kJ) */
    protected Queue<Double> gutStorageQueue = new LinkedList<Double>();
    /** shortterm storage of energy (kJ) */
    protected double shorttermStorage = 0.0;
    /** energy in gut (kJ) */
    protected double currentGutContent = 0;
    /** current amount of body bodyFat of fish (kJ) */
    protected double bodyFat = 0;
    /** current amount of body tissue (kJ) */
    protected double bodyTissue = 0;
    /** current amount of repro fraction (kJ) */
    protected double reproFraction = 0;
    /** min capacity of repro fraction for spawning: 10% of total body weight */
    protected double minReproFraction = 0.1;
    /** max capacity of repro fraction: 30% of total body weight */
    protected double maxReproFraction = 0.3;

    // ENERGY METABOLISM
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    static public final double restingMetabolicRateA = 0.0072;
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    static public final double restingMetabolicRateB = 0.79;
    /** Verlustfaktor bei flow shortterm storage <=> bodyFat */
    static public final double VerlustfaktorFatToEnergy = 0.87;
    /** Verlustfaktor bei flow shortterm storage <=> bodyTissue */
    static public final double VerlustfaktorTissueToEnergy = 0.90;
    /** Verlustfaktor bei flow shortterm storage <=> reproFraction */
    static public final double VerlustfaktorReproToEnergy = 0.87;
    // energy-biomass conversions
    /** metabolizable energy (kJ) from 1 g bodyFat */
    static public final double energyPerGramFat = 36.3;
    /** metabolizable energy (kJ) from 1 g bodyTissue */
    static public final double energyPerGramTissue = 6.5;
    /** metabolizable energy (kJ) from 1 g reproFraction (ovaries) */
    static public final double energyPerGramRepro = 23.5;
    /** 1 kJ fat*conversionRate = fat in g */
    static public final double conversionRateFat = 0.028;
    /** 1 kJ bodyTissue*conversionRate = body tissue in g */
    static public final double conversionRateTissue = 0.154;
    /** 1 kJ repro*conversionRate = repro in g */
    static public final double conversionRateRepro = 0.043;

    // FEEDING AND ENERGY BUDGET
    /** g dry weight food/m2 */
    protected double availableFood = 0;
    /** g dry weight food per fish */
    protected double foodIntake = 0;
    /** energy intake (kJ) */
    protected double energyIntake = 0;
    /** energy consumption at zero speed (kJ) */
    protected double restingMetabolicRatePerTimestep = 0;
    /**
     * energy demand of fish (difference between expectedE and currentE at age
     * in kJ)
     */
    protected double hunger = 0;
    /** hunger state of the fish */
    protected boolean isHungry = true;
    /** ueber den tag aufsummierter energy intake (kJ) */
    protected double intakeForCurrentDay = 0;
    /** current energy value of fish (all body compartments) in kJ */
    protected double currentEnergyTotal = 0;
    /** current energy value of fish (all except Repro) in kJ */
    protected double currentEnergyWithoutRepro = 0;
    /** expected energy value of fish based on vBGF in kJ */
    protected double expectedEnergyWithoutRepro = 0;

    // MOVING
    /** center of habitat-dependent foraging area */
    public Double2D centerOfAttrForaging = new Double2D();
    /** center of habitat-dependent resting area */
    public Double2D centerOfAttrResting = new Double2D();
    /** steplength in cm per time step */
    double step = 0; // check unit!
    /**
     * holds the current factor for turning angle depending on the last action
     * decision
     */
    protected double turningFactor; // wozu?
    /**
     * holds the current factor for steplength depending on the last action
     * decision
     */
    protected double stepLengthFactor; // wozu?
    /** mode of moving (resting, foraging or migrating) */
    protected MoveMode currentMoveMode;
    /** current speed of agent in x- and y-direction (unit?) */
    protected double xSpeed, ySpeed, zSpeed; // unit?
    /** current speed in cm per timestep */
    protected double currentSpeed = 0.0; // stimmt unit?
    /** net activity costs (kJ) for current timestep */
    protected double netActivityCosts = 0.0;

    // REPRODUCTION
    /** each fish starts as a post-settlement juvenile */
    // nicht besonders clever, typical pop sturcture sollte start sein!!
    // dann auch initSize/biomass und lifestage anpassen!!
    /** life stage of fish (juvenile, female or male) */
    protected GrowthState growthState = GrowthState.JUVENILE;

    /** holds the parameters that are read from xml file */
    protected final ModelParams params;
    /** references to the environment to have access e.g. to the field */
    protected final Environment environment;

    /**
     * the memory of fish's locations. for each cell a counter is increased when
     * the fish has shown up there
     */
    public IntGrid2D memField;

    public Queue<Double2D> history = new LinkedList<Double2D>();
    static final protected int HISTORY_SIZE = 10;

    /**
     * @param x
     *            initial x Position
     * @param y
     *            initial y Position
     * @param b
     *            initial biomass
     * @param environment
     *            in which agent runs
     */
    public Fish(final double x, final double y, double initialBiomass,
	    double initialSize, Environment environment, ModelParams params,
	    SpeciesDefinition speciesDefinition) {
	super(new Double2D(x, y));
	/** references to the environment to have access e.g. to the field */
	this.environment = environment;
	this.params = params;

	this.id = currentIdAssignment++;
	this.pos = new Double2D(x, y);
	this.oldpos = pos;

	this.biomass = initialBiomass;
	this.oldBiomassWeekly = biomass;
	this.size = initialSize;

	// allocating initialBiomass to body compartments as E values (kJ)
	// wenn auch female fehlt noch reproFraction!!
	this.bodyFat = biomass * 0.05 * energyPerGramFat;
	this.bodyTissue = biomass * 0.95 * energyPerGramTissue;
	this.size = speciesDefinition.initialSize;

	this.currentMoveMode = MoveMode.FORAGING;
	this.speciesDefinition = speciesDefinition;

	initCentersOfAttraction();

	memField = new IntGrid2D(Environment.MEM_CELLS_X,
		Environment.MEM_CELLS_Y);

	/*
	 * System.out.print(bodyFat); System.out.println();
	 * System.out.print(bodyTissue); System.out.println();
	 * System.out.print(reproFraction); System.out.println();
	 */
    }

    /**
     * is called by the first step() call initializes the agent and schedules
     * agent for lifeloop
     */
    private void birth(Schedule schedule) {
	this.birthTimeStep = schedule.getSteps();

	lifeState = LifeState.ALIVE;
	// put agent into the environment field
	environment.getFishField().setObjectLocation(this,
		new Double2D(pos.x, pos.y));

	schedule.scheduleRepeating(this);
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
     * called every time when its taken from queue in the scheduler lifeloop of
     * the agent
     */
    @Override
    public void step(final SimState state) {
	Sim sim = (Sim) state;
	if (sim.schedule.getSteps() % 100 == 0 && (this.id == this.environment.getFishField().getAllObjects().size() - 1)) {
		logger.finer("Step Number: " + sim.schedule.getSteps() + " Control Message every 100 Steps (Bound to the youngest Fish). If it's shown twice, the fishes are scheduled twice.");
	}
	switch (lifeState) {
	case INSTANTIATED:
	    birth(sim.schedule);
	    break;
	case ALIVE:
	    move(sim.random);
	    // memory fading
	    // memField.multiply(0.5);

	    // biomass=biomass+ 0.2 * biomass; //wo kommt das her?
	     if(isHungry==true){
	     // CONSUMPTION (C)
	     feed();
	     }

//	     ENERGY BUDGET (RESPIRATION, R)
	     if(updateEnergy(sim)==false){
	    	 logger.finer(this + "died");
	     }
	    
	       // DAILY UPDATES:
	      if(state.schedule.getSteps() % (60/getTimeResInMinutes()*24) ==
	      0) {
//	      
	      // PRODUCTION I (growth): update of BIOMASS (g wet weight) +
	      //weekly update of SIZE (cm); 
	    	  grow();
	      
	      // Change of lifeStage if size appropriate double
//	      currentSize=giveSize(); // // size frame = variation? wie macht
	      //man das mit 50% levels? // double
//	      sizeFrame=0.3*random.nextGaussian(); //warum 0.3?? //
//	      if((currentSize > (speciesDefinition.reproSize-sizeFrame)) ||
//	      (currentSize < (speciesDefinition.reproSize+sizeFrame))) { // has a probability at this point of 1/(365days/7days) ??? //
//	      if(random.nextDouble() > (1.0/(365.0/7.0))) { //
//	      lifeStage=LifeStage.FEMALE; // } // } // aber nur zu 50% !!
//	      if(currentSize > (speciesDefinition.reproSize)) {
//	      lifeStage=LifeStage.FEMALE; }
	      
	      
	      //PRODUCTION II (reproduction) // spawn if lifeStage=female &&
	      //repro zw. 20-30% of biomass && currentEohneRepro >= 75% of
	      //expectedEohneRepro at age (nach Wootton 1985) // C. sordidus
	      //spawns on a daily basis without clear seasonal pattern (McILwain
	      //2009) // HINZUFÜGEN: Wahrscheinlichkeit von nur 5-10%, damit
	      //nicht alle bei genau 20% reproduzieren, aber innerhalb der
	      //nächsten Tagen wenn über 20% if(lifeStage==LifeStage.FEMALE &&
//	      (reproFraction >= (biomass*0.2*energyPerGramRepro))) {
//	      reproduce(); }
	      
	    	  //mge: needs to set the intake Back for every Day
	    	  intakeForCurrentDay=0;
	      
	      }
	     
	    break;
		case DEAD:

	    break;
	}

    }

    /**
     * searches a randomly drawn position in suitable habitat for resting and
     * feeding
     */
    public void initCentersOfAttraction() {

	// DEFINE STARTING CENTER OF ATTRACTIONS (ONCE PER FISH LIFE)
	// find suitable point for center of attraction for foraging
	// (random, but only in preferred habitattype)
	Double2D pos;
	do {
	    pos = environment.getRandomFieldPosition();
	    // hier abfrage nach habitat preference von spp def?
	} while (environment.getHabitatOnPosition(pos) != HabitatHerbivore.SEAGRASS);

	centerOfAttrForaging = new Double2D(pos.x, pos.y);
	// centerOfAttrForaging=new Double2D(246,112);

	// find suitable point for center of attraction for resting (depending
	// on habitat preference)
	do {
	    pos = environment.getRandomFieldPosition();

	} while (environment.getHabitatOnPosition(pos) != HabitatHerbivore.CORALREEF);

	centerOfAttrResting = new Double2D(pos.x, pos.y);
	// centerOfAttrResting=new Double2D(112,603);
    }

    // calculate vector from current position to center of attraction
    protected Double2D getDirectionToAttraction(Double2D currentPos,
	    Double2D pointOfAttraction) {

	double dirX = pointOfAttraction.x - currentPos.x;
	double dirY = pointOfAttraction.y - currentPos.y;
	double length = Math.sqrt((dirX * dirX) + (dirY * dirY));

	double nx = dirX / length;
	double ny = dirY / length;
	return new Double2D(nx, ny);
    }

    protected double getDistance(Double2D currentPos, Double2D pointOfAttraction) {

	double dirX = pointOfAttraction.x - currentPos.x;
	double dirY = pointOfAttraction.y - currentPos.y;
	return Math.sqrt((dirX * dirX) + (dirY * dirY));
    }

    /**
     * agent's movement in one step with previously determined moveMode
     */
    // TODO clean up
    protected void move(MersenneTwisterFast random) {

	// remember last position
	oldpos = pos;

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
	if (dielCycle == DielCycle.SUNRISE
		|| (environment.getHabitatOnPosition(pos) == HabitatHerbivore.SEAGRASS)) {
	    dist = getDistance(pos, centerOfAttrForaging);
	    attractionDir = getDirectionToAttraction(pos, centerOfAttrForaging);
	} else if (dielCycle == DielCycle.SUNSET
		|| (environment.getHabitatOnPosition(pos) == HabitatHerbivore.CORALREEF)) {
	    dist = getDistance(pos, centerOfAttrResting);
	    attractionDir = getDirectionToAttraction(pos, centerOfAttrResting);
	}
	// probability to migrate to attraction is calulated by tanh
	double probMigration = Math.tanh(dist / scal);

	// step = cm per time step (stimmt das mit cm??) => abhängig von
	// size(bodylength), cell resolution und time resolution
	// step=6; //size/params.environmentDefinition.cellResolution *
	// speciesDefinition.stepMigration * 60 *
	// params.environmentDefinition.timeResolutionMinutes;

	// migrating -biased correlated random walk when sunrise and far from
	// attraction
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

	    HabitatHerbivore targetHabitat = environment
		    .getHabitatOnPosition(new Double2D(pos.x + newXSpeed, pos.y
			    + newYSpeed));
	    if (targetHabitat == HabitatHerbivore.MAINLAND) {
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
			    && (environment.getHabitatOnPosition(posCandidate) == HabitatHerbivore.SEAGRASS)
			    && (environment.getHabitatOnPosition(posCandidate) != HabitatHerbivore.MAINLAND)) {
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
		    if ((environment.getHabitatOnPosition(posCandidate) == HabitatHerbivore.CORALREEF)
			    && (environment.getHabitatOnPosition(posCandidate) != HabitatHerbivore.MAINLAND)) {
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

	if ((newX > environment.getFieldWidth() || newX < 0)
		|| environment.getHabitatOnPosition(new Double2D(newX, newY)) == HabitatHerbivore.MAINLAND) {
	    xSpeed = -xSpeed;
	    newX = pos.x + xSpeed;
	    // if vector intersects border then reflect at border
	    // Double2D reflected = Vec.reflectVector(new
	    // Double2D(xSpeed,ySpeed), new Double2D(0,1));
	}
	if ((newY > environment.getFieldHeight() || newY < 0)
		|| environment.getHabitatOnPosition(new Double2D(newX, newY)) == HabitatHerbivore.MAINLAND) {
	    ySpeed = -ySpeed;
	    newY = pos.y + ySpeed;
	    // if vector intersects border then reflect at border
	}

	pos = new Double2D(newX, newY);

	environment.getFishField().setObjectLocation(this, pos);

	Int2D cell = environment.getMemFieldCell(pos);
	int histo = memField.get(cell.x, cell.y);
	memField.set(cell.x, cell.y, (histo + 1));

	history.offer(pos);
	if (history.size() >= HISTORY_SIZE) {
	    history.poll();
	}

	// feeding if day
	if (dielCycle == DielCycle.DAY) {
	    availableFood = environment.getFoodOnPosition(pos);
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
	currentSpeed = step
		/ (60 * params.environmentDefinition.timeResolutionMinutes);
	// net costs per timestep = 1.193*speed pro sec^1.66*oxicaloric
	// value/60*timeResolution
	//mge : 300000 ersetzt f�r Test s --> Sterben sollt vern�nftig gehen
	// gilt so nur für parrots, gibts was allgemein gültiges??   (0.0142)
	netActivityCosts = (1.193 * Math.pow(currentSpeed, 1.66)) * 30000 / 60
		* params.environmentDefinition.timeResolutionMinutes;
    }

    // ////////////////CONSUMPTION///////////////////////////////////////
    // includes loss due to excretion/egestion/SDA)
    // food in g dry weight and fish in g wet weight!!
    public void feed() {
	// get the amount of food on current patch of foodField in g dry
	// weight/m2
	availableFood = getFoodAt(pos.x, pos.y);
	// daily consumption rate = g food dry weight/g fish wet weight*day
	// multiplied with individual fish biomass and divided by time
	// resolution
	// only 12 of 24 h are considered relevant for food intake, daher
	// divided by 12 not 24!
	double consumptionRatePerTimeStep = (speciesDefinition.consumptionRate
		* biomass / 12 / 60)
		* params.environmentDefinition.timeResolutionMinutes;
	// food intake in g food dry weight
	foodIntake = consumptionRatePerTimeStep;
	// even if fish could consume more, just take the available food on grid
	foodIntake = (foodIntake > availableFood) ? availableFood : foodIntake;

	// energy intake (kJ) = amount of food ingested (g dry weight)*energy
	// content of food (kJ/g food dry weight)
	energyIntake = foodIntake * speciesDefinition.energyContentFood;
	// in g algal dry weight
	intakeForCurrentDay += foodIntake;
	if (intakeForCurrentDay >= speciesDefinition.maxDailyFoodRationA
		* biomass + speciesDefinition.maxDailyFoodRationB) {
	    logger.finer("Fish " + this.id + " : not any more Hungry");
	    isHungry = false;
	}

	if ((energyIntake <= ((speciesDefinition.maxDailyFoodRationA * biomass + speciesDefinition.maxDailyFoodRationB) * speciesDefinition.energyContentFood))
		&& (energyIntake > 0.0)) {

	    // after queueSize steps the energyIntake flows to the shortterm
	    double delayForStorageInSteps = speciesDefinition.gutTransitTime
		    / params.environmentDefinition.timeResolutionMinutes;

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
	// update the amount of food on current foodcell
	setFoodAt(pos.x, pos.y, availableFood - foodIntake);
    }

    // ///////////////ENERGY
    // BUDGET////////////////////////////////////////////////////////////////
    // returns false if fish dies due to maxAge, starvation or naturalMortality
    private boolean updateEnergy(Sim sim) {
    	if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1) && finestPrintOnUpdateEnergyOn) {
//    		mge: info for debugging. DeepWatch. 
    		logger.finest("STEP NUMBER: " +sim.schedule.getSteps() + " Fish id 1, growth State is " + this.growthState + ", reproFraction should only exist when it is an Adult Female.");
    		logger.finest("All important Values to begin of the updateEnergy method:");
    		logger.finest("ShorttermStorage: " + shorttermStorage + ", BodyFat: " + bodyFat + ", BodyTissue: " + bodyTissue + ", ReproFraction(Should only exist when the fish is female): " + reproFraction);
			logger.finest("Next Block.");
    	}
	// METABOLISM (RESPIRATION)
	restingMetabolicRatePerTimestep = (restingMetabolicRateA * Math.pow(
		biomass, restingMetabolicRateB))
		* 0.434
		* params.environmentDefinition.timeResolutionMinutes / 60;
	// total energy consumption (RMR + activities)
	double energyConsumption = restingMetabolicRatePerTimestep
		+ netActivityCosts;
	//mge: with the given calculations fishes die of hunger very fast. Thats why  
	// I divided the energyConsumption by 24 (maybe forgot the division for the day in the earlyer formula ?)
	energyConsumption = energyConsumption / 24;
	if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//		mge: info for debugging. DeepWatch. 
		logger.finest("The energyConsumption is " + energyConsumption + ", set together out of restingMetabolicRatePerTimestep: " + restingMetabolicRatePerTimestep + 
		", and netActivityCosts: " + netActivityCosts);
		logger.finest("Next Block.");
	}

	// if not enough energy for consumption in shortterm storage
	// transfer energy to shortterm storage from bodyFat, then
	// reproFraction, and last from bodyTissue
	// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87, von
	// bodyTissue=0.90 (Brett & Groves 1979)
	if (shorttermStorage < energyConsumption) {
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: give out the energyConsumption Values all 300 Steps so it doesnt get spammed.
			logger.finest("Shortterm below the EnergyConsumption, next the decisions he has to take are to where get the Energy from.");
			logger.finest("Next Block.");
		}
	    // not enough in bodyFat AND reproFraction => metabolise energy from
	    // ALL 3 body compartments 
		// mge: changed the if clause from scratch, if that was wrong, this is the old: 
//	    if ((bodyFat < (shorttermStorage - energyConsumption)
//		    / VerlustfaktorFatToEnergy)
//		    && (reproFraction < (energyConsumption - shorttermStorage - energyConsumption) 
//			    / VerlustfaktorFatToEnergy)) {
		if ((bodyFat + reproFraction) <= (energyConsumption - shorttermStorage)  && finestPrintOnUpdateEnergyOn) 
		{
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)) {
//				mge: info for debugging. DeepWatch.
				logger.finest("Energy gets taken from Body Tissue, repro and bodyFat");
				logger.finest("BodyFat and repro gets placed at 0, but it shoul all went into the energy Consumption");
				logger.finest("Next Block.");
			}
	    double energyFromProtein = (energyConsumption
			- shorttermStorage
			- (bodyFat / VerlustfaktorFatToEnergy) - (reproFraction / VerlustfaktorReproToEnergy))
			/ VerlustfaktorTissueToEnergy;
		shorttermStorage += energyFromProtein
			* VerlustfaktorTissueToEnergy;
		bodyTissue -= energyFromProtein;
		shorttermStorage += bodyFat * VerlustfaktorFatToEnergy;
		shorttermStorage -= energyConsumption;
		bodyFat = 0;
		reproFraction = 0;
	    }
	    // transfer energy to shortterm storage from bodyFat and then from
	    // reproFraction
	    // verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87
		//mge: following is reached when bodyFat and repro could pay the energy costs
		// but bodyFat alone isnt enough
	    // mge: old if clause: else if (bodyFat < (shorttermStorage - energyConsumption)
		else if((bodyFat + reproFraction) >= ((energyConsumption - shorttermStorage)
		    / VerlustfaktorFatToEnergy) && bodyFat < (energyConsumption - shorttermStorage)) {
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//				mge: info for debugging. DeepWatch.
				logger.finest("If bodyFat and Repro is enough to fulfill energyConsumption this gets started. Should get logical amount from reproFraction in my opinion");
				logger.finest("The reproFraction is reduced but its not sure if there is any or if the fish is female...");
				logger.finest("ReproFraction is: " + reproFraction);
			}
		//mge: because Fat+Repro is bigger then consumption, this calculates how much is needed from the reproFraction
		double energyFromRepro = (energyConsumption - shorttermStorage - (bodyFat / VerlustfaktorFatToEnergy))
			/ VerlustfaktorTissueToEnergy;
		shorttermStorage += energyFromRepro
			* VerlustfaktorReproToEnergy;
		reproFraction -= energyFromRepro;  //mge: doesnt make sense here... repro could go negativ 
		shorttermStorage += bodyFat * VerlustfaktorFatToEnergy;
		bodyFat = 0; 
		shorttermStorage -= energyConsumption; 
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("When this Block is reached, bodyFat will be 0, shorttermstore should be also 0 and reproFraction should reduced.");
			logger.finest("ShorttermStoreage: " + shorttermStorage + "Taken from Repro: " + energyFromRepro + "Repro After that: " + reproFraction);
			logger.finest("Next Block.");
		}
	    }
	    // if not enough energy for consumption in shortterm storage but
	    // enough in bodyFat, energy diff is metabolized from bodyFat only
	    else {
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
	//				mge: info for debugging. DeepWatch.
				logger.finest("Energy gets taken from bodyFat and Shorttermstorage");
				logger.finest("BodyFat before: " + bodyFat + " shorttermstorage before: " + shorttermStorage);
			}	
			double diff = energyConsumption - shorttermStorage;
			shorttermStorage += diff;
			// vom bodyFat muss mehr abgezogen werden due to verlust beim
			// metabolizieren
			bodyFat -= diff / VerlustfaktorFatToEnergy;
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)) {
	//				mge: info for debugging. DeepWatch.
				logger.finest("BodyFat after: " + bodyFat + " shorttermstorage after: " + shorttermStorage);
			}	
	    }

	}
	// enough energy for consumption in shortterm storage
	else {
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("The final Else clause, which should only be reached if in ShorttermStorage is enough energy");
			logger.finest("Shortterm befor EnergyConsumption: " + shorttermStorage);
		}
	    shorttermStorage -= energyConsumption;
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1) && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("Shortterm after EnergyConsumption: " + shorttermStorage);
			logger.finest("Next Block.");
		}	
	}
	if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1) && finestPrintOnUpdateEnergyOn) {
//		mge: info for debugging. DeepWatch.
		logger.finest("values after the energy Consumption, the next thing happening is reducing of the shorttermstore, and production into BodyFat etc. ");
		logger.finest("ShorttermStorage: " + shorttermStorage + ", BodyFat: " + bodyFat + ", BodyTissue: " + bodyTissue + ", ReproFraction(Should only exist when the fish is female): " + reproFraction);
		logger.finest("Next Block");
	}

	
	// PRODUCTION (Growth, reproduction is calculated extra)
	// if more energy in shortterm storgae then needed for metablism and it
	// exceeds shortterm storage maxCapcity
	// maxCapacity shortterm storage = 450*restingMetabolicRatePerTimestep
	// (nach Hauke) CHECK!!
	if (shorttermStorage > restingMetabolicRatePerTimestep * 450) {
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("The Shorttermstorage was over its maximum and gets now digestet into Body Fat");
			logger.finest("Shortterm before: " + shorttermStorage);
			logger.finest("maxShortterm: " + (restingMetabolicRatePerTimestep * 450));
		}
	    double energySpillover = shorttermStorage
		    - restingMetabolicRatePerTimestep * 450;
	    shorttermStorage -= energySpillover;
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("The Shorttermstorage was over its maximum and gets now digestet into Body Fat");
			logger.finest("Shortterm After spillover: " + shorttermStorage);
			logger.finest("Spillover that gets insertet to BodyFat etc.: " + energySpillover);
		}
	    //mge: Cant be reached because there is no mechanism that changes the growthState.
	    if ((growthState == GrowthState.ADULT_FEMALE)
		    && (reproFraction < biomass * maxReproFraction
			    * energyPerGramRepro)) {
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//				mge: info for debugging. DeepWatch.
				logger.finest("Should not be reached, and if it is reached the Fisch is a grown female");
			}
		// => energy is transfered into bodycompartments with same
		// verlustfaktor wie beim metabolisieren zu energie
		// wenn female: zu 95% zu bodyTissue, zu 3.5% zu bodyFat, 1.5%
		// zu repro (f�r repro: following Wootton 1985)
		bodyFat += VerlustfaktorFatToEnergy * energySpillover * 0.035;
		bodyTissue += VerlustfaktorTissueToEnergy * energySpillover
			* 0.95;
		reproFraction += VerlustfaktorReproToEnergy * energySpillover
			* 0.015;
	    }
	    // wenn juvenile oder max capacity reproFraction f�r females
	    // erreicht
	    //mge: following seems to work correct. 
	    else {
			if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//				mge: info for debugging. DeepWatch.
				logger.finest("EnergySpillover for males, females with to much repro and JUVENILES");
				logger.finest("Body Fat before spillover gets inserted: " + bodyFat + "bodyTissue Before: " + bodyTissue);
			}
		// => energy is transfered into bodycompartments with same
		// verlustfaktor wie beim metabolisieren zu energie
		// zu 95% zu bodyTissue, zu 5% zu bodyFat according to body
		// composition
		bodyFat += VerlustfaktorFatToEnergy * energySpillover * 0.05;
		bodyTissue += VerlustfaktorTissueToEnergy * energySpillover
			* 0.95;
		if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//			mge: info for debugging. DeepWatch.
			logger.finest("Body Fat after spillover got inserted: " + bodyFat + "bodyTissue Before: " + bodyTissue);
			logger.finest("Next Block");
		}
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
	currentEnergyWithoutRepro = bodyTissue + bodyFat + shorttermStorage
		+ currentGutContent;
	expectedEnergyWithoutRepro = speciesDefinition.expectedEnergyWithoutRepro
		.interpolate(giveAge() - virtualAgeDifference);
	if (sim.schedule.getSteps() % finestPrintFreq == 0 && (this.id == 1)  && finestPrintOnUpdateEnergyOn) {
//		mge: info for debugging. DeepWatch.
		logger.finest("Before the age adjustment and the check if the fish dies.");
		logger.finest("The currentEnergyWithoutRepro (tissue,fat and shorttermstore): " + currentEnergyWithoutRepro + ", and the expectedEnergy gets calculated (complicated calculation with age): " + expectedEnergyWithoutRepro);
	}

	// daily: compare current growth with expected growth at age from vBGF +
	// ggf adjust virtual age + die of starvation, maxAge, naturalMortality
	if (sim.schedule.getSteps() % (60 / getTimeResInMinutes() * 24) == 0) {
	    double maxAge = speciesDefinition.maxAgeInYrs
		    + sim.random.nextGaussian();
	    
	    if ((expectedEnergyWithoutRepro - currentEnergyWithoutRepro) > 10) {

		// das funktioniert so nicht! abfrage dreht sich im kreis!!
		double virtualAge = giveAge(); // asymLenghtsL*(1-
					       // Math.pow(Math.E,-growthCoeffB*(age+ageAtTimeZero)));
		double diff = giveAge() - virtualAge;
		virtualAgeDifference += diff;
	    }
	    //mge: check if the fish dies, split up to 3 different if-clauses. So you can
//	    see why the fish died
	    if (currentEnergyWithoutRepro < 0.6 * expectedEnergyWithoutRepro) {
		logger.finer("Fish " + this.id + " died of Hunger");
	    	this.die();
	    	return false;
	    }
		if(maxAge <= giveAge()) {
		logger.finer("Fish " + this.id + " died of maximum Age");
	    	this.die();
	    	return false;
		}
		if ((speciesDefinition.mortalityRatePerYears / ((60/getTimeResInMinutes()*24) * 365)) > sim.random
			    .nextDouble()) {
		logger.finer("Fish " + this.id + " died of Random Mortality");
	    	this.die();
	    	return false;
	    }
	}

	// adjust isHungry, REICHT DIE ABFRAGE AN DIESER STELLE UND ALLES
	// ABGEDECKT?
	// 1.abfrage = to limit overall growth, 2.abfrage to limit daily intake
	// mge: Changed to change the hunger state only when the current state is different
	if ((currentEnergyWithoutRepro >= 0.95 * expectedEnergyWithoutRepro)
		|| (intakeForCurrentDay >= (speciesDefinition.maxDailyFoodRationA
			* biomass + speciesDefinition.maxDailyFoodRationB))) {

	    isHungry = false;
	} else {

	    isHungry = true;
	}

	return true;
    }

    // ///////////////GROWTH////////////////////////////////////////
    // called daily to update biomass, size only weekly
    public void grow() {

//	 update fish biomass (g wet weight)
//	 conversion factor for shortterm and gut same as for tissue
//	biomass = (bodyFat * conversionRateFat)
//		+ (bodyTissue + shorttermStorage + currentGutContent)
//		* conversionRateTissue + (reproFraction * conversionRateRepro);

	// update fish size (SL in cm)
//	if ((environment.getCurrentTimestep() % ((60 * 24 * 7) / environment
//		.getTimeRes())) == 0) {
//	    if (biomass > oldBiomassWeekly) {
//		// W(g WW)=A*L(SL in cm)^B -> L=(W/A)^1/B
//		double exp = 1 / speciesDefinition.lengthMassCoeffB;
//		double base = biomass / speciesDefinition.lengthMassCoeffA;
//		size = Math.pow(base, exp);
//		// for testing
//		//logger.fine("biomass: " + biomass);
//		//logger.fine("size: " + size);
//	    }
//	    oldBiomassWeekly = biomass;
//	}
    }

    // ///////////////REPRODUCTION////////////////////////////////////////
    protected void reproduce(Schedule schedule) {

	double reproFractionOld = 0.0;
	double diffRepro = 0.0;
	// b=Anzahl offspring, damit jeder offspring (wenn mehr als 2)
	// initialisiert werden
	// DELAY factor von der Größe post-settlement age (zb 0.33 yrs
	// einfügen!!)
	for (int b = 0; b < speciesDefinition.nrOffspring; b++) {
	    // set biomass of offspring to initialSize/initialBiomass, VORSICHT
	    // wenn INITIAL VALUES GEändert werden!!
	    Fish offSpring = new Fish(oldpos.x, oldpos.y,
		    (speciesDefinition.initialBiomass),
		    speciesDefinition.initialSize, environment, params,
		    speciesDefinition);
	    // guide the new born in the same direction as the reflected parent
	    offSpring.dx = this.dx;
	    offSpring.dy = this.dy;

	    // schedule the new born to the next timeslot for initialization
	    schedule.scheduleRepeating(offSpring);
	}
	// biomass loss of parent fish due to reproduction effort:
	// CHECK STIMMT DAS SO???
	reproFractionOld = reproFraction;
	// all E taken from reproFraction! set ReproFraction back to minRepro
	// (following Wootton 1985)
	reproFraction = biomass * minReproFraction * energyPerGramRepro;
	// substract loss in reproFraction from biomass
	// REIHENFOLGE zur berechnung von minRepro aus biomass nicht ganz
	// korrekt, CHECK MIT HAUKE!!
	diffRepro = reproFractionOld - reproFraction;
	biomass = biomass - diffRepro * conversionRateRepro;
    }

    // age in years!!
    // currenttimestep -birthtimestep converted to years
    public double giveAge() {
	// substract birthtimestep from current timestep+add
	// intialAgeInDays(=>vorher in timesteps umrechnen!)
	return (environment.getCurrentTimestep() + 1 - birthTimeStep + SpeciesDefinition.initialAgeInYrs
		* 365 * 24 * environment.getTimeRes())
		/ (60 / params.environmentDefinition.timeResolutionMinutes * 24 * 365);
    }

    public double getFoodAt(double x, double y) {

	return environment.getFoodField().get((int) x, (int) y);
    }

    public void setFoodAt(double x, double y, double val) {

	environment.getFoodField().set((int) pos.x, (int) pos.y,
		availableFood - foodIntake);
    }

    public int getTimeResInMinutes() {

	return params.environmentDefinition.timeResolutionMinutes;
    }

    public long getIdent() {
	return id;
    }

    public String getAge() {
	return (giveAge() + " years");
    }

    public String getActivityCosts() {
	return (netActivityCosts + " kJ");
    }

    public boolean isHungry() {
	return isHungry;
    }

    public String getAvailableFood() {
	return (availableFood + " g DW");
    }

    public String getFoodIntake() {
	return (foodIntake + " g DW");
    }

    public String getIntakeForCurrentDay() {
	return (intakeForCurrentDay + " g DW");
    }

    public String getEnergyIntake() {
	return (energyIntake + " kJ");
    }

    public Queue<Double> getGutQueueInKJ() {
	return gutStorageQueue;
    }

    public String getGutContent() {
	return (currentGutContent + " kJ");
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

    public String desBiomass() {
	return "in g WW";
    }

    public String getSize() {
	return (size + " SL in cm");
    }

    public GrowthState getLifeStage() {
	return growthState;
    }

    public String getCurrentEnergyStr() {
	return (currentEnergyTotal + " kJ");
    }

    public double getCurrentEnergy() {
	return (currentEnergyTotal);
    }

    public String getExpextedtEnergy() {
	return (expectedEnergyWithoutRepro + " kJ");
    }

    public double getVirtualAgeDifference() {
	return virtualAgeDifference;
    }

    public String getRMR() {
	return (restingMetabolicRatePerTimestep + " kJ per timestep");
    }
}
