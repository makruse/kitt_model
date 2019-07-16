package de.zmt.ecs.system.agent.move;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.params.SpeciesDefinition;
import de.zmt.pathfinding.FlowMap;
import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.engine.Kitt;

/**
 * Strategy using flow fields to move towards most attractive neighbor location,
 * e.g. towards patches with most food, while evading sand areas.
 * 
 * @author mey
 *
 */
class PerceptionMovement extends FlowMapMovement {
    /** Applies habitat speed factor to speed from definition */
    @Override
    protected double computeSpeed(BehaviorMode behaviorMode, Amount<Length> bodyLength, SpeciesDefinition definition,
            Habitat habitat, MersenneTwisterFast random) {
        return super.computeSpeed(behaviorMode, bodyLength, definition, habitat, random) * habitat.getSpeedFactor();
    }

    /**
     * Returns a flow map attracting to food if feeding or to the destination
     * habitats if migrating. Predation risk and boundaries are always repulsive
     * within the returned map.
     */
    @Override
    protected FlowMap specifyFlow(Entity entity, Kitt state) {
        Entity environment = state.getEnvironment();
        Metabolizing metabolizing = entity.get(Metabolizing.class);
        SpeciesPathfindingMaps speciesPathfindingMaps = environment
                .get(SpeciesPathfindingMaps.Container.class)
                .get(entity.get(SpeciesDefinition.class));

        if (metabolizing.isFeeding()) {
            return speciesPathfindingMaps.getFeedingFlowMap();
        } else if (metabolizing.getBehaviorMode() == BehaviorMode.MIGRATING) {
            SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
            SimulationTime simTime = environment.get(SimulationTime.class);

            //TODO maybe check for resting or feeding as next mode(only if sim starts doing weird stuff again)
            BehaviorMode nextMode = definition.getBehaviorMode(simTime.getTimeOfDay().getNext());
            return speciesPathfindingMaps.getMigratingFlowMap(nextMode);
        }
        // if not feeding: only evade risk
        return speciesPathfindingMaps.getRiskFlowMap();
    }
}