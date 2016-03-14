package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.pathfinding.FlowFromPotentialsMap;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Strategy for random movement with maximum speed, but the agent will turn away
 * from boundaries.
 * 
 * @author mey
 * 
 */
class RandomMovement extends DesiredDirectionMovement {

    public RandomMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    @Override
    protected Double2D computeDesiredDirection(Entity entity) {
	Double2D position = entity.get(Moving.class).getPosition();
	Flowing flowing = entity.get(Flowing.class);
	WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
	Int2D mapPosition = converter.worldToMap(position);

	FlowFromPotentialsMap boundaryFlowMap = getEnvironment().get(GlobalPathfindingMaps.class).getBoundaryFlowMap();
	flowing.setFlow(boundaryFlowMap);
	return boundaryFlowMap.obtainDirection(mapPosition.x,
		mapPosition.y);
    }


}