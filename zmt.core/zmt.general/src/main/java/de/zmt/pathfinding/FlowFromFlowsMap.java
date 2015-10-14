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

    private static final double NEUTRAL_WEIGHT = 1d;

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
	return super.addMap(new WeightedFlowMap(map, weight));
    }

    /** Adds a map and associate it with a neutral weight. */
    @Override
    public boolean addMap(FlowMap map) {
	return addMap(map, NEUTRAL_WEIGHT);
    }

    @Override
    public boolean removeMap(Object map) {
	if (map instanceof FlowMap) {
	    FlowMap flowMap = (FlowMap) map;
	    // will work with any weight, not checked in equals method
	    return super.removeMap(new WeightedFlowMap(flowMap, NEUTRAL_WEIGHT));
	}
	return false;
    }

    /** Accumulates weighted directions from underlying maps. */
    @Override
    protected Double2D computeDirection(int x, int y) {
	Double2D directionsSum = DIRECTION_NEUTRAL;
	for (FlowMap map : getIntegralMaps()) {
	    double weight = ((WeightedFlowMap) map).getWeight();
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

    private static class WeightedFlowMap implements FlowMap {
	private final FlowMap flowMap;
	private final double weight;

	public WeightedFlowMap(FlowMap flowMap, double weight) {
	    super();
	    this.flowMap = flowMap;
	    this.weight = weight;
	}

	@Override
	public int getWidth() {
	    return flowMap.getWidth();
	}

	@Override
	public Double2D obtainDirection(int x, int y) {
	    return flowMap.obtainDirection(x, y);
	}

	@Override
	public int getHeight() {
	    return flowMap.getHeight();
	}

	public double getWeight() {
	    return weight;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((flowMap == null) ? 0 : flowMap.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    WeightedFlowMap other = (WeightedFlowMap) obj;
	    if (flowMap == null) {
		if (other.flowMap != null) {
		    return false;
		}
	    } else if (!flowMap.equals(other.flowMap)) {
		return false;
	    }
	    return true;
	}
    }
}
