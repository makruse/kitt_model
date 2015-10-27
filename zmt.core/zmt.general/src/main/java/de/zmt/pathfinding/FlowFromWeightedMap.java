package de.zmt.pathfinding;

import java.util.*;

/**
 * Associates added maps with a weight.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying maps
 */
abstract class FlowFromWeightedMap<T extends PathfindingMap> extends DerivedFlowMap<T> {
    private static final long serialVersionUID = 1L;

    public static final double NEUTRAL_WEIGHT = 1d;

    /** {@code Map} pointing from pathfinding map to the objects wrapping it. */
    private final Map<T, Double> weights = new HashMap<>();

    public FlowFromWeightedMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds {@code map} and associate it with a weight.<br>
     * <b>NOTE:</b> Each instance of a map can only be associated with one
     * weight. If an instances is added more than once, all instances will be
     * associated with the weight given last.
     * 
     * @see DerivedFlowMap#addMap(PathfindingMap)
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(T map, double weight) {
	// need to set weight before adding which triggers update
	weights.put(map, weight);
	if (super.addMap(map)) {
	    return true;
	}
	// could not add map, remove weight again
	weights.remove(map);
	return false;
    }


    /**
     * Re-associates a map with a weight.
     * 
     * @param map
     * @param weight
     * @return weight that was associated with the map before
     */
    public final double setWeight(T map, double weight) {
	Double oldWeight = weights.put(map, weight);
	forceUpdateAll();

	if (oldWeight != null) {
	    return oldWeight;
	}
	return NEUTRAL_WEIGHT;
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.
     * 
     * @param map
     * @return weight factor for {@code map}
     */
    protected final double obtainWeight(T map) {
	Double weight = weights.get(map);
	if (weight != null) {
	    return weight;
	}
	return NEUTRAL_WEIGHT;
    }

    /** Associates given map with a neutral weight. */
    @Override
    public boolean addMap(T map) {
	return super.addMap(map);
    }

    @Override
    public boolean removeMap(Object map) {
	if (super.removeMap(map)) {
	    weights.remove(map);
	    return true;
	}
	return false;
    }
}
