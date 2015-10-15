package de.zmt.pathfinding;

import java.io.Serializable;

/**
 * A notifier for changes in a {@link PathfindingMap}.
 * 
 * @author mey
 *
 */
public interface MapChangeNotifier {
    /**
     * Adds a listener that is notified when the map changes.
     * 
     * @param listener
     */
    void addListener(ChangeListener listener);

    /**
     * Removes a previously added listener.
     * 
     * @param listener
     */
    void removeListener(Object listener);

    /**
     * Interface for listening for changed propagated by a
     * {@link MapChangeNotifier}.
     * 
     * @author mey
     *
     */
    public interface ChangeListener extends Serializable {
        /**
	 * Invoked when the target map changed a value.<br>
	 * <b>NOTE:</b> This must be invoked <b>after</b> the change has already
	 * happened in order to allow the implementing class to react to the
	 * change.
	 * 
	 * @param x
	 *            x-coordinate of updated location
	 * @param y
	 *            y-coordinate of updated location
	 */
        void changed(int x, int y);
    }

}