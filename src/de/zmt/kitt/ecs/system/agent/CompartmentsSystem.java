package de.zmt.kitt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.KittSim;

// TODO move logic of CompartmentsComponent to this class?
public class CompartmentsSystem extends AbstractAgentSystem {
    public CompartmentsSystem(KittSim sim) {
	super(sim);
    }

    @Override
    protected void systemUpdate(Entity entity) {
	boolean reproductive = entity.get(Reproducing.class).isReproductive();
	entity.get(Compartments.class).transferDigested(reproductive);

    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(
		Compartments.class, Reproducing.class);
    }
}
