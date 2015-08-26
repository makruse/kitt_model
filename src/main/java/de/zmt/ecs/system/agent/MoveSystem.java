package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Metabolizing.ActivityType;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.AbstractAgentSystem;
import de.zmt.sim.*;
import de.zmt.sim.params.def.*;
import de.zmt.sim.params.def.SpeciesDefinition.MoveMode;
import de.zmt.util.*;
import de.zmt.util.Grid2DUtil.*;
import sim.util.*;

public class MoveSystem extends AbstractAgentSystem {
    /** A strategy corresponding to every move mode */
    private final Map<MoveMode, MovementStrategy> movementStrategies;

    public MoveSystem(KittSim sim) {
	super(sim);

	movementStrategies = new HashMap<>();
	movementStrategies.put(MoveMode.RANDOM, new RandomMovement());
	movementStrategies.put(MoveMode.MEMORY, new MemoryMovement());
	movementStrategies.put(MoveMode.PERCEPTION, new PerceptionMovement());
    }

    @Override
    public void onAdd(EntityManager manager) {
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class,
		Moving.class, SpeciesDefinition.class);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	// execute movement strategy for selected move mode
	movementStrategies.get(
		entity.get(SpeciesDefinition.class).getMoveMode()).move(entity);

	Double2D position = entity.get(Moving.class).getPosition();
	// update memory
	if (entity.has(Memorizing.class)) {
	    entity.get(Memorizing.class).increase(position);
	}
	// update field position
	environment.get(AgentWorld.class).setAgentPosition(entity, position);
    }

    private static interface MovementStrategy {
	void move(Entity entity);
    }

    /**
     * Base class for movement strategies providing some general methods.
     * 
     * @author cmeyer
     * 
     */
    private abstract class AbstractMovementStrategy implements MovementStrategy {
	/**
	 * Computes speed based on base speed for {@code activityType} and a
	 * random deviation.
	 * 
	 * @param activityType
	 * @param definition
	 * @return speed
	 */
	protected final double computeSpeed(ActivityType activityType,
		SpeciesDefinition definition) {
	    double baseSpeed = definition.obtainSpeed(activityType)
		    .doubleValue(UnitConstants.VELOCITY);
	    double speedDeviation = random.nextGaussian()
		    * definition.getSpeedDeviation() * baseSpeed;
	    double speed = baseSpeed + speedDeviation;
	    return speed;
	}

	/**
	 * Integrates velocity by adding it to position and reflect from
	 * obstacles.
	 * 
	 * @param oldPosition
	 * @param velocity
	 * @return new position
	 */
	protected final Double2D computePosition(Double2D oldPosition,
		Double2D velocity) {
	    double delta = EnvironmentDefinition.STEP_DURATION
		    .doubleValue(UnitConstants.VELOCITY_TIME);
	    Double2D velocityStep = velocity.multiply(delta);
	    // multiply velocity with delta time (minutes) and add it to pos
	    MutableDouble2D newPosition = new MutableDouble2D(
		    oldPosition.add(velocityStep));

	    // reflect on vertical border - invert horizontal velocity
	    AgentWorld agentWorld = environment.get(AgentWorld.class);
	    if (newPosition.x >= agentWorld.getWidth() || newPosition.x < 0) {
		newPosition.x = oldPosition.x - velocityStep.x;
	    }
	    // reflect on horizontal border - invert vertical velocity
	    if (newPosition.y >= agentWorld.getHeight() || newPosition.y < 0) {
		newPosition.y = oldPosition.y - velocityStep.y;
	    }

	    Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(
		    new Double2D(newPosition),
		    environment.get(EnvironmentDefinition.class));

	    // stay away from main land // TODO reflect by using normals
	    if (habitat == Habitat.MAINLAND) {
		newPosition = new MutableDouble2D(oldPosition);
	    }

	    return new Double2D(newPosition);
	}
    }

    /**
     * Strategy for pure random movement with maximum speed.
     * 
     * @author cmeyer
     * 
     */
    private class RandomMovement extends AbstractMovementStrategy {

	@Override
	public void move(Entity entity) {
	    Moving moving = entity.get(Moving.class);

	    double speed = computeSpeed(entity.get(Metabolizing.class)
		    .getActivityType(), entity.get(SpeciesDefinition.class));
	    Double2D velocity = computeDirection(entity).multiply(speed);
	    moving.setPosition(computePosition(moving.getPosition(), velocity));
	    moving.setVelocity(velocity);
	}

	protected Double2D computeDirection(Entity entity) {
	    // return random direction
	    return new Double2D(random.nextGaussian(), random.nextGaussian())
		    .normalize();
	}

    }

    /**
     * Strategy for moving the entity towards its attraction centers.
     * 
     * @author cmeyer
     * 
     */
    private class MemoryMovement extends RandomMovement {
	/**
	 * Computes direction based on the center the entity is currently
	 * attracted to. The less the distance from the center is, the more the
	 * resulting direction will be randomized, making the movement become
	 * chaotic when in proximity to the center.
	 */
	@Override
	protected Double2D computeDirection(Entity entity) {
	    ActivityType activityType = entity.get(Metabolizing.class)
		    .getActivityType();
	    Double2D attractionCenter = entity.get(AttractionCenters.class)
		    .obtainCenter(activityType);
	    Double2D position = entity.get(Moving.class).getPosition();
	    SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	    double distance = position.distance(attractionCenter);
	    Double2D attractionDir = attractionCenter.subtract(position)
		    .normalize();

	    // will to migrate towards attraction (0 - 1)
	    // tanh function to reduce bias as the fish moves closer
	    double willToMigrate = Math.tanh(distance
		    / definition.getMaxAttractionDistance().doubleValue(
			    UnitConstants.WORLD_DISTANCE) * Math.PI);

	    // weight influences according to migration willingness
	    Double2D weightedAttractionDir = attractionDir
		    .multiply(willToMigrate);
	    Double2D weightedRandomDir = super.computeDirection(entity)
		    .multiply(1 - willToMigrate);

	    return weightedAttractionDir.add(weightedRandomDir);
	}
    }

    private class PerceptionMovement extends AbstractMovementStrategy {
	/** Number of random shots to check for within perception radius. */
	private static final int SHOTS = 5;
	/** Maximum consecutive number of sandy bottom tiles towards target. */
	private static final int MAX_TILES_SAND = 5;
	/** Neighborhood distance in food lookup when selecting best target */
	private static final int LOOKUP_DISTANCE_FOOD = 1;
	// lookup caches
	private final LocationsResult cacheLocationsInRange = new LocationsResult();
	private final LocationsResult cacheLocationsOnPath = new LocationsResult();
	private final LocationsResult cacheLocationsInArea = new LocationsResult();

	@Override
	public void move(Entity entity) {
	    FoodMap foodMap = environment.get(FoodMap.class);
	    HabitatMap habitatMap = environment.get(HabitatMap.class);
	    EnvironmentDefinition environmentDefinition = environment
		    .get(EnvironmentDefinition.class);

	    double perceptionMapRadius = environmentDefinition
		    .worldToMap(entity.get(SpeciesDefinition.class)
			    .getPerceptionRadius());
	    Moving moving = entity.get(Moving.class);
	    Double2D mapPosition = environmentDefinition.worldToMap(moving
		    .getPosition());
	    Int2D mapPositionInteger = new Int2D((int) mapPosition.x,
		    (int) mapPosition.y);
	    int width = foodMap.getWidth();
	    int height = foodMap.getHeight();

	    // get all map pixels that are in range of perception
	    LocationsResult locationsInRange = Grid2DUtil.findRadialLocations(
		    width, height, mapPosition, perceptionMapRadius,
		    LookupMode.BOUNDED, cacheLocationsInRange);
	    Int2D targetMapPosition = findBestTarget(
		    foodMap,
		    findSafeTargets(habitatMap, mapPositionInteger,
			    locationsInRange));
	    Double2D targetWorldPosition = environmentDefinition
		    .mapToWorld(targetMapPosition);
	    double speed = computeSpeed(entity.get(Metabolizing.class)
		    .getActivityType(), entity.get(SpeciesDefinition.class));
	    travelTowards(moving, targetWorldPosition, speed);
	}

	/**
	 * 
	 * @param habitatMap
	 * @param mapPositionInteger
	 * @param locationsInRange
	 * @return list of safe target locations from {@code locationsInRange},
	 *         each of them with no more than {@value #MAX_TILES_SAND}
	 *         consecutive tiles of sand, <b>OR</b> just one target with the
	 *         least number of consecutive sand tiles found.
	 */
	private List<Int2D> findSafeTargets(HabitatMap habitatMap,
		Int2D mapPositionInteger, LocationsResult locationsInRange) {
	    List<Int2D> safeTargets = new ArrayList<>();
	    Int2D leastUnsafeTarget = null;
	    int leastUnsafeTargetMaxSandTilesCount = 0;

	    for (int i = 0; i < SHOTS; i++) {
		// generate random position within perception radius...
		int randomIndex = random.nextInt(locationsInRange.getSize());
		Int2D currentTarget = new Int2D(
			locationsInRange.getX(randomIndex),
			locationsInRange.getY(randomIndex));

		int currentMaxSandTilesCount = countMaxConsecutiveSandTiles(
			mapPositionInteger, currentTarget, habitatMap);
		// not too many sand tiles in a row: safe target
		if (currentMaxSandTilesCount < MAX_TILES_SAND) {
		    safeTargets.add(currentTarget);
		}
		// target is safer than that found before
		if (leastUnsafeTarget == null
			|| leastUnsafeTargetMaxSandTilesCount > currentMaxSandTilesCount) {
		    leastUnsafeTarget = currentTarget;
		    leastUnsafeTargetMaxSandTilesCount = currentMaxSandTilesCount;
		}
	    }

	    // if there are no safe targets (below max tiles)
	    if (safeTargets.isEmpty()) {
		// ...return least unsafe only (least consecutive sand tiles)
		return Collections.singletonList(leastUnsafeTarget);
	    } else {
		// ... otherwise return safe targets
		return safeTargets;
	    }
	}

	/**
	 * Find the highest amount of consecutive sand tiles on a path.
	 * 
	 * @param start
	 *            of path
	 * @param end
	 *            of path
	 * @param habitatMap
	 * @return highest amount of consecutive sand tiles
	 */
	private int countMaxConsecutiveSandTiles(Int2D start, Int2D end,
		HabitatMap habitatMap) {
	    LocationsResult locationsOnPath = Grid2DUtil.findLineLocations(
		    start, end, cacheLocationsOnPath);

	    List<Integer> consecutiveSandTilesCounts = new ArrayList<>();

	    // find consecutive tiles of sandy bottom
	    int consecutiveSandTilesCount = 0;
	    for (int i = 0; i < locationsOnPath.getSize(); i++) {
		if (habitatMap.obtainHabitat(locationsOnPath.getX(i),
			locationsOnPath.getY(i)) == Habitat.SANDYBOTTOM) {
		    consecutiveSandTilesCount++;
		} else {
		    consecutiveSandTilesCounts.add(consecutiveSandTilesCount);
		    consecutiveSandTilesCount = 0;
		}
	    }
	    // add last count
	    consecutiveSandTilesCounts.add(consecutiveSandTilesCount);

	    // return the highest amount of consecutive sand tiles
	    return Collections.max(consecutiveSandTilesCounts);
	}

	/**
	 * @param foodMap
	 * @param safeTargets
	 * @return target location with the highest food density found in
	 *         {@code safeTargets}
	 */
	private Int2D findBestTarget(FoodMap foodMap, List<Int2D> safeTargets) {
	    // if there is only one safe target
	    if (safeTargets.size() == 1) {
		// ...return that target
		return safeTargets.get(0);
	    }

	    // ... otherwise find the one with highest food density
	    Int2D bestTarget = null;
	    double highestFoodDensity = 0;
	    for (Int2D target : safeTargets) {
		LocationsResult locationsInTargetArea = Grid2DUtil
			.findMooreLocations(foodMap.getWidth(),
				foodMap.getHeight(), new Double2D(target),
				LOOKUP_DISTANCE_FOOD, LookupMode.BOUNDED,
				cacheLocationsInArea);

		double targetFoodDensity = 0;
		for (int i = 0; i < locationsInTargetArea.getSize(); i++) {
		    targetFoodDensity += foodMap.getFoodDensity(
			    locationsInTargetArea.getX(i),
			    locationsInTargetArea.getY(i)).doubleValue(
			    UnitConstants.FOOD_DENSITY);
		}

		if (bestTarget == null
			|| highestFoodDensity < targetFoodDensity) {
		    bestTarget = target;
		    highestFoodDensity = targetFoodDensity;
		}
	    }

	    assert bestTarget != null;
	    return bestTarget;
	}

	private void travelTowards(Moving moving, Double2D target,
		double maxSpeed) {
	    Double2D position = moving.getPosition();
	    Double2D toTarget = position.subtract(target);
	    double toTargetLength = toTarget.length();

	    double delta = EnvironmentDefinition.STEP_DURATION
		    .doubleValue(UnitConstants.VELOCITY_TIME);
	    double maxSpeedStep = maxSpeed * delta;

	    Double2D velocity;
	    // target is too far away: travel towards it with maximum speed
	    if (toTargetLength > maxSpeedStep) {
		// ... resize velocity vector to max speed
		velocity = toTarget.multiply(maxSpeed / toTargetLength);
	    }
	    // target is in reach: go to target
	    else {
		// divide velocity by delta to reach target when integrating
		velocity = toTarget.multiply(1 / delta);
	    }

	    moving.setPosition(computePosition(position, velocity));
	    moving.setVelocity(velocity);
	}

    }
}
