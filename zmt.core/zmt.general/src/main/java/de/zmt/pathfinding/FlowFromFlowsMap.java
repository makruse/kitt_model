package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.DIRECTION_NEUTRAL;

import java.util.*;

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
    private static final double NEUTRAL_WEIGHT = 1d;

    private final Map<FlowMap, Double> weights = new HashMap<>();

    public FlowFromFlowsMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds {@code map} and associate it with {@code weight}.
     * 
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     * 
     * @see #addMap(FlowMap)
     */
    public boolean addMap(FlowMap map, double weight) {
	weights.put(map, weight);
	if (super.addMap(map)) {
	    return true;
	} else {
	    weights.remove(map);
	    return false;
	}
    }

    /** Adds a map and associate it with a neutral weight. */
    @Override
    public boolean addMap(FlowMap map) {
	return addMap(map, NEUTRAL_WEIGHT);
    }

    @Override
    public boolean removeMap(Object map) {
	weights.remove(map);
	return super.removeMap(map);
    }

    /** Accumulates weighted directions from underlying maps. */
    @Override
    protected Double2D computeDirection(int x, int y) {
	Double2D directionsSum = DIRECTION_NEUTRAL;
	for (FlowMap map : integralMaps) {
	    Double2D weightedDirection = map.obtainDirection(x, y).multiply(weights.get(map));
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
