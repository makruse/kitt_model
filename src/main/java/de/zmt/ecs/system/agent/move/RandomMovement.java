package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.pathfinding.FlowMap;
import ec.util.MersenneTwisterFast;

/**
 * Strategy for random movement with maximum speed, but the agent will turn away
 * from boundaries.
 * 
 * @author mey
 * 
 */
class RandomMovement extends FlowMapMovement {

    public RandomMovement(Entity environment, MersenneTwisterFast random) {
        super(environment, random);
    }

    /** Always returns the boundary flow map. */
    @Override
    protected FlowMap specifyFlow(Entity entity) {
        return getEnvironment().get(GlobalPathfindingMaps.class).getBoundaryFlowMap();
    }

}