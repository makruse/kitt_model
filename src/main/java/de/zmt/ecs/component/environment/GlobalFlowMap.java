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
public class GlobalFlowMap extends FlowFromFlowsMap implements Component {
    private static final long serialVersionUID = 1L;

    private static final double WEIGHT_FOOD = 1;
    private static final double WEIGHT_RISK = 2;

    /** Stores combined flow of weighted risk and food. */
    private FlowFromPotentialsMap flowFromPotentialsMap;

    /** {@code PotentialMap} for food. */
    private PotentialMap foodPotentialMap;
    /** {@code PotentialMap} for risk. */
    private PotentialMap riskPotentialMap;

    /** Risk flow calculated only from {@link #riskPotentialMap}. */
    private FlowFromPotentialsMap riskFlowMap;

    public GlobalFlowMap(int width, int height) {
	super(width, height);
	flowFromPotentialsMap = new FlowFromPotentialsMap(width, height);
	riskFlowMap = new FlowFromPotentialsMap(width, height);
	addMap(flowFromPotentialsMap);
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
	    flowFromPotentialsMap.removeMap(foodPotentialMap);
	}
	this.foodPotentialMap = foodPotentialMap;
	flowFromPotentialsMap.addMap(foodPotentialMap, WEIGHT_FOOD);
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
	    flowFromPotentialsMap.removeMap(riskPotentialMap);
	    riskFlowMap.removeMap(riskPotentialMap);
	}
	this.riskPotentialMap = riskPotentialMap;
	flowFromPotentialsMap.addMap(riskPotentialMap, WEIGHT_RISK);
	riskFlowMap.addMap(riskPotentialMap);
    }

    /**
     * Provides food potentials portrayable.
     *
     * @return the field portrayable
     */
    public FieldPortrayable<DoubleGrid2D> provideFoodPotentialsPortrayable() {
	return foodPotentialMap.providePortrayable();
    }

    /**
     * Provides risk potentials portrayable.
     *
     * @return the field portrayable
     */
    public FieldPortrayable<DoubleGrid2D> provideRiskPotentialsPortrayable() {
	return riskPotentialMap.providePortrayable();
    }
}
