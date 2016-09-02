package de.zmt.ecs.factory;

import java.io.Serializable;
import java.util.Arrays;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityFactory;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.engine.Stoppable;

/**
 * Factory class creating larva entities.
 * 
 * @see KittEntityCreationHandler#createLarva(SpeciesDefinition, Entity,
 *      MersenneTwisterFast)
 * @author mey
 *
 */
public class LarvaFactory implements EntityFactory<LarvaFactory.MyParam> {

    @Override
    public Entity create(EntityManager manager, MyParam parameter) {
        SpeciesDefinition definition = parameter.definition;
        /*
         * Larvae will "die" when reaching post settlement age, which triggers
         * the stoppable.
         */
        Aging aging = new Aging(AmountUtil.zero(UnitConstants.AGE), definition.getPostSettlementAge());
        Entity larvaEntity = new Entity(manager, definition.getName() + " larva", Arrays.asList(aging));
        larvaEntity.addStoppable(new LarvaStoppable(parameter));
        return larvaEntity;
    }

    /**
     * {@link Stoppable} set to every larva entity created. When stopped, a fish
     * entity is created, simulating the completed metamorphosis.
     * 
     * @author mey
     *
     */
    private static class LarvaStoppable implements Stoppable {
        private static final long serialVersionUID = 1L;

        private final MyParam param;

        public LarvaStoppable(MyParam param) {
            super();
            this.param = param;
        }

        @Override
        public void stop() {
            param.entityCreationHandler.createFish(param.definition, param.environment,
                    param.definition.getPostSettlementAge(), param.random);
        }
    }

    /**
     * Parameter class for {@link LarvaFactory}.
     * 
     * @author mey
     *
     */
    // needed serialization because it is also used in stoppable
    public static class MyParam implements EntityFactory.Parameter, Serializable {
        private static final long serialVersionUID = 1L;

        private final SpeciesDefinition definition;
        private final KittEntityCreationHandler entityCreationHandler;
        private final Entity environment;
        private final MersenneTwisterFast random;

        public MyParam(SpeciesDefinition definition, KittEntityCreationHandler entityCreationHandler,
                Entity environment, MersenneTwisterFast random) {
            super();
            this.definition = definition;
            this.entityCreationHandler = entityCreationHandler;
            this.environment = environment;
            this.random = random;
        }
    }
}
