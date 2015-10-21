package de.zmt.ecs.system.agent;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.system.AgentSystem;
import sim.engine.Kitt;
import sim.params.def.SpeciesDefinition;

public class ReproductionSystem extends AgentSystem {
    private final EntityFactory entityFactory;

    public ReproductionSystem(Kitt sim) {
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
		SpeciesDefinition.class, Compartments.class, LifeCycling.class,
		Moving.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(GrowthSystem.class);
    }
}
