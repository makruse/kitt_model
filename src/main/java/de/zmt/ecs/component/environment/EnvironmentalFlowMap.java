package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.CombinedFlowMap;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class EnvironmentalFlowMap extends CombinedFlowMap implements Component {
    private static final long serialVersionUID = 1L;

    public EnvironmentalFlowMap(int width, int height) {
	super(width, height);
    }
}
