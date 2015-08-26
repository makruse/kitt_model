package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.system.AbstractAgentSystem;
import de.zmt.sim.KittSim;
import de.zmt.sim.params.def.SpeciesDefinition;

public class ReproductionSystem extends AbstractAgentSystem {
    private final EntityFactory entityFactory;

    public ReproductionSystem(KittSim sim) {
	super(sim);
	entityFactory = sim.getEntityFactory();
    }

    /** Clears reproduction storage and creates offspring. */
    @Override
    protected void systemUpdate(Entity entity) {
	Compartments compartments = entity.get(Compartments.class);
	if (compartments.canReproduce()) {
	    compartments.clearReproductionStorage();
	    reproduce(entity);
	}
    }

    private void reproduce(Entity entity) {
	SpeciesDefinition speciesDefinition = entity
		.get(SpeciesDefinition.class);
	for (int i = 0; i < speciesDefinition.getNumOffspring(); i++) {
	    entityFactory.createFish(environment, speciesDefinition);
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(
		SpeciesDefinition.class, Compartments.class, Reproducing.class,
		Moving.class);
    }
}
