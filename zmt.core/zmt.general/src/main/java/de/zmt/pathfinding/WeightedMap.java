package de.zmt.pathfinding;

import java.io.Serializable;

/**
 * A decorator class adding a weight factor to a {@link PathfindingMap} for use
 * in combining maps.
 * 
 * @author mey
 *
 * @param <T>
 *            type of PathfindingMap
 */
class WeightedMap<T extends PathfindingMap> implements PathfindingMap, Serializable {
    private static final long serialVersionUID = 1L;

    private final T map;
    private final double weight;

    /**
     * Constructs a new decorator class with given {@code map} and
     * {@code weight}.
     * 
     * @param map
     * @param weight
     */
    public WeightedMap(T map, double weight) {
	super();
	this.map = map;
	this.weight = weight;
    }

    /**
     * Accessor to map object for child classes.
     * 
     * @return map
     */
    protected T getMap() {
        return map;
    }

    /**
     * 
     * @return weight factor associated with this map
     */
    public double getWeight() {
        return weight;
    }

    @Override
    public int getWidth() {
	return getMap().getWidth();
    }

    @Override
    public int getHeight() {
	return getMap().getWidth();
    }

    /**
     * {@code final} because this object and its referenced map need to pass the
     * equality check.
     * 
     * @see FlowFromWeightedMap#removeMap(Object)
     */
    @Override
    public final int hashCode() {
	return getMap().hashCode();
    }

    /**
     * {@code final} because this object and its referenced map need to pass the
     * equality check.
     * 
     * @see FlowFromWeightedMap#removeMap(Object)
     */
    @Override
    public final boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getMap().equals(obj)) {
	    return true;
	}

	if (obj instanceof WeightedMap<?>) {
	    WeightedMap<?> other = (WeightedMap<?>) obj;
	    if (getMap() == null) {
		if (other.getMap() == null) {
		    return true;
		}
	    } else {
		return getMap().equals(other.getMap());
	    }
	}
	return false;
    }

}
