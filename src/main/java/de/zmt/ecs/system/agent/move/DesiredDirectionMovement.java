package de.zmt.ecs.system.agent.move;

import static sim.util.DirectionConstants.NEUTRAL;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.WorldDimension;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableDouble2D;
import sim.util.Rotation2D;

/**
 * {@link MovementStrategy} that drives agents towards desired directions.
 * 
 * @author mey
 * 
 */
abstract class DesiredDirectionMovement implements MovementStrategy {
    @Override
    public void move(Entity entity, Kitt state) {
        Entity environment = state.getEnvironment();
        Moving moving = entity.get(Moving.class);
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
        BehaviorMode behaviorMode = entity.get(Metabolizing.class).getBehaviorMode();
        Amount<Length> length = entity.get(Growing.class).getLength();
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(moving.getMapPosition());

        double speed = computeSpeed(behaviorMode, length, definition, habitat, state.random);
        // if agent does not move, there is no need to calculate direction
        if (speed <= 0) {
            moving.setVelocity(NEUTRAL, 0);
            return;
        }

        Double2D direction = computeDirection(moving.getDirection(), computeDesiredDirection(entity, state),
                definition.getMaxRotationPerStep(), state.random);
        assert direction.equals(NEUTRAL)
                || Math.abs(direction.lengthSq() - 1) < 1e-10d : "Direction must be a unit vector but has length "
                + direction.length() + ".";

        updateComponent(moving, direction, speed, environment);
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
     * @param random
     *            the random number generator to be used
     * @return speed the computed speed
     */
    protected double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition,
            Habitat habitat, MersenneTwisterFast random) {
        return definition.determineSpeed(behaviorMode, bodyLength, random).doubleValue(UnitConstants.VELOCITY);
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
     * @param random
     *            the random number generator to be used
     * @return direction vector equal or turned towards the desired direction
     */
    private static Double2D computeDirection(Double2D currentDirection, Double2D desiredDirection,
            Rotation2D maxRotation, MersenneTwisterFast random) {
        if (currentDirection.equals(NEUTRAL)) {
            return desiredDirection;
        }
        // if undecided: go into random direction within max turn range
        if (desiredDirection.equals(NEUTRAL)) {
            Rotation2D randomTurn = maxRotation.opposite().nlerp(maxRotation, random.nextDouble());
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
     * Updates given {@link Moving} component from given direction and speed by
     * integrating it.
     * 
     * @param moving
     *            the {@link Moving} component to be updated
     * @param direction
     *            the direction the agent is facing
     * @param speed
     *            the speed the agent is traveling
     * @param environment
     *            the environment entity
     */
    private static void updateComponent(Moving moving, Double2D direction, double speed, Entity environment) {
        Double2D worldPosition = moving.getWorldPosition();
        double delta = EnvironmentDefinition.STEP_DURATION.doubleValue(UnitConstants.VELOCITY_TIME);
        Double2D velocityStep = direction.multiply(speed).multiply(delta);
        // multiply velocity with delta time (minutes) and add it to position
        MutableDouble2D newWorldPosition = new MutableDouble2D(worldPosition.add(velocityStep));

        // reflect on vertical border - invert horizontal velocity
        WorldDimension worldDimension = environment.get(WorldDimension.class);
        if (newWorldPosition.x >= worldDimension.getWidth() || newWorldPosition.x < 0) {
            newWorldPosition.x = worldPosition.x - velocityStep.x;
        }
        // reflect on horizontal border - invert vertical velocity
        if (newWorldPosition.y >= worldDimension.getHeight() || newWorldPosition.y < 0) {
            newWorldPosition.y = worldPosition.y - velocityStep.y;
        }

        Int2D newMapPosition = environment.get(EnvironmentDefinition.class).worldToMap(new Double2D(newWorldPosition));
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(newMapPosition);

        // only move further if habitat is accessible
        if (habitat.isAccessible()) {
            moving.setPosition(new Double2D(newWorldPosition), newMapPosition);
        }
        moving.setVelocity(direction, speed);
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
     *            the agent entity
     * @param state
     *            the simulation state
     * @return desired direction unit vector or zero vector if undecided
     */
    protected abstract Double2D computeDesiredDirection(Entity entity, Kitt state);
}