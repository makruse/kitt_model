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

    private final Collection<ChangeListener> changeListeners = new ArrayList<>(0);

    @Override
    public final void addListener(ChangeListener listener) {
	changeListeners.add(listener);
    }

    @Override
    public final void removeListener(Object listener) {
	changeListeners.remove(listener);
    }

    /**
     * Notify listeners of a changed location. Call this <b>after</b> the change
     * happened in child class.
     * 
     * @see de.zmt.pathfinding.MapChangeNotifier.ChangeListener#changed(int,
     *      int)
     * 
     * @param x
     *            x-coordinate of changed location
     * @param y
     *            y-coordinate of changed location
     */
    protected final void notifyListeners(int x, int y) {
	for (ChangeListener listener : changeListeners) {
	    listener.changed(x, y);
	}
    }
}