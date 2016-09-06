package de.zmt.ecs.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.zmt.ecs.AbstractSystem;
import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.Metamorphic;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * {@link EntitySystem} triggering metamorphosis of larvae to fish.
 * <p>
 * <img src="doc-files/gen/MetamorphosisSystem.svg" alt= "MetamorphosisSystem
 * Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/MetamorphosisSystem.svg

start
:remove larva entity;
:add new fish entity
with same definition;
stop

@enduml
@formatter:on
 */
public class MetamorphosisSystem extends AbstractSystem {

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        assert state.schedule.getTime() == entity.get(Metamorphic.class).getMetamorphosisTime() : "Larva " + entity
                + "was improperly scheduled: Metamorphosis time set to "
                + entity.get(Metamorphic.class).getMetamorphosisTime() + ", but was stepped at "
                + state.schedule.getTime();

        Kitt kitt = (Kitt) state;
        metamorphose(entity, kitt.getEntityCreationHandler(), kitt.getEnvironment(), kitt.random);
    }

    /**
     * Make larva entity undergo metamorphosis.
     * 
     * @param larva
     *            the larva {@link Entity}
     * @param creationHandler
     *            the {@link KittEntityCreationHandler}
     * @param environment
     *            the environment entity
     * @param random
     *            the random number generator of the simulation
     */
    static void metamorphose(Entity larva, KittEntityCreationHandler creationHandler, Entity environment,
            MersenneTwisterFast random) {
        SpeciesDefinition definition = larva.get(SpeciesDefinition.class);
        larva.stop();
        creationHandler.createFish(definition, environment, definition.getPostSettlementAge(), random);
    }


    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Metamorphic.class, SpeciesDefinition.class);
    }

}
