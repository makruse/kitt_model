package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.sim.engine.Kitt;
import de.zmt.sim.params.def.*;
import de.zmt.sim.params.def.SpeciesDefinition.MoveMode;
import de.zmt.util.*;
import de.zmt.util.Grid2DUtil.DoubleNeighborsResult;
import sim.field.grid.Grid2D;
import sim.util.*;

/**
 * Executes movement of simulation agents.
 * 
 * @author cmeyer
 *
 */
public class MoveSystem extends AgentSystem {
    /** A {@link MovementStrategy} corresponding to every {@link MoveMode}. */
    private final Map<MoveMode, MovementStrategy> movementStrategies;

    public MoveSystem(Kitt sim) {
	super(sim);

	movementStrategies = new HashMap<>();
	movementStrategies.put(MoveMode.RANDOM, new RandomMovement());
	movementStrategies.put(MoveMode.MEMORY, new MemoryMovement());
	movementStrategies.put(MoveMode.PERCEPTION, new PerceptionMovement());
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Moving.class, SpeciesDefinition.class);
    }

    /**
     * Executes a strategy based on the {@link MoveMode} of the species
     * currently updated.
     */
    @Override
    protected void systemUpdate(Entity entity) {
	// execute movement strategy for selected move mode
	movementStrategies.get(entity.get(SpeciesDefinition.class).getMoveMode()).move(entity);

	Double2D position = entity.get(Moving.class).getPosition();
	// update memory
	if (entity.has(Memorizing.class)) {
	    entity.get(Memorizing.class).increase(position);
	}
	// update field position
	environment.get(AgentWorld.class).setAgentPosition(entity, position);
    }

    private static interface MovementStrategy {
	/**
	 * Move the entity according to the strategy.
	 * 
	 * @param entity
	 */
	void move(Entity entity);
    }

    /**
     * Base class for movement strategies providing some general methods.
     * 
     * @author cmeyer
     * 
     */
    private abstract class AbstractMovementStrategy implements MovementStrategy {
	@Override
	public void move(Entity entity) {
	    Moving moving = entity.get(Moving.class);

	    double speed = computeSpeed(entity.get(Metabolizing.class).getBehaviorMode(),
		    entity.get(SpeciesDefinition.class));
	    Double2D velocity = computeDirection(entity).multiply(speed);
	    moving.setPosition(computePosition(moving.getPosition(), velocity));
	    moving.setVelocity(velocity);
	}

	/**
	 * Computes speed based on base speed for {@code behaviorMode} and a
	 * random deviation.
	 * 
	 * @param behaviorMode
	 * @param definition
	 * @return speed
	 */
	protected final double computeSpeed(BehaviorMode behaviorMode, SpeciesDefinition definition) {
	    double baseSpeed = definition.obtainSpeed(behaviorMode).doubleValue(UnitConstants.VELOCITY);
	    double speedDeviation = random.nextGaussian() * definition.getSpeedDeviation() * baseSpeed;
	    return baseSpeed + speedDeviation;
	}

	protected abstract Double2D computeDirection(Entity entity);

	/**
	 * Integrates velocity by adding it to position and reflect from
	 * obstacles.
	 * 
	 * @param oldPosition
	 * @param velocity
	 * @return new position
	 */
	protected final Double2D computePosition(Double2D oldPosition, Double2D velocity) {
	    double delta = EnvironmentDefinition.STEP_DURATION.doubleValue(UnitConstants.VELOCITY_TIME);
	    Double2D velocityStep = velocity.multiply(delta);
	    // multiply velocity with delta time (minutes) and add it to pos
	    MutableDouble2D newPosition = new MutableDouble2D(oldPosition.add(velocityStep));

	    // reflect on vertical border - invert horizontal velocity
	    AgentWorld agentWorld = environment.get(AgentWorld.class);
	    if (newPosition.x >= agentWorld.getWidth() || newPosition.x < 0) {
		newPosition.x = oldPosition.x - velocityStep.x;
	    }
	    // reflect on horizontal border - invert vertical velocity
	    if (newPosition.y >= agentWorld.getHeight() || newPosition.y < 0) {
		newPosition.y = oldPosition.y - velocityStep.y;
	    }

	    Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(new Double2D(newPosition),
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
	protected Double2D computeDirection(Entity entity) {
	    // return random direction
	    return new Double2D(random.nextGaussian(), random.nextGaussian()).normalize();
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
	    BehaviorMode behaviorMode = entity.get(Metabolizing.class).getBehaviorMode();
	    Double2D attractionCenter = entity.get(AttractionCenters.class).obtainCenter(behaviorMode);
	    Double2D position = entity.get(Moving.class).getPosition();
	    SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	    double distance = position.distance(attractionCenter);
	    Double2D attractionDir = attractionCenter.subtract(position).normalize();

	    // will to migrate towards attraction (0 - 1)
	    // tanh function to reduce bias as the fish moves closer
	    double willToMigrate = Math.tanh(distance
		    / definition.getMaxAttractionDistance().doubleValue(UnitConstants.WORLD_DISTANCE) * Math.PI);

	    // weight influences according to migration willingness
	    Double2D weightedAttractionDir = attractionDir.multiply(willToMigrate);
	    Double2D weightedRandomDir = super.computeDirection(entity).multiply(1 - willToMigrate);

	    return weightedAttractionDir.add(weightedRandomDir);
	}
    }

    /**
     * Strategy for moving towards patches with most food, while evading sand
     * areas.
     * 
     * @author cmeyer
     *
     */
    private class PerceptionMovement extends RandomMovement {
	/**
	 * Factor applied to food density values in {@link Habitat#SANDYBOTTOM}
	 * to drive agents away from sand.
	 */
	private static final double SANDYBOTTOM_PENALTY_FACTOR = 0.9;

	private final DoubleNeighborsResult lookupCache = new DoubleNeighborsResult();

	@Override
	protected Double2D computeDirection(Entity entity) {
	    // when resting: random direction
	    if (entity.get(Metabolizing.class).getBehaviorMode() == BehaviorMode.RESTING) {
		return super.computeDirection(entity);
	    }
	    // when foraging: go towards patch with most food
	    else {
		return computeDirectionWithMostFood(entity);
	    }
	}

	private Double2D computeDirectionWithMostFood(Entity entity) {
	    WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
	    Int2D mapPosition = converter.worldToMap(entity.get(Moving.class).getPosition());
	    Int2D highestDensityPosition = findHighestDensityPosition(obtainSurroundingFoodDensities(mapPosition));

	    // in case the current position is the best: return zero vector
	    if (highestDensityPosition.equals(mapPosition)) {
		return new Double2D();
	    }
	    return new Double2D(highestDensityPosition.subtract(mapPosition)).normalize();
	}

	private DoubleNeighborsResult obtainSurroundingFoodDensities(Int2D mapPosition) {
	    environment.get(FoodMap.class).getPotentialsField().getMooreNeighbors(mapPosition.x, mapPosition.y, 1,
		    Grid2D.BOUNDED,
		    true, lookupCache.values, lookupCache.locations.xPos, lookupCache.locations.yPos);
	    return lookupCache;
	}

	/**
	 * 
	 * @param densities
	 * @return location of the highest density value from those given
	 */
	private Int2D findHighestDensityPosition(DoubleNeighborsResult densities) {
	    HabitatMap habitatMap = environment.get(HabitatMap.class);

	    Integer highestIndex = null;
	    for (int i = 0; i < densities.size(); i++) {
		double currentDensityValue = densities.values.get(i);
		Habitat currentHabitat = habitatMap.obtainHabitat(densities.locations.xPos.get(i),
			densities.locations.yPos.get(i));
		// apply penalty factor if sand
		if (currentHabitat == Habitat.SANDYBOTTOM) {
		    currentDensityValue *= SANDYBOTTOM_PENALTY_FACTOR;
		}

		// check if current value is higher than highest found before
		if (highestIndex == null || currentDensityValue > densities.values.get(highestIndex)) {
		    highestIndex = i;
		}
	    }

	    // random index if no food around
	    if (highestIndex == null) {
		highestIndex = random.nextInt(densities.size());
	    }

	    return new Int2D(densities.locations.xPos.get(highestIndex), densities.locations.yPos.get(highestIndex));
	}
    }
}
