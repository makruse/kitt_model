package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * Creates larvae if enough energy for reproduction has been accumulated.
 * <p>
 * <img src="doc-files/gen/ReproductionSystem.svg" alt= "ReproductionSystem
 * Activity Diagram">
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
        if (entity.get(Compartments.class).tryReproduction() != null) {
            Kitt kitt = (Kitt) state;
            EnvironmentDefinition environmentDefinition = kitt.getParams().getEnvironmentDefinition();
            reproduce(entity.get(SpeciesDefinition.class), environmentDefinition.getMaxAgentCount(),
                    environmentDefinition.getStepDuration(), kitt.getEntityCreationHandler(), kitt.random);
        }
    }

    /**
     * Creates larvae according to definition.
     * 
     * @see SpeciesDefinition#getNumOffspring()
     * @param speciesDefinition
     *            the definition of the entity that is reproducing
     * @param maxAgentCount
     *            the maximum number of agents
     * @param stepDuration
     *            the duration of one simulation step
     * @param entityCreationHandler
     *            the {@link KittEntityCreationHandler}
     * @param random
     *            the random number generator of the simulation
     */
    static void reproduce(SpeciesDefinition speciesDefinition, int maxAgentCount, Amount<Duration> stepDuration,
            KittEntityCreationHandler entityCreationHandler, MersenneTwisterFast random) {
        for (int i = 0; i < speciesDefinition.getNumOffspring(); i++) {
            // cancel larva creation if there are too many agents
            if (entityCreationHandler.getManager().getAllEntitiesPossessingComponent(SpeciesDefinition.class)
                    .size() >= maxAgentCount) {
                break;
            }
            entityCreationHandler.createLarva(speciesDefinition, stepDuration, random);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.<Class<? extends Component>> asList(SpeciesDefinition.class, Compartments.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(GrowthSystem.class);
    }
}
