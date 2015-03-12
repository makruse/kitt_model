package de.zmt.kitt.sim.engine;

import static javax.measure.unit.SI.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.*;

import javax.imageio.ImageIO;
import javax.measure.Measurable;
import javax.measure.quantity.Mass;

import org.joda.time.*;
import org.jscience.physics.amount.Amount;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.TimeOfDay;
import de.zmt.kitt.sim.engine.agent.fish.Fish;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.util.MapUtil;
import ec.util.MersenneTwisterFast;

/**
 * Class for storing fields of simulation data and managing time.
 * 
 * @author cmeyer
 * 
 */
public class Environment implements Steppable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Environment.class
	    .getName());
    private static final long serialVersionUID = 1L;

    /** Converted {@link EnvironmentDefinition#STEP_DURATION} to yoda format */
    private static final Duration STEP_DURATION_YODA = new Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND))
		    .getExactValue());
    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of fish */
    private final Continuous2D fishField;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatGrid;
    /** Stores normal vectors for habitat boundaries */
    private final ObjectGrid2D normalGrid;
    /** Stores amount of food for every location */
    private final DoubleGrid2D foodGrid;
    /** {@link MutableDateTime} for storing simulation time */
    private final MutableDateTime dateTime;

    private final KittSim sim;
    private final EnvironmentDefinition envDef;
    /**
     * Save map scale, could be changed by user during simulation and cause
     * errors.
     */
    private final double mapScale;

    public Environment(KittSim sim) {
	this.sim = sim;
	this.envDef = sim.getParams().getEnvironmentDefinition();

	BufferedImage mapImage = loadMapImage(KittSim.DEFAULT_INPUT_DIR
		+ envDef.getMapImageFilename());
	this.mapScale = envDef.getMapScale();

	this.habitatGrid = MapUtil.createHabitatGridFromMap(sim.random,
		mapImage);
	this.normalGrid = MapUtil.createNormalGridFromHabitats(habitatGrid);
	this.fishField = new Continuous2D(FIELD_DISCRETIZATION,
		mapImage.getWidth() / mapScale, mapImage.getHeight() / mapScale);
	this.foodGrid = MapUtil.createFoodFieldFromHabitats(habitatGrid,
		sim.random, mapScale);

	this.dateTime = new MutableDateTime(EnvironmentDefinition.START_INSTANT);
	addSpeciesFromDefinitions(sim.random);
    }

    private BufferedImage loadMapImage(String imagePath) {
	BufferedImage mapImage = null;
	logger.fine("Loading map image from " + imagePath);
	try {
	    mapImage = ImageIO.read(new File(imagePath));
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Could not load map image from "
		    + imagePath);
	}
	return mapImage;
    }

    /**
     * Adds species to {@link #fishField} according to {@link SpeciesDefinition}
     * s found in {@link Params}.
     */
    private void addSpeciesFromDefinitions(MersenneTwisterFast random) {
	// creating the fishes
	for (SpeciesDefinition speciesDefinition : sim.getParams()
		.getSpeciesDefs()) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		Double2D pos = getRandomHabitatPosition(Habitat.CORALREEF);
		Fish fish = new Fish(pos, this, speciesDefinition, random);

		Stoppable stoppable = sim.schedule.scheduleRepeating(fish);
		fish.setStoppable(stoppable);
	    }
	}
    }

    /**
     * contains update methods of the environment e.g. growth of the seagrass
     */
    @Override
    public void step(SimState state) {
	dateTime.add(Environment.STEP_DURATION_YODA);

	// DAILY UPDATES:
	if (isFirstStepInDay()) {
	    regrowth();
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

    /**
     * 
     * @return Current time instant in simulation
     */
    public Instant getTimeInstant() {
	return dateTime.toInstant();
    }

    public TimeOfDay getCurrentTimeOfDay() {
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

    public Double2D getRandomFieldPosition() {
	double x = sim.random.nextDouble() * getWidth();
	double y = sim.random.nextDouble() * getHeight();
	return new Double2D(x, y);
    }

    /**
     * 
     * @param habitat
     * @return Random position in given habitat
     */
    public Double2D getRandomHabitatPosition(Habitat habitat) {
	Double2D pos;
	do {
	    pos = new Double2D(sim.random.nextDouble() * getWidth(),
		    sim.random.nextDouble() * getHeight());
	} while (getHabitatOnPosition(pos) != habitat);
	return pos;
    }

    /** @return field width in meters */
    public double getWidth() {
	return fishField.getWidth();
    }

    /** @return field height in meters */
    public double getHeight() {
	return fishField.getHeight();
    }

    public Continuous2D getFishField() {
	return fishField;
    }

    public DoubleGrid2D getFoodGrid() {
	return foodGrid;
    }

    public IntGrid2D getHabitatGrid() {
	return habitatGrid;
    }

    public ObjectGrid2D getNormalGrid() {
	return normalGrid;
    }
}