package de.zmt.kitt.sim.engine;

import static javax.measure.unit.SI.*;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import javax.measure.Measurable;
import javax.measure.quantity.Mass;

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
import de.zmt.kitt.util.MapUtil;
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
    private static final Duration STEP_DURATION_YODA = new Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND))
		    .getExactValue());
    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of agents */
    private final Continuous2D agentField;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatGrid;
    /** Stores normal vectors for habitat boundaries */
    private final ObjectGrid2D normalGrid;
    /** Stores amount of food for every location */
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
	    regrowth();
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
     * regrowth function: 9 mg algal dry weight per m2 and day<br>
     * 
     * @see "Adey & Goertemiller 1987", "Cliffton 1995"
     */
    private void regrowth() {
	for (int x = 0; x < foodGrid.getHeight(); x++) {
	    for (int y = 0; y < foodGrid.getWidth(); y++) {
		Habitat habitat = getHabitatOnPosition(new Double2D(y, x));
		double foodVal = foodGrid.get(y, x);

		foodVal = Math.max(habitat.getFoodMin(), foodVal);

		// TODO sig does change little on the result. needed?
		// TODO move to FormulaUtil
		double sig = 1 / (1 + Math.exp(-foodVal));
		double foodGrowth = foodVal * 0.2 * sig;
		foodVal += foodGrowth;

		foodVal = Math.min(habitat.getFoodMax(), foodVal);
		foodGrid.set(y, x, foodVal);
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
    public Habitat getHabitatOnPosition(Double2D position) {
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
	} while (getHabitatOnPosition(pos) != habitat);
	return pos;
    }

    /**
     * 
     * @param pos
     * @return amount of food on patch at given position in g dry weight
     */
    // TODO full amount differs from g/m^2 if 1px != 1m^2
    public Amount<Mass> getFoodOnPosition(Double2D pos) {
	return Amount.valueOf(foodGrid.get((int) pos.x, (int) pos.y), GRAM);
    }

    /**
     * Set amount of food at patch of given position.
     * 
     * @param pos
     * @param foodAmount
     *            dry weight, preferably in g
     * @throws IllegalArgumentException
     *             if {@code foodAmount} is negative
     */
    public void setFoodOnPosition(Double2D pos, Measurable<Mass> foodAmount) {
	double gramFood = foodAmount.doubleValue(GRAM);
	if (gramFood < 0) {
	    throw new IllegalArgumentException("Amount must be positive");
	}

	foodGrid.set((int) pos.x, (int) pos.y, gramFood);
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