package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.params.SpeciesDefinition;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * Creates larvae if enough energy for reproduction has been accumulated.
 * <p>
 * <img src="doc-files/gen/ReproductionSystem.svg" alt=
 * "ReproductionSystem Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/ReproductionSystem.svg

start
if (reproduction storage\n at upper limit?) then (yes)
	:clear reproduction storage;
	:create larvae according to
	parameter numOffspring;
else (no)
endif
stop

@enduml
@formatter:on
 */
public class ReproductionSystem extends AgentSystem {
    /** Clears reproduction storage and creates offspring. */
    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Compartments compartments = entity.get(Compartments.class);

        if (compartments.tryReproduction() != null) {
            reproduce(entity, (Kitt) state);
        }
    }

    /**
     * Creates larvae according to definition.
     * 
     * @see SpeciesDefinition#getNumOffspring()
     * @param entity
     *            the entity that is reproducing
     * @param state
     *            the simulation state
     */
    private static void reproduce(Entity entity, Kitt state) {
        SpeciesDefinition speciesDefinition = entity.get(SpeciesDefinition.class);
        for (int i = 0; i < speciesDefinition.getNumOffspring(); i++) {
            state.getEntityCreationHandler().createLarva(speciesDefinition, state.getEnvironment(), state.random);
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
