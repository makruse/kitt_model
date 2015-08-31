package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.sim.KittSim;

// TODO move logic of Compartments to this class?
public class CompartmentsSystem extends AgentSystem {
    public CompartmentsSystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	boolean reproductive = entity.get(LifeCycling.class).isReproductive();
	entity.get(Compartments.class).transferDigested(reproductive);

    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(
		Compartments.class, LifeCycling.class);
    }
}
