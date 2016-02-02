package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.ecs.system.AgentSystem;
import sim.engine.Kitt;
import sim.params.def.SpeciesDefinition;

public class ReproductionSystem extends AgentSystem {
    private final KittEntityCreationHandler entityCreationHandler;

    public ReproductionSystem(Kitt sim) {
	super(sim);
	entityCreationHandler = sim.getEntityCreationHandler();
    }

    /** Clears reproduction storage and creates offspring. */
    @Override
    protected void systemUpdate(Entity entity) {
	Compartments compartments = entity.get(Compartments.class);

	Amount<Energy> reproductionAmount = compartments.tryReproduction();
	if (reproductionAmount != null) {
	    reproduce(entity);
	}
    }

    private void reproduce(Entity entity) {
	SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);
	for (int i = 0; i < speciesDefinition.getNumOffspring(); i++) {
	    entityCreationHandler.createFish(speciesDefinition, getEnvironment());
	}
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(SpeciesDefinition.class, Compartments.class,
		LifeCycling.class, Moving.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(GrowthSystem.class);
    }
}
