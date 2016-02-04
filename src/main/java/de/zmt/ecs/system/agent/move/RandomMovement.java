package de.zmt.ecs.system.agent.move;

import static javax.measure.unit.SI.RADIAN;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.util.DirectionUtil;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;

/**
 * Strategy for pure random movement with maximum speed.
 * 
 * @author mey
 * 
 */
class RandomMovement extends AbstractMovementStrategy {

    public RandomMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    /**
     * Returns a random direction.
     */
    @Override
    protected Double2D computeDirection(Entity entity) {
	Double2D currentVelocity = entity.get(Moving.class).getVelocity();
	if (currentVelocity.equals(DirectionUtil.DIRECTION_NEUTRAL)) {
	    return DirectionUtil.generate(getRandom());
	}

	double maxAngle = entity.get(SpeciesDefinition.class).getMaxTurnSpeed()
		.times(EnvironmentDefinition.STEP_DURATION).to(RADIAN).getEstimatedValue();
	double randomAngle = (getRandom().nextDouble() * 2 - 1) * maxAngle;
	return DirectionUtil.rotate(currentVelocity, randomAngle);
    }
}