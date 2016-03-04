package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.PotentialMap;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalPathfindingMaps implements Component {
    private static final long serialVersionUID = 1L;

    /** {@code PotentialMap} attracting to food. */
    private final PotentialMap foodPotentialMap;
    /** {@link PotentialMap} repulsing at map borders and mainland. */
    private final PotentialMap boundaryPotentialMap;

    public GlobalPathfindingMaps(PotentialMap foodPotentialMap, PotentialMap boundaryPotentialMap) {
	super();
	this.foodPotentialMap = foodPotentialMap;
	this.boundaryPotentialMap = boundaryPotentialMap;
    }

    /**
     * Gets the {@code PotentialMap} attracting to food.
     *
     * @return the {@code PotentialMap} attracting to food
     */
    public PotentialMap getFoodPotentialMap() {
	return foodPotentialMap;
    }

    /**
     * Gets the {@link PotentialMap} repulsing at map borders and mainland.
     *
     * @return the {@link PotentialMap} repulsing at map borders and mainland
     */
    public PotentialMap getBoundaryPotentialMap() {
	return boundaryPotentialMap;
    }
}
