package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.FlowFromPotentialsMap;
import de.zmt.pathfinding.FlowMap;
import de.zmt.pathfinding.MapType;
import de.zmt.pathfinding.PotentialMap;
import sim.util.Proxiable;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalPathfindingMaps implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Flow map attracting to food. */
    private final PotentialMap foodPotentialMap;
    /** Flow map repulsing at map borders and mainland. */
    private final FlowFromPotentialsMap boundaryFlowMap;

    private final String boundaryPotentialMapName;

    public GlobalPathfindingMaps(PotentialMap foodPotentialMap, PotentialMap boundaryPotentialMap) {
	super();
	this.foodPotentialMap = foodPotentialMap;
	this.boundaryFlowMap = new FlowFromPotentialsMap(boundaryPotentialMap.getWidth(),
		boundaryPotentialMap.getHeight());
	boundaryFlowMap.setName(MapType.BOUNDARY.getFlowMapName());
	boundaryPotentialMapName = boundaryFlowMap.addMap(boundaryPotentialMap);
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
	return boundaryFlowMap.getUnderlyingMap(boundaryPotentialMapName);
    }

    /**
     * Gets the flow map repulsing at map borders and mainland.
     *
     * @return the flow map repulsing at map borders and mainland
     */
    public FlowMap getBoundaryFlowMap() {
	return boundaryFlowMap;
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public PotentialMap getFoodPotentialMap() {
	    return GlobalPathfindingMaps.this.getFoodPotentialMap();
	}

	public PotentialMap getBoundaryPotentialMap() {
	    return GlobalPathfindingMaps.this.getBoundaryPotentialMap();
	}
    }
}
