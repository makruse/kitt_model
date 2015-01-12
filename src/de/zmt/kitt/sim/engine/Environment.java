package de.zmt.kitt.sim.engine;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.*;

import javax.imageio.ImageIO;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.*;
import de.zmt.kitt.util.MapUtil;
import de.zmt.sim_base.engine.ParamsSim;

public class Environment implements Steppable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Environment.class
	    .getName());

    private static final long serialVersionUID = 1L;

    private static final double BUCKET_SIZE = 10;
    private static final String MAP_IMAGE_FILENAME = "CoralEyeHabitatMapGUI.png";

    // TODO mem cells have to be adapted to image
    public static final int MEM_CELLS_X = 10;
    public static final int MEM_CELLS_Y = 10;
    /** Stores locations of fish */
    private Continuous2D fishField;
    /** Stores habitat for every location (immutable, loaded from image) */
    private final ObjectGrid2D habitatField;
    /** Stores amount of food for every location */
    private DoubleGrid2D foodField;

    private final Sim sim;
    private final EnvironmentDefinition envDef;

    public Environment(Sim sim) {
	this.sim = sim;
	this.envDef = sim.getParams().environmentDefinition;
	this.habitatField = MapUtil.createHabitatFieldFromMap(sim.random,
		loadMapImage(Sim.DEFAULT_INPUT_DIR + MAP_IMAGE_FILENAME));
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

    /** Populates fish and food fields. */
    public void initialize() {
	fishField = new Continuous2D(BUCKET_SIZE, habitatField.getWidth(),
		habitatField.getHeight());
	foodField = MapUtil.createFoodFieldFromHabitats(habitatField,
		sim.random);

	addSpeciesFromDefinitions();
    }

    private void addSpeciesFromDefinitions() {
	// creating the fishes
	for (SpeciesDefinition speciesDefinition : sim.getParams()
		.getSpeciesDefs()) {
	    for (int i = 0; i < speciesDefinition.initialNr; i++) {
		Double2D pos;

		// find random position on coral reef
		do {
		    pos = new Double2D(sim.random.nextDouble() * getWidth(),
			    sim.random.nextDouble() * getHeight());
		} while (getHabitatOnPosition(pos) != Habitat.CORALREEF);

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

	ParamsSim sim = (ParamsSim) state;

	// DAILY UPDATES:
	if (sim.schedule.getSteps() % (60 / envDef.timeResolutionMinutes * 24) == 0) {

	    // if(sim.schedule.getSteps() %
	    // (60/sim.cfg.environmentDefinition.timeResolutionMinutes) == 0) {
	    // regrowth function: 9 mg algal dry weight per m2 and day!!
	    // nach Adey & Goertemiller 1987 und Cliffton 1995
	    // put random food onto the foodField

	    for (int cy = 0; cy < foodField.getHeight(); cy++) {
		for (int cx = 0; cx < foodField.getWidth(); cx++) {
		    Habitat iHabitat = getHabitatOnPosition(new Double2D(cx, cy));

		    double max = iHabitat.getInitialFoodMax();

		    double foodVal = foodField.get(cx, cy);
		    if (foodVal <= 0) {
			foodVal = 0.1;
		    }
		    double sig = 1 / (1 + Math.exp(-foodVal));
		    double foodOfset = foodVal * 0.2 * sig;

		    foodVal += foodOfset;

		    if (foodVal > max)
			foodVal = max;
		    // initialize foodfield by habitat rules
		    foodField.set(cx, cy, foodVal);
		    // mge: Place 0 food everywhere, to see if the fish die of
		    // hunger
		    // setFoodAtCell(cx, cy, 0);
		}
	    }
	}
    }

    public long getHourOfDay() {
	long allHours = sim.schedule.getSteps() * envDef.timeResolutionMinutes
		/ 60;
	return allHours % 24;
    }

    public Habitat getHabitatOnPosition(Double2D position) {
	int habitatX = (int) (position.x * habitatField.getWidth() / fishField
		.getWidth());
	int habitatY = (int) (position.y * habitatField.getHeight() / fishField
		.getHeight());
	if (habitatX >= habitatField.getWidth())
	    habitatX = habitatField.getWidth() - 1;
	if (habitatX < 0)
	    habitatX = 0;
	if (habitatY >= habitatField.getHeight())
	    habitatY = habitatField.getHeight() - 1;
	if (habitatY < 0)
	    habitatY = 0;
	Habitat habitatType = (Habitat) habitatField.get(habitatX, habitatY);
	return habitatType;
    }

    public double getFoodOnPosition(Double2D pos) {

	double cellX = pos.x / (getWidth() / foodField.getWidth());
	double cellY = pos.y / (getHeight() / foodField.getHeight());
	// TODO this should not be necessary, check Fish.move()
	if (cellX >= foodField.getWidth())
	    cellX = foodField.getWidth() - 1;
	if (cellY >= foodField.getHeight())
	    cellY = foodField.getHeight() - 1;
	if (cellX < 0)
	    cellX = 0;
	if (cellY < 0)
	    cellY = 0;
	return foodField.get((int) cellX, (int) cellY);
    }

    public void setFoodOnPosition(Double2D pos, double foodVal) {
	double cellX = pos.x / (getWidth() / foodField.getWidth());
	double cellY = pos.y / (getHeight() / foodField.getHeight());
	// TODO bounds check should not be necessary, check Fish.move()
	if (cellX >= foodField.getWidth())
	    cellX = foodField.getWidth() - 1;
	if (cellY >= foodField.getHeight())
	    cellY = foodField.getHeight() - 1;
	if (cellX < 0)
	    cellX = 0;
	if (cellY < 0)
	    cellY = 0;
	foodField.set((int) cellX, (int) cellY, foodVal);
    }

    // TODO move to Fish?
    public Int2D getMemFieldCell(Double2D pos) {

	int cellX = (int) (pos.x / (getWidth() / MEM_CELLS_X)) - 1;
	int cellY = (int) (pos.y / (getHeight() / MEM_CELLS_Y));
	if (cellX >= MEM_CELLS_X)
	    cellX = MEM_CELLS_X - 1;
	if (cellY >= MEM_CELLS_Y)
	    cellY = MEM_CELLS_Y - 1;
	if (cellX < 0)
	    cellX = 0;
	if (cellY < 0)
	    cellY = 0;
	return new Int2D((cellX), (cellY));
    }

    public Double2D getRandomFieldPosition() {
	double x = sim.random.nextDouble() * getWidth();
	double y = sim.random.nextDouble() * getHeight();
	return new Double2D(x, y);
    }

    public double getWidth() {
	return habitatField.getWidth();
    }

    public double getHeight() {
	return habitatField.getHeight();
    }

    public Continuous2D getFishField() {
	return fishField;
    }

    public DoubleGrid2D getFoodField() {
	return foodField;
    }
}