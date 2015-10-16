package de.zmt.pathfinding;

import java.util.*;

/**
 * Associates added maps with a weight by wrapping them into a
 * {@link WeightedMap}. Implementing classes need to specify the concrete
 * wrapper object by the means of abstract method
 * {@link #createWeightedMap(PathfindingMap, double)}.
 * <p>
 * Not that maps added without weight using {@link #addMap(PathfindingMap)} are
 * not wrapped but stored as-is. To safely get the weight, use
 * {@link #obtainWeight(PathfindingMap)}.
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
    private final Map<T, Queue<WeightedMap<?>>> weightedMapQueues = new HashMap<>();

    public FlowFromWeightedMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds {@code map} and associate it with {@code weight}. This is done by
     * wrapping it into a decorator object.
     * 
     * @see DerivedFlowMap#addMap(PathfindingMap)
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(T map, double weight) {
	T weightedMap = createWeightedMap(map, weight);

	Queue<WeightedMap<?>> weightedMapQueue = weightedMapQueues.get(map);
	// create collection if needed
	if (weightedMapQueue == null) {
	    weightedMapQueue = new ArrayDeque<>(1);
	    weightedMapQueues.put(map, weightedMapQueue);
	}
	// register wrapper object
	weightedMapQueue.add((WeightedMap<?>) weightedMap);
	return super.addMap(weightedMap);
    }

    /**
     * Obtains weight associated with map. If there is no weight associated a
     * neutral factor is returned.<br>
     * <b>NOTE:</b> This should be called for maps fetched from
     * {@link #getIntegralMaps()}. Internally it simply checks for being
     * instance of WeightedMap and return that weight, or the neutral factor if
     * not.
     * 
     * @param map
     * @return weight factor of {@code map}
     */
    protected final double obtainWeight(T map) {
	if (map instanceof WeightedMap<?>) {
	    return ((WeightedMap<?>) map).getWeight();
	}
	return NEUTRAL_WEIGHT;
    }

    /**
     * Associate given {@code map} with {@code weight} and return the wrapper
     * object.
     * 
     * @param map
     * @param weight
     * @return wrapper object
     */
    protected abstract T createWeightedMap(T map, double weight);

    /**
     * Removes an underlying pathfinding map.<br>
     * <b>NOTE:</b> If the same map is added more than once, only one entry is
     * removed from the internal collection. There is no way to specify which
     * entry is to be removed, even though different weights can be associated.
     */
    @Override
    public boolean removeMap(Object map) {
	Queue<WeightedMap<?>> weightedMapQueue = weightedMapQueues.get(map);
	if (weightedMapQueue != null) {
	    WeightedMap<?> weightedMap = weightedMapQueue.remove();
	    // remove queue if empty
	    if (weightedMapQueue.isEmpty()) {
		weightedMapQueues.remove(map);
	    }
	    return super.removeMap(weightedMap);
	}
	return super.removeMap(map);
    }
}
