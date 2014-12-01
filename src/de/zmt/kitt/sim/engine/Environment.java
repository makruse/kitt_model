package de.zmt.kitt.sim.engine;

import java.util.*;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.io.ModelParams;
import de.zmt.kitt.sim.params.SpeciesDefinition;

public class Environment implements Steppable {
    private static final long serialVersionUID = 1L;

    protected Continuous2D field;
    protected IntGrid2D habitatField;
    protected DoubleGrid2D foodField;

    protected Sim sim;
    protected ModelParams params;

    public Environment(Sim sim) {
	this.sim = sim;
	this.params = sim.params;
    }

    public void initPlayground() {

	// readHabitatDataFromText("reef1.txt");

	double bucketSize = 10; // sim.cfg.environmentDefinition.xMax /
				// habitatField.getWidth();
	// initialize foodgrid according to habitattype in input map
	MapGenerator mapGen = new MapGenerator(sim.random);
	// System.out.println("width:" + mapGen.getMapWidth());
	field = new Continuous2D(bucketSize, mapGen.getMapWidth(),
		mapGen.getMapHeight());

	// foodField=new DoubleGrid2D(mapGen.getMapWidth(),
	// mapGen.getMapHeight());
	foodField = new DoubleGrid2D((int) (getFoodCellsX()),
		(int) (getFoodCellsY()));

	habitatField = new IntGrid2D(mapGen.getMapWidth(),
		mapGen.getMapHeight());
	mapGen.createFieldsByMap(foodField, habitatField, this);
	// field = new Continuous2D( bucketSize,
	// sim.cfg.environmentDefinition.xMax,
	// sim.cfg.environmentDefinition.yMax);
	// foodField = new DoubleGrid2D (
	// (int)sim.cfg.environmentDefinition.xMax,
	// (int)sim.cfg.environmentDefinition.yMax);
	// habitatField = new IntGrid2D (
	// (int)sim.cfg.environmentDefinition.xMax,
	// (int)sim.cfg.environmentDefinition.yMax );
	// reset any population count

	double x = 0.0, y = 0.0;

	// Seeding the fishes
	for (SpeciesDefinition speciesDefinition : params.speciesList) {
	    for (int i = 0; i < speciesDefinition.initialNr; i++) {

		do {
		    x = sim.random.nextDouble() * (getFieldSizeX());
		    y = sim.random.nextDouble() * (getFieldSizeY());
		} while (getHabitatOnPosition(new Double2D(x, y)) != HabitatHerbivore.CORALREEF.id);

		Fish fish = new Fish(x, y, speciesDefinition.initialBiomass,
			speciesDefinition.initialSize, this, params,
			speciesDefinition);

		// schedule agents for the first possible time in a random order
		// from 0 to 100
		sim.schedule.scheduleRepeating(fish);
		// if( i==0)
		// sim.setIdInFocus((int)fish.getId());
	    }
	}

	sim.schedule.scheduleRepeating(this);
	// System.out.println(getFieldSizeX());
	System.out.println("creating fields finished");
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

	    for (int cy = 0; cy < getFoodCellsY(); cy++) {
		for (int cx = 0; cx < getFoodCellsX(); cx++) {

		    // habitatsabhängig
		    int iHabitat = getHabitatOnPosition(new Double2D(cx, cy));

		    double max = 0;
		    if (HabitatHerbivore.CORALREEF.id == iHabitat)
			max = HabitatHerbivore.CORALREEF.initialFoodMax;
		    else if (HabitatHerbivore.MANGROVE.id == iHabitat)
			max = HabitatHerbivore.MANGROVE.initialFoodMax;
		    else if (HabitatHerbivore.ROCK.id == iHabitat)
			max = HabitatHerbivore.ROCK.initialFoodMax;
		    else if (HabitatHerbivore.SANDYBOTTOM.id == iHabitat)
			max = HabitatHerbivore.SANDYBOTTOM.initialFoodMax;
		    else if (HabitatHerbivore.SEAGRASS.id == iHabitat)
			max = HabitatHerbivore.SEAGRASS.initialFoodMax;

		    double foodVal = getFoodAtCell(cx, cy);
		    // double foodOfset = foodVal *0.2 +
		    // sim.random.nextGaussian()*foodVal *0.01;
		    // sigmoid function for growth: e.g. 1/(1+e^-x)
		    double sig = 1 / (1 + Math.exp(-foodVal));
		    // double sig= Math.exp(-foodVal);
		    double foodOfset = foodVal * 0.2 * sig;

		    foodVal += foodOfset;

		    if (foodVal > max)
			foodVal = max;
		    // initialize foodfield by habitat rules
		    setFoodAtCell(cx, cy, foodVal);
		    // foodField.set(ix, iy, foodVal);
		}
	    }
	    // System.out.println("growth");
	    // System.out.println(getHabitatOnPosition(new Double2D( 3, 20)));
	    // System.out.println( "growth: " + foodField.get(3, 20));
	}
    }

    public void setObjectLocation(Steppable object, Double2D location) {
	field.setObjectLocation(object, location);
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

    public int getHabitatOnPosition(Double2D position) {

	int habitatX = (int) (position.x * habitatField.getWidth() / field
		.getWidth());
	int habitatY = (int) (position.y * habitatField.getHeight() / field
		.getHeight());
	if (habitatX >= habitatField.getWidth())
	    habitatX = habitatField.getWidth() - 1;
	if (habitatX < 0)
	    habitatX = 0;
	if (habitatY >= habitatField.getHeight())
	    habitatY = habitatField.getHeight() - 1;
	if (habitatY < 0)
	    habitatY = 0;
	int habitatType = habitatField.get(habitatX, habitatY);
	return habitatType;
    }

    public double getFoodOnPosition(Double2D pos) {

	double cellX = pos.x / (getFieldSizeX() / getFoodCellsX());
	double cellY = pos.y / (getFieldSizeY() / getFoodCellsY());
	if (cellX >= getFoodCellsX())
	    cellX = getFoodCellsX() - 1;
	if (cellY >= getFoodCellsY())
	    cellY = getFoodCellsY() - 1;
	if (cellX < 0)
	    cellX = 0;
	if (cellY < 0)
	    cellY = 0;
	return foodField.get((int) cellX, (int) cellY);
    }

    public void setFoodOnPosition(Double2D pos, double foodVal) {
	double cellX = pos.x / (getFieldSizeX() / getFoodCellsX());
	double cellY = pos.y / (getFieldSizeY() / getFoodCellsY());
	if (cellX >= getFoodCellsX())
	    cellX = getFoodCellsX() - 1;
	if (cellY >= getFoodCellsY())
	    cellY = getFoodCellsY() - 1;
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

    public Int2D getMemFieldCell(Double2D pos) {

	int cellX = (int) (pos.x / (getFieldSizeX() / getMemCellsX())) - 1;
	int cellY = (int) (pos.y / (getFieldSizeY() / getMemCellsY()));
	if (cellX >= getMemCellsX())
	    cellX = (int) getMemCellsX() - 1;
	if (cellY >= getMemCellsY())
	    cellY = (int) getMemCellsY() - 1;
	if (cellX < 0)
	    cellX = 0;
	if (cellY < 0)
	    cellY = 0;
	return new Int2D((cellX), (cellY));
    }

    public Double2D getRandomFieldPosition() {
	double x = sim.random.nextDouble() * (getFieldSizeX());
	double y = sim.random.nextDouble() * (getFieldSizeY());
	return new Double2D(x, y);
    }

    public List<Double2D> getCentersOfAttraction(int species) {
	List<Double2D> centers = new ArrayList<Double2D>();
	double scalingX = getFieldSizeX() / getHabitatFieldSizeX();
	double scalingY = getFieldSizeY() / getHabitatFieldSizeY();

	// define coral reef center hardcoded
	centers.add(new Double2D(20 * scalingX, 12 * scalingY));

	return centers;
    }

    public double getFieldSizeX() {
	return field.getWidth();
    }

    public double getFieldSizeY() {
	return field.getHeight();
    }

    public int getHabitatFieldSizeX() {
	return habitatField.getWidth();
    }

    public int getHabitatFieldSizeY() {
	return habitatField.getHeight();
    }

    public IntGrid2D getHabitatField() {
	return habitatField;
    }

    public Continuous2D getField() {
	return field;
    }

    public DoubleGrid2D getFoodField() {
	return foodField;
    }

    public double getMemCellsX() {
	return params.environmentDefinition.memCellsX;
    }

    public double getMemCellsY() {
	return params.environmentDefinition.memCellsY;
    }

    public double getFoodCellsX() {
	return params.environmentDefinition.foodCellsX;
    }

    public double getFoodCellsY() {
	return params.environmentDefinition.foodCellsY;
    }

    public long getCurrentTimestep() {
	return sim.schedule.getSteps();
    }
}