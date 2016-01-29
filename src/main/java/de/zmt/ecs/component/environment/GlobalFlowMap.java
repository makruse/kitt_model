package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;

/**
 * Global map simulating pathfinding flow from environmental influences.
 * 
 * @author mey
 *
 */
public class GlobalFlowMap extends FlowFromFlowsMap implements Component {
    private static final long serialVersionUID = 1L;

    private static final double WEIGHT_FOOD = 1;

    /** Stores combined flow of weighted risk and food. */
    private final FlowFromPotentialsMap flowFromPotentialsMap;

    /** {@code PotentialMap} for food. */
    private PotentialMap foodPotentialMap;

    public GlobalFlowMap(int width, int height) {
	super(width, height);
	flowFromPotentialsMap = new FlowFromPotentialsMap(width, height);
	addMap(flowFromPotentialsMap);
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
     * Provides food potentials portrayable.
     *
     * @return the field portrayable
     */
    public FieldPortrayable<DoubleGrid2D> provideFoodPotentialsPortrayable() {
	return foodPotentialMap.providePortrayable();
    }
}
