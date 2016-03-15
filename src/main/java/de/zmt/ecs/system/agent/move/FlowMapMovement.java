package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.pathfinding.FlowMap;
import ec.util.MersenneTwisterFast;
import sim.params.def.EnvironmentDefinition;
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
    public FlowMapMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    /**
     * Specifies the {@link FlowMap} used for deriving the agent's desired
     * direction.
     * 
     * @param entity
     * @return the {@link FlowMap} to be used to derive the desired direction
     */
    protected abstract FlowMap specifyFlow(Entity entity);

    @Override
    protected final Double2D computeDesiredDirection(Entity entity) {
	Flowing flowing = entity.get(Flowing.class);
	Double2D position = entity.get(Moving.class).getPosition();
	WorldToMapConverter converter = getEnvironment().get(EnvironmentDefinition.class);
	Int2D mapPosition = converter.worldToMap(position);

	FlowMap flow = specifyFlow(entity);
	flowing.setFlow(flow);
	return flow.obtainDirection(mapPosition.x, mapPosition.y);
    }

}
