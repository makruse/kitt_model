package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.agent.StepSkipping;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * This system kills agents according to mortality risks.
 * <p>
 * <img src="doc-files/gen/MortalitySystem.svg" alt= "MortalitySystem Activity
 * Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/MortalitySystem.svg

start
if (coin flip successful\non habitat mortality risk?) then (yes)
	:agent dies;
	end
else if (first step in day **AND**\ncoin flip successful on\nrandom mortality risk?) then (yes)
    :agent dies;
    end
endif
stop

@enduml
@formatter:on
 */
public class MortalitySystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MortalitySystem.class.getName());

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Entity environment = ((Kitt) state).getEnvironment();
        // habitat mortality per step (because it changes all the time)
        Int2D mapPosition = entity.get(Moving.class).getMapPosition();
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(mapPosition);

        // scale predation risk according to step duration
        Amount<Frequency> predationRisk = entity.get(SpeciesDefinition.class).getPredationRisk(habitat);
        Amount<Duration> deltaTime = entity.get(StepSkipping.class).getDeltaTime();
        double currentPredationRisk = computeCurrentPredationRisk(predationRisk, deltaTime);
        if (state.random.nextBoolean(currentPredationRisk)) {
            killAgent(entity, CauseOfDeath.HABITAT);
        }
        // check for natural mortality just once per day
        else if (environment.get(SimulationTime.class).isFirstStepInDay() && state.random.nextBoolean(
                entity.get(SpeciesDefinition.class).getNaturalMortalityRisk().doubleValue(UnitConstants.PER_DAY))) {
            killAgent(entity, CauseOfDeath.RANDOM);
        }
    }

    /**
     * Scales predation risk according to passed time.
     * 
     * @param predationRisk
     *            the predation risk amount
     * @param deltaTime
     *            the time passed between iterations
     * @return the predation risk over the given step duration
     */
    private static double computeCurrentPredationRisk(Amount<Frequency> predationRisk, Amount<Duration> deltaTime) {
        long secondsPerStep = deltaTime.to(UnitConstants.SIMULATION_TIME).getExactValue();
        return predationRisk.doubleValue(UnitConstants.PER_SIMULATION_TIME) * secondsPerStep;
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Moving.class, SpeciesDefinition.class, StepSkipping.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.asList(
                // for updating the position
                MoveSystem.class,
                // for updating the delta time
                StepSkipSystem.class);
    }
}
