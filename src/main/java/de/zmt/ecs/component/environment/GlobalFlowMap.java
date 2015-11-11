package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalFlowMap extends CombinedFlowMap implements Component {
    private static final long serialVersionUID = 1L;

    private FlowFromPotentialMap foodMap;
    private FlowFromPotentialMap riskMap;

    public GlobalFlowMap(int width, int height) {
	super(width, height);
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
	return riskMap.obtainDirection(x, y);
    }

    /**
     * Sets map containing food potentials and add it to the combined map. If
     * already set, the old one will be removed from the combined map as well.
     * 
     * @param foodPotentialMap
     */
    public void setFoodPotentialMap(PotentialMap foodPotentialMap) {
	if (this.foodMap != null) {
	    removeMap(foodMap);
	}
	foodMap = new FlowFromPotentialMap(foodPotentialMap);
	addMap(foodMap);
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
	if (riskMap != null) {
	    removeMap(riskMap);
	}
	riskMap = new FlowFromPotentialMap(riskPotentialMap);
	addMap(riskMap);
    }

    public FieldPortrayable<DoubleGrid2D> provideFoodPotentialsPortrayable() {
	return foodMap.getUnderlyingMap().providePortrayable();
    }

    public FieldPortrayable<DoubleGrid2D> provideRiskPotentialsPortrayable() {
	return riskMap.getUnderlyingMap().providePortrayable();
    }
}
