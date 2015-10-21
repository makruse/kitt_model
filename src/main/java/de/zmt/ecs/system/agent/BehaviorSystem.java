package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.environment.SimulationTimeSystem;
import de.zmt.sim.engine.Kitt;

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
	if (environment.get(SimulationTime.class).getTimeOfDay()
		.isForageTime()) {
	    metabolizing.setBehaviorMode(BehaviorMode.FORAGING);
	} else {
	    metabolizing.setBehaviorMode(BehaviorMode.RESTING);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays
		.<Class<? extends Component>> asList(Metabolizing.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(SimulationTimeSystem.class);
    }

}
