package de.zmt.ecs.component.agent;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.environment.SpeciesFlowMap;
import de.zmt.pathfinding.FlowFromFlowsMap;
import de.zmt.pathfinding.FlowMap;

/**
 * Grants an agent the ability to derive a direction from several influences,
 * combined within a {@link FlowMap}.
 * 
 * @author mey
 *
 */
public class Flowing extends FlowFromFlowsMap implements Component {
    private static final long serialVersionUID = 1L;

    public Flowing(SpeciesFlowMap speciesFlowMap) {
	super(speciesFlowMap);
    }
}