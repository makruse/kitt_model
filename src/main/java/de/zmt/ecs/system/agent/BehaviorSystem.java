package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.TimeOfDay;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * Updates behavior mode according to time of day.
 * <p>
 * <img src="doc-files/gen/BehaviorSystem.svg" alt= "BehaviorSystem Activity
 * Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/BehaviorSystem.svg

start
partition GetBehaviorMode {
    :timeOfDay, activityPattern<
    if (SUNRISE || SUNSET) then
	    :MIGRATING>
    else if (DAY && DIURNAL ||\nNIGHT && NOCTURNAL) then
	    :FORAGING>
    else
	    :RESTING>
    endif
}

if (FORAGING AND //IsHungry//?) then (yes)
	:set feeding;
else (no)
	:unset feeding;
endif
stop

partition IsHungry {
    start
	if (gut at upper limit OR\n excess at desired amount?) then (yes)
	    :return false>
    else (no)
        :return true>
	endif
	stop
}
@enduml
@formatter:on
 */
public class BehaviorSystem extends AgentSystem {

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        // activity based on time of day
        Metabolizing metabolizing = entity.get(Metabolizing.class);
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
        Compartments compartments = entity.get(Compartments.class);

        TimeOfDay timeOfDay = ((Kitt) state).getEnvironment().get(SimulationTime.class).getTimeOfDay();
        BehaviorMode behaviorMode = definition.getBehaviorMode(timeOfDay);

        metabolizing.setBehaviorMode(behaviorMode);
        metabolizing.setFeeding(behaviorMode == BehaviorMode.FORAGING && compartments.isHungry());
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.<Class<? extends Component>> asList(Metabolizing.class, SpeciesDefinition.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(SimulationTimeSystem.class);
    }

}
