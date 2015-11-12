package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;

import sim.util.Double2D;

/**
 * A flow map that returns directions from combining other flow maps. This is
 * done by adding and normalizing the directions at every location.
 * <p>
 * {@link PotentialMap}s can be added when wrapped into a
 * {@link FlowFromPotentialsMap}.
 * 
 * @author mey
 */
public class FlowFromFlowsMap extends DerivedFlowMap<FlowMap> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code CombinedFlowMap} with given dimensions.
     * 
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public FlowFromFlowsMap(int width, int height) {
	super(width, height);
    }

    /**
     * Constructs a new {@code CombinedFlowMap} with given flow map as its first
     * underlying map. This constructor is a shortcut copying the grid from
     * {@code underlyingMap} instead of updating every location individually.
     * 
     * @param underlyingMap
     *            the first underlying map
     */
    public FlowFromFlowsMap(GridBackedFlowMap underlyingMap) {
	super(underlyingMap.getWidth(), underlyingMap.getHeight());
	addMapInternal(underlyingMap);
	getMapGrid().setTo(underlyingMap.getMapGrid());
    }

    /**
     * Accumulates weighted directions from underlying maps.
     */
    @Override
    protected Double2D computeDirection(int x, int y) {
	if (getUnderlyingMaps().isEmpty()) {
	    return DIRECTION_NEUTRAL;
	}
	// if there is only one underlying map, return its direction
	if (getUnderlyingMaps().size() == 1) {
	    return getUnderlyingMaps().iterator().next().obtainDirection(x, y);
	}

	Double2D directionsSum = DIRECTION_NEUTRAL;
	for (FlowMap map : getUnderlyingMaps()) {
	    double weight = getWeight(map);
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
