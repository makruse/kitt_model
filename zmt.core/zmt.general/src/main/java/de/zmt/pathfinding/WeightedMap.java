package de.zmt.pathfinding;

import java.io.Serializable;

/**
 * A decorator class adding a weight factor to a {@link PathfindingMap} for use
 * in combining maps.<br>
 * <b>NOTE:</b> Weighted maps are equal to the referenced map in terms of equals
 * and hashCode methods.
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
	return getMap().getHeight();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[map=" + map + ", weight=" + weight + "]";
    }
}
