package de.zmt.pathfinding;

/**
 * Associates added maps with a weight by wrapping them into a
 * {@link WeightedMap}. Implementing classes need to specify the concrete
 * wrapper object by the means of abstract method
 * {@link #createWeightedMap(PathfindingMap, double)}.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying maps
 */
abstract class FlowFromWeightedMap<T extends PathfindingMap> extends DerivedFlowMap<T> {
    private static final long serialVersionUID = 1L;

    public FlowFromWeightedMap(int width, int height) {
	super(width, height);
    }

    /**
     * Adds {@code map} and associate it with {@code weight}.
     * 
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     * 
     * @see DerivedFlowMap#addMap(PathfindingMap)
     */
    public boolean addMap(T map, double weight) {
	return super.addMap(createWeightedMap(map, weight));
    }

    /** Adds a map and associate it with a neutral weight. */
    @Override
    public boolean addMap(T map) {
	return addMap(map, WeightedMap.NEUTRAL_WEIGHT);
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
