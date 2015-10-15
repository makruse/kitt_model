package de.zmt.pathfinding;

/**
 * A flow map which combines functionality of {@link FlowFromFlowsMap} and
 * {@link FlowFromPotentialsMap}, so that both flow and potential maps can be
 * added.
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
     * @param potentialsWeight
     *            weight of the combined potential maps
     */
    public CombinedFlowMap(int width, int height, double potentialsWeight) {
	super(width, height);
	this.flowFromPotentialsMap = new FlowFromPotentialsMap(width, height);
	addMap(flowFromPotentialsMap, potentialsWeight);
    }

    /**
     * Adds a potential map to derive directions from.
     * 
     * @see DerivedFlowMap#addMap(PathfindingMap)
     * @param map
     * @return <code>true</code> if the map was added
     */
    public boolean addMap(PotentialMap map) {
	return flowFromPotentialsMap.addMap(map);
    }

    @Override
    public boolean removeMap(Object map) {
	// potential maps will be removed as well
	return super.removeMap(map) || flowFromPotentialsMap.removeMap(map);
    }
}
