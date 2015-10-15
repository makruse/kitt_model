package de.zmt.pathfinding;

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
    private static final double NEUTRAL_WEIGHT = 1d;

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
	return super.addMap(createWeightedMap(map, weight));
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
}
