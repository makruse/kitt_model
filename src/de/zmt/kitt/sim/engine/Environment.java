package de.zmt.kitt.sim.engine;

import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.SI.*;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.joda.time.*;
import org.jscience.physics.amount.Amount;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.TimeOfDay;
import de.zmt.kitt.sim.display.KittGui.GuiPortrayable;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.agent.fish.Fish;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.AreaDensity;
import de.zmt.sim.engine.params.def.ParameterDefinition;
import de.zmt.sim.portrayal.portrayable.ProvidesPortrayable;
import ec.util.MersenneTwisterFast;

/**
 * Class for storing fields of simulation data and managing time.
 * 
 * @author cmeyer
 * 
 */
public class Environment implements Steppable,
	ProvidesPortrayable<GuiPortrayable>, Proxiable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Environment.class
	    .getName());
    private static final long serialVersionUID = 1L;

    /** Converted {@link EnvironmentDefinition#STEP_DURATION} to yoda format */
    private static final org.joda.time.Duration STEP_DURATION_YODA = new org.joda.time.Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND))
		    .getExactValue());
    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of agents */
    private final Continuous2D agentField;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatGrid;
    /** Stores normal vectors for habitat boundaries */
    private final ObjectGrid2D normalGrid;
    /** Stores amount of <b>available</b> food for every location */
    private final DoubleGrid2D foodGrid;
    /** {@link MutableDateTime} for storing simulation time */
    private final MutableDateTime dateTime;

    private final MersenneTwisterFast random;
    /**
     * Save map scale, could be changed by user during simulation and cause
     * errors.
     */
    private final double mapScale;

    private final MyPropertiesProxy proxy = new MyPropertiesProxy();

    public Environment(MersenneTwisterFast random, KittParams params,
	    Schedule schedule) {
	this.random = random;

	BufferedImage mapImage = MapUtil.loadMapImage(KittSim.DEFAULT_INPUT_DIR
		+ params.getEnvironmentDefinition().getMapImageFilename());
	this.mapScale = params.getEnvironmentDefinition().getMapScale();

	this.habitatGrid = MapUtil.createHabitatGridFromMap(random, mapImage);
	this.normalGrid = MapUtil.createNormalGridFromHabitats(habitatGrid);
	this.agentField = new Continuous2D(FIELD_DISCRETIZATION,
		mapImage.getWidth() / mapScale, mapImage.getHeight() / mapScale);
	this.foodGrid = MapUtil.createFoodFieldFromHabitats(habitatGrid,
		random, mapScale);

	this.dateTime = new MutableDateTime(EnvironmentDefinition.START_INSTANT);

	addSpeciesFromDefinitions(params.getSpeciesDefs(), schedule);
    }

    /**
     * Adds species to {@link #fishField} according to {@link SpeciesDefinition}
     * s found in {@link Params}.
     */
    private void addSpeciesFromDefinitions(
	    Collection<SpeciesDefinition> speciesDefs, Schedule schedule) {
	// creating the fishes
	for (SpeciesDefinition speciesDefinition : speciesDefs) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		Double2D pos = generateRandomHabitatPosition(Habitat.CORALREEF);
		Agent fish = new Fish(pos, this, speciesDefinition, random);

		addAgent(fish, schedule);
	    }
	}
    }

    /**
     * Schedules agent, sets stoppable, adds to field and increment count.
     * 
     * @param agent
     * @param schedule
     * @param identifier
     *            agents with the same identifier are totaled
     */
    public void addAgent(Agent agent, Schedule schedule) {
	Stoppable stoppable = schedule.scheduleRepeating(agent);
	agent.setStoppable(stoppable);

	agentField.setObjectLocation(agent, agent.getPosition());
	proxy.incrementAgentCount(agent.getDefinition(), 1);
    }

    /**
     * Removes agent from field and decrement its count.
     * 
     * @param agent
     * @param identifier
     *            agents with the same identifier are totaled
     */
    public void removeAgent(Agent agent) {
	agentField.remove(agent);
	proxy.incrementAgentCount(agent.getDefinition(), -1);
    }

    /** Contains update methods of the environment e.g. growth of the seagrass */
    @Override
    public void step(SimState state) {
	dateTime.add(Environment.STEP_DURATION_YODA);

	updateFieldPositions();

	// DAILY UPDATES:
	if (isFirstStepInDay()) {
	    growFood(Amount.valueOf(1, DAY));
	}
    }

    /**
     * Updates field positions of all agents from {@link Agent#getPosition()}.
     */
    private void updateFieldPositions() {
	for (Object obj : agentField.allObjects) {
	    if (obj instanceof Agent) {
		agentField.setObjectLocation(obj, ((Agent) obj).getPosition());
	    }
	}
    }

    /**
     * Let algae grow for whole food grid.<br>
     * <b>NOTE:</b> Computationally expensive.
     * 
     * @param delta
     */
    private void growFood(Amount<Duration> delta) {
	for (int y = 0; y < foodGrid.getHeight(); y++) {
	    for (int x = 0; x < foodGrid.getWidth(); x++) {
		Double2D position = new Double2D(x, y);
		Habitat habitat = obtainHabitat(position);

		// total food density is the available plus minimum
		Amount<AreaDensity> totalFoodDensity = obtainFoodDensity(
			position).plus(habitat.getFoodDensityMin());

		Amount<AreaDensity> grownFoodDensity = FormulaUtil.growAlgae(
			totalFoodDensity, habitat.getFoodDensityMax(), delta)
			.minus(habitat.getFoodDensityMin());

		setFoodDensity(position, grownFoodDensity);
	    }
	}
    }

    /**
     * 
     * @return true if current step is the first of the day.
     */
    public boolean isFirstStepInDay() {
	return dateTime.getMillisOfDay() < Environment.STEP_DURATION_YODA
		.getMillis();
    }

    /**
     * 
     * @return true if current step is the first of the week, i.e. the first of
     *         Monday.
     */
    public boolean isFirstStepInWeek() {
	return dateTime.getDayOfWeek() == DateTimeConstants.MONDAY
		&& isFirstStepInDay();
    }

    public TimeOfDay getTimeOfDay() {
	return TimeOfDay.timeFor(dateTime.getHourOfDay());
    }

    /**
     * 
     * @param position
     * @return {@link Habitat} on given position
     */
    public Habitat obtainHabitat(Double2D position) {
	// habitat is different from field size if mapScale != 1
	return Habitat.values()[habitatGrid.get((int) (position.x * mapScale),
		(int) (position.y * mapScale))];
    }

    /**
     * 
     * @param habitat
     * @return Random position in given habitat
     */
    public Double2D generateRandomHabitatPosition(Habitat habitat) {
	Double2D pos;
	do {
	    pos = new Double2D(random.nextDouble() * getWidth(),
		    random.nextDouble() * getHeight());
	} while (obtainHabitat(pos) != habitat);
	return pos;
    }

    /**
     * 
     * @param position
     * @return available food density on patch at given position in g dry weight
     *         per square meter
     */
    // TODO full amount differs from g/m^2 if 1px != 1m^2
    public Amount<AreaDensity> obtainFoodDensity(Double2D position) {
	return Amount.valueOf(foodGrid.get((int) position.x, (int) position.y),
		UnitConstants.FOOD_DENSITY);
    }

    /**
     * Places available food density at patch of given position.
     * 
     * @param position
     * @param foodDensity
     *            dry weight, preferably in g/m2
     * @throws IllegalArgumentException
     *             if food density is negative or exceeds maximum
     */
    public void placeFoodDensity(Double2D position,
	    Amount<AreaDensity> foodDensity) {
	Habitat habitat = obtainHabitat(position);
	if (foodDensity.getEstimatedValue() < 0) {
	    throw new IllegalArgumentException("foodDensity(" + foodDensity
		    + ") is negative.");
	} else if (foodDensity.isGreaterThan(habitat.getFoodDensityRange())) {
	    throw new IllegalArgumentException("foodDensity (" + foodDensity
		    + ") is beyond maximum (" + habitat.getFoodDensityRange()
		    + ").");
	}

	setFoodDensity(position, foodDensity);
    }

    private void setFoodDensity(Double2D position,
	    Amount<AreaDensity> foodDensity) {
	double gramFood = foodDensity.doubleValue(UnitConstants.FOOD_DENSITY);
	foodGrid.set((int) position.x, (int) position.y, gramFood);
    }

    /** @return field width in meters */
    public double getWidth() {
	return agentField.getWidth();
    }

    /** @return field height in meters */
    public double getHeight() {
	return agentField.getHeight();
    }

    @Override
    public GuiPortrayable providePortrayable() {
	return new MyPortrayable();
    }

    @Override
    public Object propertiesProxy() {
	return proxy;
    }

    public class MyPortrayable implements GuiPortrayable {

	@Override
	public Continuous2D getAgentField() {
	    return agentField;
	}

	@Override
	public DoubleGrid2D getFoodGrid() {
	    return foodGrid;
	}

	@Override
	public IntGrid2D getHabitatGrid() {
	    return habitatGrid;
	}

	@Override
	public ObjectGrid2D getNormalGrid() {
	    return normalGrid;
	}
    }

    public class MyPropertiesProxy implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<ParameterDefinition, Integer> agentCounts = new HashMap<ParameterDefinition, Integer>();

	private Integer incrementAgentCount(ParameterDefinition definition,
		int increment) {
	    int count = agentCounts.containsKey(definition) ? agentCounts
		    .get(definition) : 0;
	    int incrementedCount = count + increment;

	    if (incrementedCount > 0) {
		agentCounts.put(definition, incrementedCount);
	    } else {
		// count is zero, remove group from map
		agentCounts.remove(definition);
	    }
	    return count;
	}

	public double getWidth() {
	    return Environment.this.getWidth();
	}

	public double getHeight() {
	    return Environment.this.getHeight();
	}

	public Map<ParameterDefinition, Integer> getAgentCounts() {
	    return agentCounts;
	}

	public Period getTime() {
	    return new Period(EnvironmentDefinition.START_INSTANT, dateTime);
	}

	public TimeOfDay getTimeOfDay() {
	    return Environment.this.getTimeOfDay();
	}
    }
}