package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.params.EnvironmentDefinition;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * Adds passed time to age. If the agent is beyond maximum age it will die.
 * <p>
 * <img src="doc-files/gen/AgeSystem.svg" alt= "AgeSystem Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/AgeSystem.svg

start
:increase age;
if (beyond max age?) then (yes)
    :agent dies;
    end
else (no)
    stop
endif

@enduml
@formatter:on
 */
public class AgeSystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgeSystem.class.getName());

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Amount<Duration> delta = ((Kitt) state).getEnvironment().get(EnvironmentDefinition.class).getStepDuration();

        // increase age
        Aging aging = entity.get(Aging.class);
        Amount<Duration> newAge = aging.addAge(delta);
        if (newAge.isGreaterThan(aging.getMaxAge())) {
            killAgent(entity, CauseOfDeath.OLD_AGE);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.<Class<? extends Component>> asList(Aging.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(SimulationTimeSystem.class);
    }

}
