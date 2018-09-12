package de.zmt.ecs.system.agent.move;

import static sim.util.DirectionConstants.NEUTRAL;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.DynamicScheduling;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.WorldDimension;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.MathUtil;
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
    /** The maximum number of steps to pass per update. */
    private static final long MAX_STEPS_TO_PASS = 1800;

    private final RotationCache rotationCache = new RotationCache();

    @Override
    public void move(Entity entity, Kitt state) {
        Entity environment = state.getEnvironment();
        Moving moving = entity.get(Moving.class);
        DynamicScheduling dynamicScheduling = entity.get(DynamicScheduling.class);
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

        BehaviorMode behaviorMode = entity.get(Metabolizing.class).getBehaviorMode();
        Amount<Length> length = entity.get(Growing.class).getLength();
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(moving.getMapPosition());

        double speedMPerS = computeSpeed(behaviorMode, length, definition, habitat, state.random);
        EnvironmentDefinition environmentDefinition = environment.get(EnvironmentDefinition.class);
        dynamicScheduling.setSkip(state.schedule.getTime(),
                computeStepsToSkip(speedMPerS, environmentDefinition.getMapScale(),
                        entity.get(SpeciesDefinition.class).getCellPassPerUpdate()),
                environmentDefinition.getStepDuration());


        // if agent does not move, there is no need to calculate direction
        if (speedMPerS <= 0) {
            moving.setVelocity(NEUTRAL, 0);
            return;
        }

        Amount<Duration> deltaTime = dynamicScheduling.getDeltaTime();
        Double2D direction = computeDirection(moving.getDirection(), computeDesiredDirection(entity, state),
                rotationCache.request(definition, deltaTime), state.random);
        assert direction.equals(NEUTRAL)
                || Math.abs(direction.lengthSq() - 1) < 1e-10d : "Direction must be a unit vector but has length "
                        + direction.length() + ".";

        moving.setVelocity(direction, speedMPerS);

        double deltaTimeValue = deltaTime.doubleValue(UnitConstants.VELOCITY_TIME);
        updatePosition(moving, direction.multiply(speedMPerS).multiply(deltaTimeValue), environment);
    }

    /**
     * Computes speed value in m/s via definition.
     * 
     * @see SpeciesDefinition#determineSpeed(BehaviorMode, Amount,
     *      MersenneTwisterFast)
     * @param behaviorMode
     * @param bodyLength
     *            length of the fish
     * @param definition
     * @param habitat
     *            the habitat the agent is in
     * @param random
     *            the random number generator to be used
     * @return speed the computed speed in m/s
     */
    protected double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition,
            Habitat habitat, MersenneTwisterFast random) {
        return definition.determineSpeed(behaviorMode, bodyLength, random).doubleValue(UnitConstants.VELOCITY);
    }

    /**
     * Computes the steps to skip. The result will not exceed the limit of cells
     * to pass.
     * 
     * @param speedMPerS
     *            the agent speed in m/s
     * @param mapScale
     *            the map scale
     * @param cellPassPerUpdate
     *            the desired number of cells to be passed per update
     * @return the number of steps to skip
     */
    private static long computeStepsToSkip(double speedMPerS, double mapScale, double cellPassPerUpdate) {
        if (speedMPerS > 0) {
            return MathUtil.clamp((long) ((mapScale * cellPassPerUpdate) / speedMPerS), 1, MAX_STEPS_TO_PASS);
        }
        // if zero speed: sets next step to skip maximum
        else {
            return MAX_STEPS_TO_PASS;
        }
    }

    //TODO debug computeDirection
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
    static Double2D computeDirection(Double2D currentDirection, Double2D desiredDirection,
            Rotation2D maxRotation, MersenneTwisterFast random) {
        // if the agent is not limited by a current direction...
        if (currentDirection.equals(NEUTRAL)) {
            // ... and undecided: use a direction from a random angle
            if (desiredDirection.equals(NEUTRAL)) {
                double randomAngle = random.nextDouble() * Math.PI * 2;
                return Rotation2D.fromAngle(randomAngle).getVector();
            }
            // ... otherwise take the desired direction
            else {
                return desiredDirection;
            }
        }
        
        // if going somewhere and undecided:
        // go into random direction within max turn range
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
     * Updates world and map positions in given {@link Moving} component,
     * reflecting from map borders and inaccessible areas if necessary.
     * 
     * @param moving
     *            the {@link Moving} component to be updated
     * @param positionChange
     *            the change in position for the current step
     * @param environment
     *            the environment entity
     */
    private static void updatePosition(Moving moving, Double2D positionChange, Entity environment) {
        Double2D worldPosition = moving.getWorldPosition();
        // multiply velocity with delta time (minutes) and add it to position
        MutableDouble2D newWorldPosition = new MutableDouble2D(worldPosition.add(positionChange));

        // reflect on vertical border - invert horizontal velocity
        WorldDimension worldDimension = environment.get(WorldDimension.class);
        if (newWorldPosition.x >= worldDimension.getWidth() || newWorldPosition.x < 0) {
            newWorldPosition.x = worldPosition.x - positionChange.x;
        }
        // reflect on horizontal border - invert vertical velocity
        if (newWorldPosition.y >= worldDimension.getHeight() || newWorldPosition.y < 0) {
            newWorldPosition.y = worldPosition.y - positionChange.y;
        }

        Int2D newMapPosition = environment.get(EnvironmentDefinition.class).worldToMap(new Double2D(newWorldPosition));
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(newMapPosition);

        // only move further if habitat is accessible
        if (habitat.isAccessible()) {
            moving.setPosition(new Double2D(newWorldPosition), newMapPosition);
        }
    }

    /**
     * The desired direction the agent would like to go towards. Turning towards
     * it will be limited by
     * {@link SpeciesDefinition#determineMaxRotationPerStep(Amount)}.
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

    /**
     * Class for caching {@link Rotation2D} objects.
     * 
     * @author mey
     *
     */
    private static class RotationCache {
        private final Table<SpeciesDefinition, Amount<Duration>, Rotation2D> table = HashBasedTable.create();

        /**
         * Retrieves maximum rotation per step from cache if possible or
         * computes and caches it.
         * 
         * @param definition
         *            the {@link SpeciesDefinition}
         * @param deltaTime
         *            the time passed between iterations
         * @return the maximum rotation allowed within this duration
         */
        private Rotation2D request(SpeciesDefinition definition, Amount<Duration> deltaTime) {
            Rotation2D maxRotationPerStep = table.get(definition, deltaTime);
            if (maxRotationPerStep == null) {
                maxRotationPerStep = definition.determineMaxRotationPerStep(deltaTime);
                table.put(definition, deltaTime, maxRotationPerStep);
            }
            return maxRotationPerStep;
        }
    }
}