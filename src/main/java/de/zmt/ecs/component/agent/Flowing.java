package de.zmt.ecs.component.agent;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.FlowMap;

/**
 * Grants an agent the ability to derive a direction from several influences,
 * combined within a {@link FlowMap}.
 * 
 * @author mey
 *
 */
public class Flowing implements Component {
    private static final long serialVersionUID = 1L;

    private FlowMap flow;

    public Flowing(FlowMap flow) {
        super();
        this.flow = flow;
    }

    public FlowMap getFlow() {
        return flow;
    }

    public void setFlow(FlowMap flow) {
        this.flow = flow;
    }
}