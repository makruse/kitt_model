package de.zmt.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides a basic implementation of the {@link MapChangeNotifier}
 * interface.
 * <p>
 * Child classes must call {@link #notifyListeners(int, int)} after the map
 * changes.
 * 
 * @author mey
 *
 */
class BasicMapChangeNotifier implements MapChangeNotifier, Serializable {
    private static final long serialVersionUID = 1L;

    /** Initial capacity for the listeners array list. */
    private static final int LISTENERS_INITIAL_CAPACITY = 1;

    private final Collection<DynamicMap> dynamicMaps = new ArrayList<>(LISTENERS_INITIAL_CAPACITY);
    private UpdateMode updateMode = UpdateMode.getDefault();

    @Override
    public final void addListener(DynamicMap listener) {
	dynamicMaps.add(listener);
    }

    @Override
    public final void removeListener(Object listener) {
	dynamicMaps.remove(listener);
    }

    @Override
    public void setUpdateMode(UpdateMode mode) {
	updateMode = mode;
    }

    /**
     * Notify listeners of a changed location. The way the changes are
     * propagated depends on the update mode.
     * 
     * @see #setUpdateMode(UpdateMode)
     * @param x
     *            x-coordinate of changed location
     * @param y
     *            y-coordinate of changed location
     */
    protected final void notifyListeners(int x, int y) {
	for (DynamicMap listener : dynamicMaps) {
	    switch (updateMode) {
	    case LAZY:
		listener.markDirty(x, y);
		break;
	    case EAGER:
		listener.forceUpdate(x, y);
		break;
	    default:
		throw new UnsupportedOperationException(updateMode + " not yet implemented.");
	    }
	}
    }

    /** Notifies listeners about a change of all locations. */
    protected final void notifyListenersAll() {
	for (DynamicMap listener : dynamicMaps) {
	    listener.forceUpdateAll();
	}
    }
}