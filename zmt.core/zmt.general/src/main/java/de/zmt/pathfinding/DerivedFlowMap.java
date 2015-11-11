package de.zmt.pathfinding;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from another underlying pathfinding map. Changes from the underlying map are
 * propagated automatically if it implements the {@link MapChangeNotifier}
 * interface.
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

    private final T underlyingMap;

    /**
     * Constructs a new DerivedFlowMap with given dimensions.<br>
     * <b>NOTE:</b> Implementing classes need to handle the initial update from
     * the underlying map, e.g. call {@link #forceUpdateAll()} in constructor.
     * <p>
     * <b>NOTE:</b> If {@code underlyingMap} implements
     * {@link MapChangeNotifier} a listener to this object is added to it. To
     * allow this object to be garbage-collected while the underlying map is
     * referred elsewhere, the reference has to be removed manually by calling
     * {@link MapChangeNotifier#removeListener(Object)}.
     * 
     * @param underlyingMap
     *            pathfinding map to derive directions from
     * 
     */
    public DerivedFlowMap(T underlyingMap) {
	super(underlyingMap.getWidth(), underlyingMap.getHeight());
	this.underlyingMap = underlyingMap;
	if (underlyingMap instanceof MapChangeNotifier) {
	    ((MapChangeNotifier) underlyingMap).addListener(this);
	}
    }

    public T getUnderlyingMap() {
	return underlyingMap;
    }

    @Override
    public void updateIfDirty(int x, int y) {
	// update underlying map before updating itself
	if (underlyingMap instanceof MapUpdateHandler) {
	    ((MapUpdateHandler) underlyingMap).updateIfDirty(x, y);
        }
        super.updateIfDirty(x, y);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[" + underlyingMap + "]";
    }
}