package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.pathfinding.FlowMap;
import sim.engine.Kitt;

/**
 * Strategy for random movement with maximum speed, but the agent will turn away
 * from boundaries.
 * 
 * @author mey
 * 
 */
class RandomMovement extends FlowMapMovement {

    /** Always returns the boundary flow map. */
    @Override
    protected FlowMap specifyFlow(Entity entity, Kitt state) {
        return state.getEnvironment().get(GlobalPathfindingMaps.class).getBoundaryFlowMap();
    }

}