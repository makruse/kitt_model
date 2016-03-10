package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.NEUTRAL;

import sim.util.Double2D;

/**
 * A flow map that returns directions from combining other flow maps. This is
 * done by adding and normalizing the directions at every location.
 * <p>
 * {@link PotentialMap}s can be added when wrapped into a
 * {@link FlowFromPotentialsMap}.
 * 
 * @see "Moersch et al. 2013, Hybrid Vector Field Pathfinding, p. 14"
 * @see "Hagelbäck 2012, Potential-Field Based navigation in StarCraft, p. 2"
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
     * underlying map.
     * 
     * @param firstMap
     *            the first underlying map
     * @param other
     *            the other content
     */
    public FlowFromFlowsMap(FlowMap firstMap, MapContent<FlowMap> other) {
	super(firstMap.getWidth(), firstMap.getHeight());

	// speedup if grid-backed
	if (firstMap instanceof GridBackedFlowMap) {
	    addMapInternal(firstMap);
	    getMapGrid().setTo(((GridBackedFlowMap) firstMap).getMapGrid());
	} else {
	    addMap(firstMap);
	}

	changeStructure(other);
    }

    /**
     * Accumulates weighted directions from underlying maps.
     */
    @Override
    protected Double2D computeDirection(int x, int y) {
	if (getUnderlyingMaps().isEmpty()) {
	    return NEUTRAL;
	}
	// if there is only one underlying map, return its direction
	if (getUnderlyingMaps().size() == 1) {
	    return getUnderlyingMaps().iterator().next().obtainDirection(x, y);
	}

	Double2D directionsSum = NEUTRAL;
	for (FlowMap map : getUnderlyingMaps()) {
	    double weight = getWeight(map);
	    Double2D weightedDirection = map.obtainDirection(x, y).multiply(weight);
	    directionsSum = directionsSum.add(weightedDirection);
	}

	// check needed here: normalizing (0,0) will throw an exception
	if (directionsSum.equals(NEUTRAL)) {
	    return NEUTRAL;
	} else {
	    return directionsSum.normalize();
	}
    }
}
