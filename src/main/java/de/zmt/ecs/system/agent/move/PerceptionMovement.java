package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.pathfinding.FlowMap;
import ec.util.MersenneTwisterFast;
import sim.params.def.SpeciesDefinition;

/**
 * Strategy using flow fields to move towards most attractive neighbor location,
 * e.g. towards patches with most food, while evading sand areas.
 * 
 * @author mey
 *
 */
class PerceptionMovement extends FlowMapMovement {
    public PerceptionMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    /**
     * Returns a flow map attracting to food if feeding or to the destination
     * habitats if migrating. Predation risk and boundaries are always repulsive
     * within the returned map.
     */
    @Override
    protected FlowMap specifyFlow(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	SpeciesPathfindingMaps speciesPathfindingMaps = getEnvironment().get(SpeciesPathfindingMaps.Container.class)
		.get(entity.get(SpeciesDefinition.class));

	FlowMap flow;
	if (metabolizing.isFeeding()) {
	    flow = speciesPathfindingMaps.getFeedingFlowMap();
	} else if (metabolizing.getBehaviorMode() == BehaviorMode.MIGRATING) {
	    SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
	    SimulationTime simTime = getEnvironment().get(SimulationTime.class);

	    BehaviorMode nextMode = definition.getBehaviorMode(simTime.getTimeOfDay().getNext());
	    flow = speciesPathfindingMaps.getMigratingFlowMap(nextMode);
	}
	// if not feeding: only evade risk
	else {
	    flow = speciesPathfindingMaps.getRiskFlowMap();
	}

	return flow;
    }
}