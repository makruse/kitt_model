package de.zmt.ecs.system.agent;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;
import static javax.measure.unit.SI.RADIAN;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.AttractionCenters;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SpeciesFlowMap;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.FoodSystem;
import de.zmt.util.DirectionUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.params.def.SpeciesDefinition.MoveMode;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableDouble2D;

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
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Moving.class, SpeciesDefinition.class,
		Flowing.class, Growing.class);
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
	    Metabolizing metabolizing = entity.get(Metabolizing.class);
	    Growing growing = entity.get(Growing.class);

	    double speed = computeSpeed(metabolizing.getBehaviorMode(), growing.getLength(), definition);
	    // if agent does not move, there is no need to calculate direction
	    if (speed <= 0) {
		moving.setVelocity(DIRECTION_NEUTRAL);
		return;
	    }

	    Double2D desiredDirection = computeDesiredDirection(entity);
	    double maxAnglePerStep = definition.getMaxTurnSpeed().times(EnvironmentDefinition.STEP_DURATION).to(RADIAN)
		    .getEstimatedValue();

	    Double2D velocity = turn(moving.getVelocity(), desiredDirection, maxAnglePerStep, speed);
	    moving.setPosition(computePosition(moving.getPosition(), velocity));
	    moving.setVelocity(velocity);
	}

	/**
	 * Turns {@code currentVelocity} towards {@code desiredDirection}
	 * without exceeding {@code maxAngle}. Desired direction is assumed to
	 * be a unit vector. The resulting velocity will match {@code speed} in
	 * length.
	 * 
	 * @param currentVelocity
	 * @param desiredDirection
	 * @param maxAngle
	 * @param speed
	 *            the length of the resulting velocity vector
	 * @return velocity vector turned towards {@code desiredDirection}
	 *         without exceeding {@code maxAngle}
	 */
	private Double2D turn(Double2D currentVelocity, Double2D desiredDirection, double maxAngle, double speed) {
	    if (desiredDirection.equals(DIRECTION_NEUTRAL)) {
		return DIRECTION_NEUTRAL;
	    }
	    if (currentVelocity.equals(DIRECTION_NEUTRAL)) {
		return desiredDirection.multiply(speed);
	    }

	    double angleBetween = DirectionUtil.angleBetween(currentVelocity, desiredDirection);

	    // if beyond maximum: rotate towards it and resize to match speed
	    if (Math.abs(angleBetween) > maxAngle) {
		return DirectionUtil.rotate(currentVelocity, maxAngle * Math.signum(angleBetween)).resize(speed);
	    }
	    return desiredDirection.multiply(speed);
	}

	/**
	 * Computes speed based on base speed for {@code behaviorMode} and a
	 * random deviation.
	 * 
	 * @param behaviorMode
	 * @param bodyLength
	 *            length of the fish
	 * @param definition
	 * @return speed
	 */
	private double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength,
		SpeciesDefinition definition) {
	    double baseSpeed = definition.computeBaseSpeed(behaviorMode, bodyLength)
		    .doubleValue(UnitConstants.VELOCITY);
	    double speedDeviation = getRandom().nextGaussian() * definition.getSpeedDeviation();
	    return baseSpeed + (baseSpeed * speedDeviation);
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

	    // stay away from main land
	    if (!habitat.isAccessible()) {
		newPosition = new MutableDouble2D(oldPosition);
	    }

	    return new Double2D(newPosition);
	}

	/**
	 * The desired direction the entity would like to go towards.
	 * 
	 * @param entity
	 * @return desired direction unit vector
	 */
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
    private class PerceptionMovement extends RandomMovement {
	@Override
	protected Double2D computeDesiredDirection(Entity entity) {
	    Metabolizing metabolizing = entity.get(Metabolizing.class);
	    SpeciesFlowMap speciesFlowMap = getEnvironment().get(SpeciesFlowMap.Container.class)
		    .get(entity.get(SpeciesDefinition.class));
	    Flowing flowing = entity.get(Flowing.class);

	    Double2D position = entity.get(Moving.class).getPosition();
	    WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
	    Int2D mapPosition = converter.worldToMap(position);

	    Double2D flowDirection;
	    // when resting or not hungry: only evade risk
	    if (metabolizing.getBehaviorMode() == BehaviorMode.RESTING || !metabolizing.isHungry()) {
		flowDirection = speciesFlowMap.obtainRiskDirection(mapPosition.x, mapPosition.y);
	    }
	    // when foraging: include all influences
	    else {
		flowDirection = flowing.obtainDirection(mapPosition.x, mapPosition.y);
	    }

	    // if no direction from flow map: use random direction
	    if (flowDirection.equals(DIRECTION_NEUTRAL)) {
		return super.computeDesiredDirection(entity);
	    }
	    return flowDirection;
	}
    }
}
