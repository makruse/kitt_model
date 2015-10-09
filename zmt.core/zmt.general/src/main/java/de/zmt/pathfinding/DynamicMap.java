package de.zmt.pathfinding;

/**
 * A map used in pathfinding that notifies listeners about changes.
 * 
 * @author mey
 *
 */
public interface DynamicMap {
    /**
     * Adds a listener that is notified when the dynamic map changes.
     * 
     * @param listener
     */
    void addListener(ChangeListener listener);

    /**
     * Remove a previously added listener.
     * 
     * @param listener
     */
    void removeListener(Object listener);

    /**
     * Interface for listening to changes in a {@link DynamicMap}.
     * 
     * @author mey
     *
     */
    public interface ChangeListener {
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