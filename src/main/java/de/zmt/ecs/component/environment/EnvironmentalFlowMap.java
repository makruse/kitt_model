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
public class EnvironmentalFlowMap implements FlowMap, Component {
    private static final long serialVersionUID = 1L;

    private final CombinedFlowMap combinedFlowMap;
    private PotentialMap foodPotentialMap;
    private PotentialMap riskPotentialMap;
    private FlowFromPotentialsMap riskFlowMap;

    public EnvironmentalFlowMap(int width, int height) {
	combinedFlowMap = new CombinedFlowMap(width, height);
	riskFlowMap = new FlowFromPotentialsMap(width, height);
    }

    /**
     * Obtains risk-only flow direction vector for given location.
     * 
     * @param x
     * @param y
     * @return direction vector at given location
     */
    public Double2D obtainRiskDirection(int x, int y) {
	return riskFlowMap.obtainDirection(x, y);
    }

    public void setFoodPotentialMap(PotentialMap foodPotentialMap) {
	if (this.foodPotentialMap != null) {
	    combinedFlowMap.removeMap(this.foodPotentialMap);
	}
	this.foodPotentialMap = foodPotentialMap;
	combinedFlowMap.addMap(foodPotentialMap);
    }

    public void setRiskPotentialMap(PotentialMap riskPotentialMap) {
	if (this.riskPotentialMap != null) {
	    combinedFlowMap.removeMap(this.riskPotentialMap);
	    riskFlowMap.removeMap(riskPotentialMap);
	}
	this.riskPotentialMap = riskPotentialMap;
	riskFlowMap.addMap(riskPotentialMap);
	combinedFlowMap.addMap(riskPotentialMap);

    }

    /**
     * Re-associates a map with a weight.
     * 
     * @param map
     * @param weight
     * @return weight that was associated with the map before
     */
    public final double setWeight(PotentialMap map, double weight) {
	return combinedFlowMap.setWeight(map, weight);
    }

    /**
     * Re-associates a map with a weight.
     * 
     * @param map
     * @param weight
     * @return weight that was associated with the map before
     */
    public final double setWeight(FlowMap map, double weight) {
	return combinedFlowMap.setWeight(map, weight);
    }

    @Override
    public int getWidth() {
	return combinedFlowMap.getWidth();
    }

    @Override
    public int getHeight() {
	return combinedFlowMap.getHeight();
    }

    /** Obtains combined flow direction vector for given location. */
    @Override
    public Double2D obtainDirection(int x, int y) {
	return combinedFlowMap.obtainDirection(x, y);
    }

}
