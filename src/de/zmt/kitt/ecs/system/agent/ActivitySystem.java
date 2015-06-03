package de.zmt.kitt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Metabolizing.ActivityType;
import de.zmt.kitt.ecs.component.environment.SimulationTime;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;

public class ActivitySystem extends AbstractAgentSystem {

    public ActivitySystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	// activity based on time of day
	Metabolizing energyComp = entity.get(Metabolizing.class);
	if (environment.get(SimulationTime.class).getTimeOfDay()
		.isForageTime()) {
	    energyComp.setActivityType(ActivityType.FORAGING);
	} else {
	    energyComp.setActivityType(ActivityType.RESTING);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays
		.<Class<? extends Component>> asList(Metabolizing.class);
    }

}
