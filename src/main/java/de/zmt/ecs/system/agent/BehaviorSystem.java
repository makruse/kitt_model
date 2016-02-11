package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.util.TimeOfDay;
import sim.engine.Kitt;
import sim.params.def.SpeciesDefinition;

/**
 * Updates behavior mode according to time of day.
 * 
 * @author mey
 *
 */
public class BehaviorSystem extends AgentSystem {

    public BehaviorSystem(Kitt sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	// activity based on time of day
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	TimeOfDay timeOfDay = getEnvironment().get(SimulationTime.class).getTimeOfDay();
	metabolizing.setBehaviorMode(definition.getBehaviorMode(timeOfDay));
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
