package de.zmt.ecs.system.agent.move;

import static sim.util.DirectionConstants.NEUTRAL;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.util.Double2D;
import sim.util.MutableDouble2D;
import sim.util.Rotation2D;

/**
 * {@link MovementStrategy} that drives agents towards desired directions.
 * 
 * @author mey
 * 
 */
abstract class DesiredDirectionMovement implements MovementStrategy {
    /** Entity representing the environment the agents are set into. */
    private final Entity environment;
    /** Random number generator for this simulation. */
    private final MersenneTwisterFast random;

    public DesiredDirectionMovement(Entity environment, MersenneTwisterFast random) {
	this.environment = environment;
	this.random = random;
    }

    /**
     * Gets the entity representing the environment the agents are set into.
     *
     * @return the entity representing the environment the agents are set into
     */
    protected Entity getEnvironment() {
	return environment;
    }

    /**
     * Gets the random number generator for this simulation.
     *
     * @return the random number generator for this simulation
     */
    protected MersenneTwisterFast getRandom() {
	return random;
    }

    @Override
    public void move(Entity entity) {
	Moving moving = entity.get(Moving.class);
	SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
	BehaviorMode behaviorMode = entity.get(Metabolizing.class).getBehaviorMode();
	Amount<Length> length = entity.get(Growing.class).getLength();
	WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
	Habitat habitat = getEnvironment().get(HabitatMap.class).obtainHabitat(moving.getPosition(), converter);

	double speed = computeSpeed(behaviorMode, length, definition, habitat);
	// if agent does not move, there is no need to calculate direction
	if (speed <= 0) {
	    moving.setVelocity(NEUTRAL, 0);
	    return;
	}

	Double2D direction = computeDirection(moving.getDirection(), computeDesiredDirection(entity),
		definition.getMaxRotationPerStep());
	assert Math.abs(direction.lengthSq() - 1) < 1e-10d : "Direction must be a unit vector but has length "
		+ direction.length() + ".";

	Double2D position = computePosition(moving.getPosition(), direction.multiply(speed));
	moving.setPosition(position);
	moving.setVelocity(direction, speed);
    }

    /**
     * Computes speed via definition.
     * 
     * @param behaviorMode
     * @param bodyLength
     *            length of the fish
     * @param definition
     * @param habitat
     *            the habitat the agent is in
     * @return speed
     */
    protected double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition,
	    Habitat habitat) {
	return definition.determineSpeed(behaviorMode, bodyLength, getRandom()).doubleValue(UnitConstants.VELOCITY);
    }

    /**
     * Computes direction by ensuring that the difference between desired and
     * current direction does not exceed the maximum. If the desired direction
     * is neutral a random direction below maximum will be returned. Both given
     * directions are assumed to be unit vectors. The returned vector is a unit
     * vector as well.
     * 
     * @param currentDirection
     *            the direction the agent is currently moving towards
     * @param desiredDirection
     *            the direction the agent likes to move towards
     * @param maxRotation
     *            the maximum rotation the agent can perform this step
     * @return direction vector equal or turned towards the desired direction
     */
    private Double2D computeDirection(Double2D currentDirection, Double2D desiredDirection, Rotation2D maxRotation) {
	if (currentDirection.equals(NEUTRAL)) {
	    return desiredDirection;
	}
	// if undecided: go into random direction within max turn range
	if (desiredDirection.equals(NEUTRAL)) {
	    Rotation2D randomTurn = maxRotation.opposite().slerp(maxRotation, random.nextDouble());
	    return randomTurn.multiply(currentDirection);
	}

	Rotation2D desiredRotation = Rotation2D.fromBetween(currentDirection, desiredDirection);
	// if desired exceeds maximum
	if (desiredRotation.compareTo(maxRotation) > 0) {
	    /*
	     * Based on desired rotation's direction of circular motion, apply
	     * maximum rotation on current direction either clockwise or
	     * anti-clockwise.
	     */
	    return (desiredRotation.isClockwise() ? maxRotation : maxRotation.opposite()).multiply(currentDirection);
	}
	// desired direction does not exceed maximum: just use it
	return desiredDirection;
    }

    /**
     * Integrates velocity by adding it to position and reflect from obstacles.
     * 
     * @param oldPosition
     * @param velocity
     * @return new position
     */
    private Double2D computePosition(Double2D oldPosition, Double2D velocity) {
	double delta = EnvironmentDefinition.STEP_DURATION.doubleValue(UnitConstants.VELOCITY_TIME);
	Double2D velocityStep = velocity.multiply(delta);
	// multiply velocity with delta time (minutes) and add it to position
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
     * The desired direction the agent would like to go towards. Turning towards
     * it will be limited by {@link SpeciesDefinition#getMaxRotationPerStep()}.
     * <p>
     * Subclasses can safely specify any direction without making the agent
     * exceed that maximum. If undecided a zero vector can be returned, making
     * the agent turn randomly.
     * 
     * @param entity
     * @return desired direction unit vector or zero vector if undecided
     */
    protected abstract Double2D computeDesiredDirection(Entity entity);
}