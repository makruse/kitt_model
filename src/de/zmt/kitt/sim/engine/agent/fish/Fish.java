package de.zmt.kitt.sim.engine.agent.fish;

import static javax.measure.unit.SI.*;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.MetabolismStoppedException;
import de.zmt.kitt.sim.engine.agent.fish.Metabolism.Sex;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.sim.portrayal.FishPortrayal.FishPortrayable;
import de.zmt.kitt.sim.portrayal.FishPortrayal.MetabolismPortrayable;
import de.zmt.kitt.sim.portrayal.MemoryPortrayal.MemoryPortrayable;
import de.zmt.kitt.util.AmountUtil;
import de.zmt.sim.engine.params.def.ParameterDefinition;
import de.zmt.sim.portrayal.inspector.CombinedInspector;
import de.zmt.sim.portrayal.portrayable.ProvidesPortrayable;
import ec.util.MersenneTwisterFast;

/**
 * Fish implements the behavior of the fish<br />
 * 
 * @author oth
 * @author cmeyer
 */
public class Fish extends Agent implements Proxiable, Oriented2D,
	ProvidesInspector, ProvidesPortrayable<FishPortrayable> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Fish.class.getName());

    private static final long serialVersionUID = 1L;

    /** Maximum size of position history */
    public static final int POS_HISTORY_MAX_SIZE = 10;

    private static final Habitat RESTING_HABITAT = Habitat.CORALREEF;
    private static final Habitat FORAGING_HABITAT = Habitat.SEAGRASS;
    /** Area accessible around the current position for foraging */
    private static final Amount<Area> ACCESSIBLE_FORAGING_AREA = Amount
	    .valueOf(1, SQUARE_METRE);

    private static final double FEMALE_PROBABILITY = 0.5;

    /** Fish's energy metabolism */
    private final Metabolism metabolism;

    // MOVING
    /** attraction center of habitat-dependent foraging area */
    private final Double2D attrCenterForaging;
    /** attraction center of habitat-dependent resting area */
    private final Double2D attrCenterResting;
    /** velocity vector of agent (m/s) */
    private Double2D velocity = new Double2D();
    /** Current kind of activity the fish is doing. */
    private ActivityType activityType;

    /**
     * Memory of fish's locations. For each cell a counter is increased when the
     * fish has shown up there.
     */
    private final Memory memory;

    /** History of the last {@value #POS_HISTORY_MAX_SIZE} positions. */
    private final Queue<Double2D> posHistory = new LimitedQueue<Double2D>(
	    POS_HISTORY_MAX_SIZE);

    private final SpeciesDefinition speciesDefinition;

    /** references to the environment to have access e.g. to the field */
    private final Environment environment;

    public Fish(Double2D pos, Environment environment,
	    SpeciesDefinition speciesDefinition, MersenneTwisterFast random) {
	super(pos);
	this.environment = environment;
	this.speciesDefinition = speciesDefinition;
	this.metabolism = new Metabolism(
		random.nextBoolean(FEMALE_PROBABILITY) ? Sex.FEMALE : Sex.MALE,
		speciesDefinition);
	memory = new Memory(environment.getWidth(), environment.getHeight());

	// DEFINE STARTING CENTER OF ATTRACTIONS
	// find attraction centers for foraging and resting
	// (random, but only in preferred habitat type)
	attrCenterForaging = environment
		.generateRandomHabitatPosition(FORAGING_HABITAT);
	attrCenterResting = environment
		.generateRandomHabitatPosition(RESTING_HABITAT);
    }

    @Override
    public void step(SimState state) {
	super.step(state);
	KittSim sim = (KittSim) state;

	// activity based on time of day
	activityType = environment.getTimeOfDay().isForageTime() ? ActivityType.FORAGING
		: ActivityType.RESTING;

	move(sim.random);
	try {
	    metabolize();
	} catch (MetabolismStoppedException e) {
	    // fish died while metabolizing
	    die(this + e.getMessage());
	}

	if (metabolism.canReproduce()) {
	    reproduce(sim.schedule, sim.random);
	}

	// check for death one time a day
	if (environment.isFirstStepInDay()) {
	    applyMortalityRisk(sim.random);
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
    private void move(MersenneTwisterFast random) {
	updateVelocity(random);
	integrateVelocity();

	memory.increase(position);
	posHistory.offer(position);
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
     */
    private void updateVelocity(MersenneTwisterFast random) {
	// TODO species-dependent foraging / resting cycle
	double baseSpeed = (activityType == ActivityType.FORAGING ? speciesDefinition
		.getSpeedForaging() : speciesDefinition.getSpeedResting()).to(
		AmountUtil.VELOCITY_UNIT).getEstimatedValue();
	double speedDeviation = random.nextGaussian()
		* speciesDefinition.getSpeedDeviation() * baseSpeed;

	Double2D attractionDir = new Double2D();
	Double2D randomDir = new Double2D(random.nextGaussian(),
		random.nextGaussian()).normalize();
	double willToMigrate = 0;

	if (speciesDefinition.isAttractionEnabled()) {
	    double distance;
	    if (activityType == ActivityType.FORAGING) {
		distance = position.distance(attrCenterForaging);
		attractionDir = attrCenterForaging.subtract(position)
			.normalize();
	    } else {
		distance = position.distance(attrCenterResting);
		attractionDir = attrCenterResting.subtract(position)
			.normalize();
	    }

	    // will to migrate towards attraction (0 - 1)
	    // tanh function to reduce bias as the fish moves closer
	    willToMigrate = Math.tanh(distance
		    / speciesDefinition.getMaxAttractionDistance().doubleValue(
			    AmountUtil.LENGTH_UNIT) * Math.PI);
	}
	// weight directed and random walk according to migration willingness
	Double2D weightedAttractionDir = attractionDir.multiply(willToMigrate);
	Double2D weightedRandomDir = randomDir.multiply(1 - willToMigrate);

	velocity = (weightedAttractionDir.add(weightedRandomDir))
		.multiply(baseSpeed + speedDeviation);
    }

    /**
     * Integrates velocity by adding it to position and reflect from obstacles.
     * The field is updated with the new position as well.
     */
    private void integrateVelocity() {
	double delta = EnvironmentDefinition.STEP_DURATION.to(SECOND)
		.getEstimatedValue();
	// multiply velocity with delta time (minutes) and add it to pos
	MutableDouble2D newPosition = new MutableDouble2D(position.add(velocity
		.multiply(delta)));

	// reflect on vertical border - invert horizontal velocity
	if (newPosition.x >= environment.getWidth() || newPosition.x < 0) {
	    newPosition.x = position.x - velocity.x;
	}
	// reflect on horizontal border - invert vertical velocity
	if (newPosition.y >= environment.getHeight() || newPosition.y < 0) {
	    newPosition.y = position.y - velocity.y;
	}

	// stay away from main land // TODO reflect by using normals
	if (environment.obtainHabitat(new Double2D(newPosition)) == Habitat.MAINLAND) {
	    newPosition = new MutableDouble2D(position);
	}

	position = new Double2D(newPosition);
    }

    /**
     * Offer available food from current patch to metabolism and update
     * accordingly.
     * 
     * @throws MetabolismStoppedException
     *             if the fish died during metabolizing
     */
    private void metabolize() throws MetabolismStoppedException {
	// calculate available food from density
	Amount<Mass> availableFood = environment.obtainFoodDensity(position)
		.times(ACCESSIBLE_FORAGING_AREA).to(AmountUtil.MASS_UNIT);
	Amount<Mass> rejectedFood = metabolism.update(availableFood,
		activityType, EnvironmentDefinition.STEP_DURATION);
	// update the amount of food on current food cell
	environment.placeFoodDensity(
		position,
		rejectedFood.divide(ACCESSIBLE_FORAGING_AREA).to(
			AmountUtil.AREA_DENSITY_UNIT));
    }

    /**
     * Clears reproduction storage and creates offspring.
     * 
     * @param schedule
     * @param random
     */
    private void reproduce(Schedule schedule, MersenneTwisterFast random) {
	metabolism.clearReproductionStorage();
	for (int i = 0; i < speciesDefinition.getNumOffspring(); i++) {
	    Agent offSpring = new Fish(oldpos, environment, speciesDefinition,
		    random);
	    environment.addAgent(offSpring, schedule);
	}
    }

    private void applyMortalityRisk(MersenneTwisterFast random) {
	// random mortality
	if (random.nextBoolean(speciesDefinition.getMortalityRisk()
		.to(AmountUtil.PER_DAY).getEstimatedValue())) {
	    die(this + " had bad luck and died from random mortality.");
	}
	// habitat mortality
	else if (random.nextBoolean(environment.obtainHabitat(position)
		.getMortalityRisk().to(AmountUtil.PER_DAY).getEstimatedValue())) {
	    die(this
		    + " was torn apart by a predator and died from habitat mortality.");
	}
    }

    /**
     * Remove object from schedule and fish field.
     */
    private void die(String deathMessage) {
	logger.fine(deathMessage);

	if (stoppable != null) {
	    stoppable.stop();
	} else {
	    logger.warning(this
		    + " could not remove itself from the schedule: "
		    + "No stoppable set.");
	}
	metabolism.stop();
	environment.removeAgent(this);
    }

    @Override
    public ParameterDefinition getDefinition() {
	return speciesDefinition;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public double orientation2D() {
	return velocity.angle();
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(metabolism, state, metabolism.getClass()
			.getSimpleName()));
    }

    @Override
    public FishPortrayable providePortrayable() {
	return new MyPortrayable();
    }

    @Override
    public String toString() {
	return Fish.class.getSimpleName() + "[species="
		+ speciesDefinition.getSpeciesName() + ", pos=" + position
		+ "]";
    }

    /** Proxy class to define the properties displayed when inspected. */
    public class MyPropertiesProxy {
	public String getSpeciesName() {
	    return speciesDefinition.getSpeciesName();
	}

	public ActivityType getActivityType() {
	    return activityType;
	}

	public Double2D getPosition() {
	    return position;
	}

	public Double2D getVelocity() {
	    return velocity;
	}

	public String nameVelocity() {
	    return "Velocity_" + AmountUtil.VELOCITY_UNIT;
	}

	public double getSpeed() {
	    return velocity.length();
	}

	public String nameSpeed() {
	    return "Speed_" + AmountUtil.VELOCITY_UNIT;
	}
    }

    public class MyPortrayable implements FishPortrayable {

	@Override
	public Double2D getAttrCenterForaging() {
	    if (speciesDefinition.isAttractionEnabled()) {
		return attrCenterForaging;
	    } else {
		return null;
	    }
	}

	@Override
	public Double2D getAttrCenterResting() {
	    if (speciesDefinition.isAttractionEnabled()) {
		return attrCenterResting;
	    } else {
		return null;
	    }
	}

	@Override
	public Collection<Double2D> getPosHistory() {
	    return Collections.unmodifiableCollection(posHistory);
	}

	/** Hash code of this fish' species. Same species will return same code. */
	@Override
	public int getSpeciesHash() {
	    return speciesDefinition.hashCode();
	}

	@Override
	public MemoryPortrayable provideMemoryPortrayable() {
	    return memory.providePortrayable();
	}

	@Override
	public MetabolismPortrayable provideMetabolismPortrayble() {
	    return metabolism.providePortrayable();
	}
    }

    /**
     * @see <a
     *      href=http://stackoverflow.com/questions/5498865/size-limited-queue-
     *      that-holds-last-n-elements-in-java>Size-limited queue that holds
     *      last N elements in Java</a>
     */
    private static class LimitedQueue<E> extends LinkedList<E> {
	private static final long serialVersionUID = 1L;

	private final int limit;

	public LimitedQueue(int limit) {
	    this.limit = limit;
	}

	@Override
	public boolean add(E o) {
	    boolean added = super.add(o);
	    while (added && size() > limit) {
		super.remove();
	    }
	    return added;
	}
    }
}
