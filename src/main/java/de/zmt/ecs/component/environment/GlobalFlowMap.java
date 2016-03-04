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
    private final PotentialMap boundaryPotentialMap;

    public GlobalFlowMap(PotentialMap foodPotentialMap, PotentialMap boundaryPotentialMap) {
	super();
	this.foodPotentialMap = foodPotentialMap;
	this.boundaryPotentialMap = boundaryPotentialMap;
    }

    public PotentialMap getFoodPotentialMap() {
	return foodPotentialMap;
    }

    public PotentialMap getBoundaryPotentialMap() {
	return boundaryPotentialMap;
    }
}
