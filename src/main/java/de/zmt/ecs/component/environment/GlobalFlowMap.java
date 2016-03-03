package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.PotentialMap;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalFlowMap implements Component {
    private static final long serialVersionUID = 1L;

    /** {@code PotentialMap} for food. */
    private final PotentialMap foodPotentialMap;

    public GlobalFlowMap(PotentialMap foodPotentialMap) {
	super();
	this.foodPotentialMap = foodPotentialMap;
    }

    public PotentialMap getFoodPotentialMap() {
	return foodPotentialMap;
    }
}
