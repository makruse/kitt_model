package de.zmt.ecs.system.agent.move;

import static de.zmt.util.DirectionUtil.NEUTRAL;
import static javax.measure.unit.SI.RADIAN;

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
import de.zmt.ecs.system.agent.move.MoveSystem.MoveMode;
import de.zmt.util.DirectionUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 * {@link MovementStrategy} that drives agents towards desired directions.
 * 
 * @author mey
 * 
 */
abstract class DesiredDirectionMovement implements MovementStrategy {
    /**
     * Turns {@code currentVelocity} towards {@code desiredDirection} without
     * exceeding {@code maxAngle}. Desired direction is assumed to be a unit
     * vector.
     * 
     * @param currentVelocity
     * @param desiredDirection
     * @param maxAngle
     * @return velocity vector turned towards {@code desiredDirection} without
     *         exceeding {@code maxAngle}
     */
    private static Double2D turn(Double2D currentVelocity, Double2D desiredDirection, double maxAngle) {
        if (desiredDirection.equals(NEUTRAL)) {
            return NEUTRAL;
        }
        if (currentVelocity.equals(NEUTRAL)) {
            return desiredDirection;
        }
    
        double angleBetween = DirectionUtil.angleBetween(currentVelocity, desiredDirection);
    
        // if beyond maximum: rotate towards it and resize to match speed
        if (Math.abs(angleBetween) > maxAngle) {
            return DirectionUtil.rotate(currentVelocity, maxAngle * Math.signum(angleBetween));
        }
        return desiredDirection;
    }

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
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	Growing growing = entity.get(Growing.class);
	HabitatMap habitatMap = getEnvironment().get(HabitatMap.class);
	WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);

	Habitat habitat = habitatMap.obtainHabitat(moving.getPosition(), converter);

	double speed = computeSpeed(metabolizing.getBehaviorMode(), growing.getLength(), definition, habitat);
	// if agent does not move, there is no need to calculate direction
	if (speed <= 0) {
	    moving.setVelocity(NEUTRAL);
	    return;
	}

	Double2D direction = computeDirection(entity);
	assert direction.lengthSq() == 1 : "Direction must be a unit vector.";
	Double2D velocity = direction.multiply(speed);
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
     * @param habitat
     *            the habitat the agent is in
     * @return speed
     */
    private double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition,
	    Habitat habitat) {
	double baseSpeed = definition.computeBaseSpeed(behaviorMode, bodyLength).doubleValue(UnitConstants.VELOCITY);
	// base speed is zero, no need to compute deviation
	if (baseSpeed == 0) {
	    return 0;
	}

	// random value between +speedDeviation and -speedDeviation
	double speedDeviation = (getRandom().nextDouble() * 2 - 1) * definition.getSpeedDeviation();
	double speed = baseSpeed + baseSpeed * speedDeviation;

	// only change speed according to habitat when in PERCEPTION
	if (definition.getMoveMode() == MoveMode.PERCEPTION) {
	    return speed * habitat.getSpeedFactor();
	}
	return speed;
    }

    /**
     * The direction the entity will go towards. The returned vector must be of
     * unit length (1).
     * 
     * @param entity
     * @return direction unit vector
     */
    private Double2D computeDirection(Entity entity) {
        Double2D desiredDirection = computeDesiredDirection(entity);
    
        // if undecided: go into random direction
        if (desiredDirection.equals(DirectionUtil.NEUTRAL)) {
            desiredDirection = DirectionUtil.generate(getRandom());
        }
    
        Double2D currentVelocity = entity.get(Moving.class).getVelocity();
        double maxAngle = entity.get(SpeciesDefinition.class).getMaxTurnSpeed()
        	.times(EnvironmentDefinition.STEP_DURATION).to(RADIAN).getEstimatedValue();
        return turn(currentVelocity, desiredDirection, maxAngle);
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
     * The desired direction the agent would like to go towards. Turning towards
     * it will be limited by {@link SpeciesDefinition#getMaxTurnSpeed()}.
     * Subclasses can safely specify any direction without making the agent
     * exceed that maximum. If undecided a zero vector can be returned, making
     * the agent turn randomly.
     * 
     * @param entity
     * @return desired direction unit vector or zero vector if undecided
     */
    protected abstract Double2D computeDesiredDirection(Entity entity);
}