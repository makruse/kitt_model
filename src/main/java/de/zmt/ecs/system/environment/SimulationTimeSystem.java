package de.zmt.ecs.system.environment;

import java.util.Collection;
import java.util.Collections;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.environment.SimulationTime;

/**
 * Advances simulation time on each step.
 * <p>
 * <img src="doc-files/gen/SimulationTimeSystem.svg" alt=
 * "SimulationTimeSystem Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/SimulationTimeSystem.svg

start
:add STEP_DURATION
to simulation time;
stop

@enduml
@formatter:on
 */
public class SimulationTimeSystem extends AbstractSystem {

    @Override
    protected void systemUpdate(Entity entity) {
	entity.get(SimulationTime.class).addStep();
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Collections.<Class<? extends Component>> singleton(SimulationTime.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Collections.emptySet();
    }
}
