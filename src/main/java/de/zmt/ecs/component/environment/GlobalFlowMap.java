package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.*;
import sim.util.Double2D;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalFlowMap extends CombinedFlowMap implements Component {
    private static final long serialVersionUID = 1L;

    private PotentialMap foodPotentialMap;
    private PotentialMap riskPotentialMap;
    private FlowFromPotentialsMap riskFlowMap;

    public GlobalFlowMap(int width, int height) {
	super(width, height);
	riskFlowMap = new FlowFromPotentialsMap(width, height);
    }

    /**
     * Obtains risk-only flow direction vector for given location.
     * 
     * @see #setRiskPotentialMap(PotentialMap)
     * @param x
     * @param y
     * @return risk-only direction vector at given location
     */
    public Double2D obtainRiskDirection(int x, int y) {
	return riskFlowMap.obtainDirection(x, y);
    }

    /**
     * Sets map containing food potentials and add it to the combined map. If
     * already set, the old one will be removed from the combined map as well.
     * 
     * @param foodPotentialMap
     */
    public void setFoodPotentialMap(PotentialMap foodPotentialMap) {
	if (this.foodPotentialMap != null) {
	    removeMap(this.foodPotentialMap);
	}
	this.foodPotentialMap = foodPotentialMap;
	addMap(foodPotentialMap);
    }

    /**
     * Sets potential map containing predation risk and add it to the combined
     * map. If already set, the old one will be removed from the combined map as
     * well.
     * 
     * @see #obtainRiskDirection(int, int)
     * @param riskPotentialMap
     */
    public void setRiskPotentialMap(PotentialMap riskPotentialMap) {
	if (this.riskPotentialMap != null) {
	    removeMap(this.riskPotentialMap);
	    riskFlowMap.removeMap(riskPotentialMap);
	}
	this.riskPotentialMap = riskPotentialMap;
	riskFlowMap.addMap(riskPotentialMap);
	addMap(riskPotentialMap);
    }
}
