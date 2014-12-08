package de.zmt.kitt.sim.engine;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
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

public class Environment implements Steppable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Environment.class
	    .getName());

    private static final long serialVersionUID = 1L;

    private static final double BUCKET_SIZE = 10;
    private static final String MAP_IMAGE_FILENAME = "CoralEyeHabitatMapGUI.png";

    public static final int MEM_CELLS_X = 10;
    public static final int MEM_CELLS_Y = 10;
    private Continuous2D fishField;
    private ObjectGrid2D habitatField;
    private DoubleGrid2D foodField;

    private final Sim sim;
    private final ModelParams params;

    public Environment(Sim sim) {
	this.sim = sim;
	this.params = sim.getParams();
    }

    public void initPlayground() {
	String imagePath = Sim.DEFAULT_INPUT_DIR + MAP_IMAGE_FILENAME;
	BufferedImage mapImage = null;
	logger.fine("Loading map image from " + imagePath);
	try {

	    mapImage = ImageIO.read(new File(imagePath));

	} catch (IOException e) {

	    logger.log(Level.WARNING, "Could not load map image from "
		    + imagePath);
	}

	// initialize food grid according to habitat type in input map
	fishField = new Continuous2D(BUCKET_SIZE, mapImage.getWidth(),
		mapImage.getHeight());

	habitatField = MapUtil.createHabitatFieldFromMap(sim.random, mapImage);
	foodField = MapUtil.createFoodFieldFromHabitats(habitatField,
		sim.random);

	double x = 0.0, y = 0.0;

	// creating the fishes
	for (SpeciesDefinition speciesDefinition : params.getSpeciesDefs()) {
	    for (int i = 0; i < speciesDefinition.initialNr; i++) {

		do {
		    x = sim.random.nextDouble() * (getFieldWidth());
		    y = sim.random.nextDouble() * (getFieldHeight());
		} while (getHabitatOnPosition(new Double2D(x, y)) != HabitatHerbivore.CORALREEF);

		Fish fish = new Fish(x, y, speciesDefinition.initialBiomass,
			speciesDefinition.initialSize, this, params,
			speciesDefinition);

		sim.schedule.scheduleRepeating(fish);
	    }
	}

	sim.schedule.scheduleRepeating(this);
    }

    /**
     * contains update methods of the environment e.g. growth of the seagrass
     */
    @Override
    public void step(SimState state) {

	Sim sim = (Sim) state;

	// DAILY UPDATES:
	if (sim.schedule.getSteps()
		% (60 / params.environmentDefinition.timeResolutionMinutes * 24) == 0) {

	    // if(sim.schedule.getSteps() %
	    // (60/sim.cfg.environmentDefinition.timeResolutionMinutes) == 0) {
	    // regrowth function: 9 mg algal dry weight per m2 and day!!
	    // nach Adey & Goertemiller 1987 und Cliffton 1995
	    // put random food onto the foodField

	    for (int cy = 0; cy < foodField.getHeight(); cy++) {
		for (int cx = 0; cx < foodField.getWidth(); cx++) {
		    HabitatHerbivore iHabitat = getHabitatOnPosition(new Double2D(
			    cx, cy));

		    double max = iHabitat.getInitialFoodMax();

		    double foodVal = getFoodAtCell(cx, cy);
		    double sig = 1 / (1 + Math.exp(-foodVal));
		    double foodOfset = foodVal * 0.2 * sig;

		    foodVal += foodOfset;

		    if (foodVal > max)
			foodVal = max;
		    // initialize foodfield by habitat rules
		    setFoodAtCell(cx, cy, foodVal);
		    // foodField.set(ix, iy, foodVal);
		}
	    }
	}
    }

    public long getTimeRes() {
	return params.environmentDefinition.timeResolutionMinutes;
    }

    public long getDielCycle() {
	long numberOfCurrentSteps = getCurrentTimestep();
	long minutesAlltogether = numberOfCurrentSteps * getTimeRes();
	long minutesPerDay = 24 * 60 * 60;
	long day = minutesAlltogether / minutesPerDay;
	return day + 1;
    }

    public long getDay() {
	long numberOfCurrentSteps = getCurrentTimestep();
	long minutesAlltogether = numberOfCurrentSteps * getTimeRes();
	long minutesPerDay = 24 * 60 * 60;
	long day = minutesAlltogether / minutesPerDay;
	return day + 1;
    }

    public long getHourOfDay() {

	long allHours = getCurrentTimestep() * getTimeRes() / 60;
	return allHours % 24;
    }

    public long getDayTimeInMinutes() {
	long numberOfCurrentSteps = getCurrentTimestep();
	long minutesAlltogether = numberOfCurrentSteps * getTimeRes();
	long minutesPerDay = 24 * 60 * 60;
	long dayTimeInMinutes = minutesAlltogether % minutesPerDay;
	return dayTimeInMinutes;
    }

    public HabitatHerbivore getHabitatOnPosition(Double2D position) {

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
	HabitatHerbivore habitatType = (HabitatHerbivore) habitatField.get(
		habitatX, habitatY);
	return habitatType;
    }

    public double getFoodOnPosition(Double2D pos) {

	double cellX = pos.x / (getFieldWidth() / foodField.getWidth());
	double cellY = pos.y / (getFieldHeight() / foodField.getHeight());
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
	double cellX = pos.x / (getFieldWidth() / foodField.getWidth());
	double cellY = pos.y / (getFieldHeight() / foodField.getHeight());
	// TODO this should not be necessary, check Fish.move()
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

    public double getFoodAtCell(int cx, int cy) {

	return foodField.get(cx, cy);
    }

    public void setFoodAtCell(int cx, int cy, double foodVal) {

	foodField.set(cx, cy, foodVal);
    }

    // TODO move to Fish?
    public Int2D getMemFieldCell(Double2D pos) {

	int cellX = (int) (pos.x / (getFieldWidth() / MEM_CELLS_X)) - 1;
	int cellY = (int) (pos.y / (getFieldHeight() / MEM_CELLS_Y));
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
	double x = sim.random.nextDouble() * (getFieldWidth());
	double y = sim.random.nextDouble() * (getFieldHeight());
	return new Double2D(x, y);
    }

    public List<Double2D> getCentersOfAttraction(int species) {
	List<Double2D> centers = new ArrayList<Double2D>();
	double scalingX = getFieldWidth() / getHabitatFieldSizeX();
	double scalingY = getFieldHeight() / getHabitatFieldSizeY();

	// define coral reef center hardcoded
	centers.add(new Double2D(20 * scalingX, 12 * scalingY));

	return centers;
    }

    public double getFieldWidth() {
	return fishField.getWidth();
    }

    public double getFieldHeight() {
	return fishField.getHeight();
    }

    public int getHabitatFieldSizeX() {
	return habitatField.getWidth();
    }

    public int getHabitatFieldSizeY() {
	return habitatField.getHeight();
    }

    public ObjectGrid2D getHabitatField() {
	return habitatField;
    }

    public Continuous2D getFishField() {
	return fishField;
    }

    public DoubleGrid2D getFoodField() {
	return foodField;
    }

    public long getCurrentTimestep() {
	return sim.schedule.getSteps();
    }
}