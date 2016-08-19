package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.pathfinding.FlowMap;
import sim.engine.Kitt;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Skeletal implementation of a {@link MovementStrategy} that derives the
 * desired direction from a {@link FlowMap} specified in child classes.
 * 
 * @author mey
 *
 */
abstract class FlowMapMovement extends DesiredDirectionMovement {
    @Override
    protected final Double2D computeDesiredDirection(Entity entity, Kitt state) {
        Flowing flowing = entity.get(Flowing.class);
        Int2D mapPosition = entity.get(Moving.class).getMapPosition();

        FlowMap flow = specifyFlow(entity, state);
        flowing.setFlow(flow);
        return flow.obtainDirection(mapPosition.x, mapPosition.y);
    }

    /**
     * Specifies the {@link FlowMap} used for deriving the agent's desired
     * direction.
     * 
     * @param entity
     *            the agent entity
     * @param state
     *            the simulation state
     * @return the {@link FlowMap} to be used to derive the desired direction
     */
    protected abstract FlowMap specifyFlow(Entity entity, Kitt state);

}
