package de.zmt.pathfinding;

/**
 * A flow map which combines workings of {@link FlowFromFlowsMap} and
 * {@link FlowFromPotentialsMap}, so that both flow and potential maps can be
 * added.
 * <p>
 * The weight of the internal {@code FlowFromPotentialsMap} will match the
 * accumulated weights of the added potential maps, so that weights of potential
 * maps are reflected correctly in the final direction calculated by
 * {@code FlowFromFlowsMap}.
 * 
 * @author mey
 *
 */
public class CombinedFlowMap extends FlowFromFlowsMap {
    private static final long serialVersionUID = 1L;

    private final FlowFromPotentialsMap flowFromPotentialsMap;

    /**
     * Constructs an empty map with given dimensions. All locations are
     * initialized to zero vectors.
     * 
     * @param width
     * @param height
     */
    public CombinedFlowMap(int width, int height) {
	super(width, height);
	this.flowFromPotentialsMap = new FlowFromPotentialsMap(width, height);
    }

    /**
     * Adds a potential map to derive directions from.
     * 
     * @param map
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(PotentialMap map) {
	return updateWeightAfterPotentialMapsChange(flowFromPotentialsMap.addMap(map));
    }

    /**
     * Adds a potential map to derive directions from and associate it with a
     * weight.
     * 
     * @param map
     * @param weight
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(PotentialMap map, double weight) {
	return updateWeightAfterPotentialMapsChange(flowFromPotentialsMap.addMap(map, weight));
    }

    @Override
    public boolean removeMap(Object map) {
	// potential maps will be removed as well
	return super.removeMap(map) || updateWeightAfterPotentialMapsChange(flowFromPotentialsMap.removeMap(map));
    }

    /**
     * Updates weight in {@code FlowFromFlowsMap} after potential maps have been
     * added or removed. By passing {@code addResult} this method can be used in
     * chaining operations as an extension of the used {@code addMap} method.
     * 
     * @param addResult
     *            return result of the add operation
     * @return {@code addResult} for chaining
     */
    private boolean updateWeightAfterPotentialMapsChange(boolean addResult) {
	if (addResult) {
	    super.removeMap(flowFromPotentialsMap);

	    // accumulate weights of potential maps...
	    double accumulatedWeight = 0;
	    for (PotentialMap potentialMap : flowFromPotentialsMap.getIntegralMaps()) {
		accumulatedWeight += flowFromPotentialsMap.obtainWeight(potentialMap);
	    }

	    // ... and set it as weight for the flow map containing them
	    addMap(flowFromPotentialsMap, accumulatedWeight);
	    return true;
	}
	return false;
    }
}
