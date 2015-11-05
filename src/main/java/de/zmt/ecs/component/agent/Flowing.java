package de.zmt.ecs.component.agent;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.*;

/**
 * Grants an agent the ability to derive a direction from several influences,
 * combined within a {@link FlowMap}.
 * 
 * @author mey
 *
 */
public class Flowing extends CombinedFlowMap implements Component {
    private static final long serialVersionUID = 1L;

    public Flowing(int width, int height) {
	super(width, height);
    }
}