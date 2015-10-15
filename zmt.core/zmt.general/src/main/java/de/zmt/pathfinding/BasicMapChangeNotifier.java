package de.zmt.pathfinding;

import java.io.Serializable;
import java.util.*;

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
abstract class BasicMapChangeNotifier implements MapChangeNotifier, Serializable {
    private static final long serialVersionUID = 1L;

    private final Collection<MapChangeListener> mapChangeListeners = new ArrayList<>(0);

    @Override
    public final void addListener(MapChangeListener listener) {
	mapChangeListeners.add(listener);
    }

    @Override
    public final void removeListener(Object listener) {
	mapChangeListeners.remove(listener);
    }

    /**
     * Notify listeners of a changed location. Call this <b>after</b> the change
     * happened in child class.
     * 
     * @see MapChangeListener#changed(int, int)
     * 
     * @param x
     *            x-coordinate of changed location
     * @param y
     *            y-coordinate of changed location
     */
    protected final void notifyListeners(int x, int y) {
	for (MapChangeListener listener : mapChangeListeners) {
	    listener.changed(x, y);
	}
    }
}