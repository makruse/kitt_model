package de.zmt.ecs.system.agent;

import static javax.measure.unit.SI.RADIAN;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.FoodSystem;
import de.zmt.util.*;
import sim.engine.Kitt;
import sim.params.def.*;
import sim.params.def.SpeciesDefinition.MoveMode;
import sim.util.*;

/**
 * Executes movement of simulation agents.
 * 
 * @author mey
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
	getEnvironment().get(AgentWorld.class).setAgentPosition(entity, position);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(
		// for food potentials in flow map
		FoodSystem.class,
		// for behavior mode
		BehaviorSystem.class);
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
     * @author mey
     * 
     */
    private abstract class AbstractMovementStrategy implements MovementStrategy {
	@Override
	public void move(Entity entity) {
	    Moving moving = entity.get(Moving.class);
	    SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	    double speed = computeSpeed(entity.get(Metabolizing.class).getBehaviorMode(), definition);
	    Double2D desiredDirection = computeDesiredDirection(entity).multiply(speed);
	    double maxAnglePerStep = definition.getMaxTurnSpeed().times(EnvironmentDefinition.STEP_DURATION).to(RADIAN)
		    .getEstimatedValue();

	    Double2D velocity = clampDirection(moving.getVelocity(), desiredDirection, maxAnglePerStep);
	    moving.setPosition(computePosition(moving.getPosition(), velocity));
	    moving.setVelocity(velocity);
	}

	/**
	 * Rotates towards {@code desiredDirection}, but do not exceed
	 * {@code maxAngle}.
	 * 
	 * @param currentDirection
	 * @param desiredDirection
	 * @param maxAngle
	 * @return maximum direction towards {@code desiredDirection} without
	 *         exceeding {@code maxAngle}
	 */
	private Double2D clampDirection(Double2D currentDirection, Double2D desiredDirection, double maxAngle) {
	    if (currentDirection.equals(DirectionUtil.DIRECTION_NEUTRAL)) {
		return desiredDirection;
	    }

	    double angleBetween = DirectionUtil.angleBetween(currentDirection, desiredDirection);

	    // if beyond maximum, rotate towards it
	    if (Math.abs(angleBetween) > maxAngle) {
		return DirectionUtil.rotate(currentDirection, maxAngle * Math.signum(angleBetween));
	    }
	    return desiredDirection;
	}

	/**
	 * Computes speed based on base speed for {@code behaviorMode} and a
	 * random deviation.
	 * 
	 * @param behaviorMode
	 * @param definition
	 * @return speed
	 */
	private double computeSpeed(BehaviorMode behaviorMode, SpeciesDefinition definition) {
	    double baseSpeed = definition.obtainSpeed(behaviorMode).doubleValue(UnitConstants.VELOCITY);
	    double speedDeviation = getRandom().nextGaussian() * definition.getSpeedDeviation() * baseSpeed;
	    return baseSpeed + speedDeviation;
	}

	/**
	 * Integrates velocity by adding it to position and reflect from
	 * obstacles.
	 * 
	 * @param oldPosition
	 * @param velocity
	 * @return new position
	 */
	private Double2D computePosition(Double2D oldPosition, Double2D velocity) {
	    double delta = EnvironmentDefinition.STEP_DURATION.doubleValue(UnitConstants.VELOCITY_TIME);
	    Double2D velocityStep = velocity.multiply(delta);
	    // multiply velocity with delta time (minutes) and add it to pos
	    MutableDouble2D newPosition = new MutableDouble2D(oldPosition.add(velocityStep));

	    // reflect on vertical border - invert horizontal velocity
	    AgentWorld agentWorld = getEnvironment().get(AgentWorld.class);
	    if (newPosition.x >= agentWorld.getWidth() || newPosition.x < 0) {
		newPosition.x = oldPosition.x - velocityStep.x;
	    }
	    // reflect on horizontal border - invert vertical velocity
	    if (newPosition.y >= agentWorld.getHeight() || newPosition.y < 0) {
		newPosition.y = oldPosition.y - velocityStep.y;
	    }

	    Habitat habitat = getEnvironment().get(HabitatMap.class).obtainHabitat(new Double2D(newPosition),
		    getEnvironment().get(EnvironmentDefinition.class));

	    // stay away from main land // TODO reflect by using normals
	    if (habitat == Habitat.MAINLAND) {
		newPosition = new MutableDouble2D(oldPosition);
	    }

	    return new Double2D(newPosition);
	}

	protected abstract Double2D computeDesiredDirection(Entity entity);
    }

    /**
     * Strategy for pure random movement with maximum speed.
     * 
     * @author mey
     * 
     */
    private class RandomMovement extends AbstractMovementStrategy {
	/**
	 * Returns a random direction.
	 */
	@Override
	protected Double2D computeDesiredDirection(Entity entity) {
	    return DirectionUtil.generate(getRandom());
	}

    }

    /**
     * Strategy for moving the entity towards its attraction centers.
     * 
     * @author mey
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
	protected Double2D computeDesiredDirection(Entity entity) {
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
	    Double2D weightedRandomDir = super.computeDesiredDirection(entity).multiply(1 - willToMigrate);

	    return weightedAttractionDir.add(weightedRandomDir);
	}
    }

    /**
     * Strategy using flow fields to move towards most attractive neighbor
     * location, e.g. towards patches with most food, while evading sand areas.
     * 
     * @author mey
     *
     */
    // TODO what to do with perception radius?
    // TODO continue to evade predation risk if sated
    private class PerceptionMovement extends RandomMovement {
	@Override
	protected Double2D computeDesiredDirection(Entity entity) {
	    Metabolizing metabolizing = entity.get(Metabolizing.class);
	    // when resting or not hungry: random direction
	    if (metabolizing.getBehaviorMode() == BehaviorMode.RESTING || !metabolizing.isHungry()) {
		return super.computeDesiredDirection(entity);
	    }
	    // when foraging: go towards patch with most food
	    else {
		Double2D position = entity.get(Moving.class).getPosition();
		WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
		Int2D mapPosition = converter.worldToMap(position);
		return getEnvironment().get(EnvironmentalFlowMap.class).obtainDirection(mapPosition.x, mapPosition.y);
	    }
	}
    }
}
