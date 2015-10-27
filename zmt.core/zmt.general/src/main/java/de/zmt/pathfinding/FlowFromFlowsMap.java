package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.DIRECTION_NEUTRAL;

import sim.util.Double2D;

/**
 * A flow map that returns directions from combining other flow maps. This is
 * done by adding and normalizing the directions at every location. A weight
 * factor is associated with every flow map, which defines the influence each
 * map has on the final result.
 * 
 * @author mey
 *
 */
public class FlowFromFlowsMap extends DerivedFlowMap<FlowMap> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty map with given dimensions. All locations are
     * initialized to zero vectors.
     * 
     * @param width
     * @param height
     */
    public FlowFromFlowsMap(int width, int height) {
	super(width, height);
    }

    /** Accumulates weighted directions from underlying maps. */
    @Override
    protected Double2D computeDirection(int x, int y) {
	Double2D directionsSum = DIRECTION_NEUTRAL;
	for (FlowMap map : getIntegralMaps()) {
	    double weight = obtainWeight(map);
	    Double2D weightedDirection = map.obtainDirection(x, y).multiply(weight);
	    directionsSum = directionsSum.add(weightedDirection);
	}

	// check needed here: normalizing (0,0) will throw an exception
	if (directionsSum.equals(DIRECTION_NEUTRAL)) {
	    return DIRECTION_NEUTRAL;
	} else {
	    return directionsSum.normalize();
	}
    }
}
