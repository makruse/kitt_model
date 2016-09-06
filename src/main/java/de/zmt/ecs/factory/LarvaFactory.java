package de.zmt.ecs.factory;

import java.util.Arrays;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityFactory;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.Metamorphic;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;

/**
 * Factory class creating larva entities.
 * 
 * @see KittEntityCreationHandler#createLarva(SpeciesDefinition, Amount,
 *      MersenneTwisterFast)
 * @author mey
 *
 */
public class LarvaFactory implements EntityFactory<LarvaFactory.MyParam> {

    @Override
    public Entity create(EntityManager manager, MyParam parameter) {
        SpeciesDefinition definition = parameter.definition;
        double postSettlementTime = definition.getPostSettlementAge().divide(parameter.stepDuration).to(Unit.ONE)
                .getEstimatedValue();
        Metamorphic metamorphic = new Metamorphic(parameter.time + postSettlementTime);

        return new Entity(manager, definition.getName() + " larva", Arrays.asList(definition, metamorphic));
    }

    /**
     * Parameter class for {@link LarvaFactory}.
     * 
     * @author mey
     *
     */
    public static class MyParam implements EntityFactory.Parameter {
        private final SpeciesDefinition definition;
        private final Amount<Duration> stepDuration;
        private final double time;

        public MyParam(SpeciesDefinition definition, Amount<Duration> stepDuration, double time) {
            super();
            this.definition = definition;
            this.stepDuration = stepDuration;
            this.time = time;
        }
    }
}
