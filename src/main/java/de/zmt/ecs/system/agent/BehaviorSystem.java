package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.sim.engine.Kitt;

public class BehaviorSystem extends AgentSystem {

    public BehaviorSystem(Kitt sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	// activity based on time of day
	Metabolizing energyComp = entity.get(Metabolizing.class);
	if (environment.get(SimulationTime.class).getTimeOfDay()
		.isForageTime()) {
	    energyComp.setBehaviorMode(BehaviorMode.FORAGING);
	} else {
	    energyComp.setBehaviorMode(BehaviorMode.RESTING);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays
		.<Class<? extends Component>> asList(Metabolizing.class);
    }

}
