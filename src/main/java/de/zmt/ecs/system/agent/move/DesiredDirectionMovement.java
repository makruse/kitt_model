package de.zmt.ecs.system.agent.move;

import static de.zmt.util.DirectionUtil.NEUTRAL;
import static javax.measure.unit.SI.RADIAN;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.util.DirectionUtil;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;

/**
 * {@link MovementStrategy} that drives agents towards desired directions.
 * 
 * @author mey
 *
 */
abstract class DesiredDirectionMovement extends AbstractMovementStrategy {
    public DesiredDirectionMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    @Override
    protected final Double2D computeDirection(Entity entity) {
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
    protected abstract Double2D computeDesiredDirection(Entity entity) ;
}
