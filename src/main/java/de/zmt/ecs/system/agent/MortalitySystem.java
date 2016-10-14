package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.DynamicScheduling;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * This system kills agents according to mortality risks.
 * <p>
 * <img src="doc-files/gen/MortalitySystem.svg" alt= "MortalitySystem Activity
 * Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/MortalitySystem.svg

start
if (coin flip successful\non habitat mortality risk?) then (yes)
	:agent dies;
	end
else if (coin flip successful on\nrandom mortality risk?) then (yes)
    :agent dies;
    end
endif
stop

@enduml
@formatter:on
 */
public class MortalitySystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MortalitySystem.class.getName());

    private final RiskCache riskCache = new RiskCache();

    /**
     * Converts the risk to be applied within another time frame.
     * <p>
     * If a risk is applied for a different time frame, normal conversion will
     * not lead to the desired results. The joint probability of independent
     * events is their product. This is taken into account here. The original
     * probability will match the joint probability of the converted one if
     * applied for the conversion factor times.
     * <p>
     * For example the risk per day converted to one hour will match the joint
     * probability of the converted one applied 24 times.
     * 
     * @param risk
     *            the mortality risk to convert
     * @param timeFrame
     *            the time frame the converted risk will be applied in
     * @return the converted risk value
     */
    static double convertMortalityRisk(Amount<Frequency> risk, Amount<Duration> timeFrame) {
        double conversionFactor = timeFrame.to(risk.getUnit().inverse().asType(Duration.class)).getEstimatedValue();
        return 1 - Math.pow(1 - risk.getEstimatedValue(), conversionFactor);
    }

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Entity environment = ((Kitt) state).getEnvironment();
        // habitat mortality per step (because it changes all the time)
        Int2D mapPosition = entity.get(Moving.class).getMapPosition();
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(mapPosition);

        // scale predation risk according to step duration
        Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();
        double naturalMortalityRisk = riskCache.get(deltaTime,
                entity.get(SpeciesDefinition.class)::getNaturalMortalityRisk);
        double predationRisk = naturalMortalityRisk
                * entity.get(SpeciesDefinition.class).getPredationRiskFactor(habitat);

        if (state.random.nextBoolean(predationRisk)) {
            killAgent(entity, CauseOfDeath.HABITAT);
        } else if (state.random.nextBoolean(naturalMortalityRisk)) {
            killAgent(entity, CauseOfDeath.RANDOM);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Moving.class, SpeciesDefinition.class, DynamicScheduling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.asList(
                // for updating the position and delta time
                MoveSystem.class);
    }

    /**
     * Cache for storing a natural mortality risk value per {@link Duration}.
     * 
     * @author mey
     *
     */
    private static class RiskCache {
        private final Map<Amount<Duration>, Double> map = new HashMap<>();

        /**
         * Returns the natural mortality risk from cache for the given duration.
         * If not cached, the risk is computed, stored in cache and returned.
         * 
         * @param deltaTime
         *            the duration in which the risk is applied
         * @param naturalMortalityRiskSupplier
         *            the {@link Supplier} of the natural mortality risk used as
         *            base
         * @return the natural mortality risk value for the given duration
         */
        public double get(Amount<Duration> deltaTime, Supplier<Amount<Frequency>> naturalMortalityRiskSupplier) {
            assert deltaTime.isExact();
            Double risk = map.get(deltaTime);

            // not in cache, compute and store
            if (risk == null) {
                risk = convertMortalityRisk(naturalMortalityRiskSupplier.get(), deltaTime);
                map.put(deltaTime, risk);
            }

            return risk;
        }
    }
}
