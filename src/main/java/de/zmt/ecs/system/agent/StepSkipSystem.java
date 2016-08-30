package de.zmt.ecs.system.agent;

import static javax.measure.unit.NonSI.MINUTE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.agent.StepSkipping;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.Schedule;
import sim.engine.SimState;

/**
 * {@link EntitySystem} that sets skip of {@link StepSkipping} component
 * according to agent speed, making sure the agent will not progress more than
 * one cell per step.
 * 
 * @author mey
 *
 */
public class StepSkipSystem extends AbstractSystem {
    /** The maximum skipped duration that can be set. */
    private static final Amount<Duration> MAX_SKIPPED_DURATION = Amount.valueOf(30, MINUTE)
            .to(UnitConstants.SIMULATION_TIME);

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        long currentSteps = state.schedule.getSteps();
        // do nothing if at epoch, speed will be at its initial value
        if (currentSteps <= Schedule.EPOCH) {
            return;
        }

        EnvironmentDefinition environmentDefinition = ((Kitt) state).getEnvironment().get(EnvironmentDefinition.class);
        StepSkipping stepSkipping = entity.get(StepSkipping.class);

        Amount<Duration> stepDuration = environmentDefinition.getStepDuration();
        double speed = entity.get(Moving.class).getSpeed().doubleValue(UnitConstants.VELOCITY);

        if (speed > 0) {
            stepSkipping.setSkip(currentSteps, (long) (environmentDefinition.getMapScale() / speed), stepDuration);
        }
        // if zero speed: sets next step to skip maximum
        else {
            stepSkipping.setSkip(currentSteps, MAX_SKIPPED_DURATION, stepDuration);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Moving.class, StepSkipping.class);
    }

}
