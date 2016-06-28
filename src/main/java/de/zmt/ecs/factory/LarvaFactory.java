package de.zmt.ecs.factory;

import java.util.Arrays;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.engine.Stoppable;

/**
 * Factory class creating larvae entities. Larvae are fish before reaching the
 * post settlement age. After that, metamorphosis is complete and a fish entity
 * will then enter the simulation, while the larva is removed.
 * 
 * @author mey
 *
 */
public class LarvaFactory implements EntityFactory<LarvaFactory.MyParam> {

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random, MyParam parameter) {
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
                    param.definition.getPostSettlementAge());
        }
    }

    /**
     * Parameter class for {@link LarvaFactory}.
     * 
     * @author mey
     *
     */
    public static class MyParam {
        private final SpeciesDefinition definition;
        private final KittEntityCreationHandler entityCreationHandler;
        private final Entity environment;

        public MyParam(SpeciesDefinition definition, KittEntityCreationHandler entityCreationHandler,
                Entity environment) {
            super();
            this.definition = definition;
            this.entityCreationHandler = entityCreationHandler;
            this.environment = environment;
        }
    }
}
