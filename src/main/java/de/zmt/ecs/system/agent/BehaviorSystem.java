package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
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
	// TODO change to migrating on sunset / sunrise
	if (getEnvironment().get(SimulationTime.class).getTimeOfDay().isForagingTime(definition.getActivityPattern())) {
	    metabolizing.setBehaviorMode(BehaviorMode.FORAGING);
	} else {
	    metabolizing.setBehaviorMode(BehaviorMode.RESTING);
	}
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
