package de.zmt.kitt.sim.engine;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.*;

import javax.imageio.ImageIO;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.*;
import de.zmt.kitt.util.MapUtil;

public class Environment implements Steppable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Environment.class
	    .getName());

    private static final long serialVersionUID = 1L;

    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of fish */
    private final Continuous2D fishField;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatField;
    /** Stores amount of food for every location */
    private final DoubleGrid2D foodField;

    private final Sim sim;
    private final EnvironmentDefinition envDef;
    /**
     * Save map scale, could be changed by user during simulation and cause
     * errors.
     */
    private final double mapScale;

    public Environment(Sim sim) {
	this.sim = sim;
	this.envDef = sim.getParams().environmentDefinition;

	BufferedImage mapImage = loadMapImage(Sim.DEFAULT_INPUT_DIR
		+ envDef.getMapImageFilename());
	this.mapScale = envDef.getMapScale();

	this.habitatField = MapUtil.createHabitatFieldFromMap(sim.random,
		mapImage);
	this.fishField = new Continuous2D(FIELD_DISCRETIZATION,
		mapImage.getWidth() / mapScale, mapImage.getHeight() / mapScale);
	this.foodField = MapUtil.createFoodFieldFromHabitats(habitatField,
		sim.random, mapScale);

	addSpeciesFromDefinitions();
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
    private void addSpeciesFromDefinitions() {
	// creating the fishes
	for (SpeciesDefinition speciesDefinition : sim.getParams()
		.getSpeciesDefs()) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		Double2D pos = getRandomHabitatPosition(Habitat.CORALREEF);
		Fish fish = new Fish(pos, this, speciesDefinition);

		sim.schedule.scheduleRepeating(fish);
	    }
	}
    }

    /**
     * contains update methods of the environment e.g. growth of the seagrass
     */
    @Override
    public void step(SimState state) {
	// DAILY UPDATES:
	if (sim.schedule.getSteps() % (60 / envDef.getTimeScale() * 24) == 0) {

	    // regrowth function: 9 mg algal dry weight per m2 and day!!
	    // nach Adey & Goertemiller 1987 und Cliffton 1995
	    for (int x = 0; x < foodField.getHeight(); x++) {
		for (int y = 0; y < foodField.getWidth(); y++) {
		    Habitat habitat = getHabitatOnPosition(new Double2D(y, x));

		    double foodVal = foodField.get(y, x);
		    foodVal = Math.max(habitat.getFoodMin(), foodVal);

		    // TODO sig does change little on the result. needed?
		    double sig = 1 / (1 + Math.exp(-foodVal));
		    double foodGrowth = foodVal * 0.2 * sig;
		    foodVal += foodGrowth;

		    foodVal = Math.min(habitat.getFoodMax(), foodVal);
		    foodField.set(y, x, foodVal);
		}
	    }
	}
    }

    /**
     * 
     * @param position
     * @return {@link Habitat} on given position
     */
    public Habitat getHabitatOnPosition(Double2D position) {
	try {
	    // habitat is different from field size if mapScale != 1
	    return Habitat.values()[habitatField.get(
		    (int) (position.x * mapScale),
		    (int) (position.y * mapScale))];
	} catch (IndexOutOfBoundsException e) {
	    logger.log(Level.WARNING,
		    "Index out of bounds should not happen. Fix wrong call", e);
	    return null;
	}
    }

    /**
     * 
     * @param pos
     * @return amount of food on given position in g dry weight/m^2
     */
    public double getFoodOnPosition(Double2D pos) {
	try {
	    return foodField.get((int) pos.x, (int) pos.y);
	} catch (IndexOutOfBoundsException e) {
	    logger.log(Level.WARNING,
		    "Index out of bounds should not happen. Fix wrong call", e);
	    return 0;
	}
    }

    /**
     * Set food at given position.
     * 
     * @param pos
     * @param foodVal
     *            in g dry weight/m^2
     */
    public void setFoodOnPosition(Double2D pos, double foodVal) {
	try {
	    foodField.set((int) pos.x, (int) pos.y, foodVal);
	} catch (IndexOutOfBoundsException e) {
	    logger.log(Level.WARNING,
		    "Index out of bounds should not happen. Fix wrong call", e);
	}
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

    /** Field width in meters */
    public double getWidth() {
	return fishField.getWidth();
    }

    /** Field height in meters */
    public double getHeight() {
	return fishField.getHeight();
    }

    public Continuous2D getFishField() {
	return fishField;
    }

    public DoubleGrid2D getFoodField() {
	return foodField;
    }

    public IntGrid2D getHabitatField() {
	return habitatField;
    }
}