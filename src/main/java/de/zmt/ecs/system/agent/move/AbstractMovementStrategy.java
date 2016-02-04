package de.zmt.ecs.system.agent.move;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.util.DirectionUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 * Base class for movement strategies providing some general methods.
 * 
 * @author mey
 * 
 */
abstract class AbstractMovementStrategy implements MovementStrategy {
    /** Entity representing the environment the agents are set into. */
    private final Entity environment;
    /** Random number generator for this simulation. */
    private final MersenneTwisterFast random;

    public AbstractMovementStrategy(Entity environment, MersenneTwisterFast random) {
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
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	Growing growing = entity.get(Growing.class);

	double speed = computeSpeed(metabolizing.getBehaviorMode(), growing.getLength(), definition);
	// if agent does not move, there is no need to calculate direction
	if (speed <= 0) {
	    moving.setVelocity(DIRECTION_NEUTRAL);
	    return;
	}

	Double2D direction = computeDirection(entity);
	assert !direction.equals(DirectionUtil.DIRECTION_NEUTRAL) : "Direction must not be neutral.";
	Double2D velocity = direction.resize(speed);
	moving.setPosition(computePosition(moving.getPosition(), velocity));
	moving.setVelocity(velocity);
    }

    /**
     * Computes speed based on base speed for {@code behaviorMode} and a random
     * deviation.
     * 
     * @param behaviorMode
     * @param bodyLength
     *            length of the fish
     * @param definition
     * @return speed
     */
    private double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition) {
	double baseSpeed = definition.computeBaseSpeed(behaviorMode, bodyLength).doubleValue(UnitConstants.VELOCITY);
	// random value between +speedDeviation and -speedDeviation
	double speedDeviation = (getRandom().nextDouble() * 2 - 1) * definition.getSpeedDeviation();
	return baseSpeed + (baseSpeed * speedDeviation);
    }

    /**
     * The direction the entity will go towards. The length of the vector is
     * irrelevant but must not be zero.
     * 
     * @param entity
     * @return direction unit vector
     */
    protected abstract Double2D computeDirection(Entity entity);

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
}