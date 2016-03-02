package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.SpeciesFlowMaps;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.pathfinding.FlowMap;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Strategy using flow fields to move towards most attractive neighbor location,
 * e.g. towards patches with most food, while evading sand areas.
 * 
 * @author mey
 *
 */
class PerceptionMovement extends DesiredDirectionMovement {
    public PerceptionMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    @Override
    protected Double2D computeDesiredDirection(Entity entity) {
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	SpeciesFlowMaps speciesFlowMaps = getEnvironment().get(SpeciesFlowMaps.Container.class)
		.get(entity.get(SpeciesDefinition.class));
	Flowing flowing = entity.get(Flowing.class);

	Double2D position = entity.get(Moving.class).getPosition();
	WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
	Int2D mapPosition = converter.worldToMap(position);

	FlowMap flow;
	if (metabolizing.isFeeding()) {
	    flow = speciesFlowMaps.getFeedingFlowMap();
	} else if (metabolizing.getBehaviorMode() == BehaviorMode.MIGRATING) {
	    SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
	    SimulationTime simTime = getEnvironment().get(SimulationTime.class);

	    BehaviorMode nextMode = definition.getBehaviorMode(simTime.getTimeOfDay().getNext());
	    flow = speciesFlowMaps.getMigratingFlowMap(nextMode);
	}
	// if not feeding: only evade risk
	else {
	    flow = speciesFlowMaps.getRiskFlowMap();
	}

	flowing.setFlow(flow);
	return flow.obtainDirection(mapPosition.x, mapPosition.y);
    }
}