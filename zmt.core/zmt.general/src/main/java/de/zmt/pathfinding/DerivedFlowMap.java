package de.zmt.pathfinding;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. If an underlying map changes and
 * these changes should be reflected, it must implement the
 * {@link MapChangeNotifier} interface. Weights can be associated with
 * underlying maps to control its impact in the final result.
 * <p>
 * Implementing classes need to specify abstract
 * {@link #computeDirection(int, int)} which is called when an update is needed.
 * Otherwise directions are fetched from a grid where results of that method are
 * cached.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying maps
 */
abstract class DerivedFlowMap<T extends PathfindingMap> extends ListeningFlowMap {
    private static final long serialVersionUID = 1L;

    private final T integralMap;

    /**
     * Constructs a new DerivedFlowMap with given dimensions.<br>
     * <b>NOTE:</b> Implementing classes need to handle the initial update from
     * the integral map, e.g. call {@link #forceUpdateAll()} in constructor.
     * 
     * @param integralMap
     *            pathfinding map to derive directions from
     * 
     */
    public DerivedFlowMap(T integralMap) {
	super(integralMap.getWidth(), integralMap.getHeight());
	this.integralMap = integralMap;
	if (integralMap instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) integralMap).addListener(getMyChangeListener());
	}
    }

    T getIntegralMap() {
	return integralMap;
    }

    @Override
    public void updateIfDirty(int x, int y) {
        // update integral map before updating itself
        if (integralMap instanceof MapUpdateHandler) {
            ((MapUpdateHandler) integralMap).updateIfDirty(x, y);
        }
        super.updateIfDirty(x, y);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[" + integralMap + "]";
    }
}