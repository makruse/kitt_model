package de.zmt.pathfinding;

import java.io.Serializable;

/**
 * Interface for listening to change propagated by a {@link MapChangeNotifier} .
 * 
 * @author mey
 *
 */
public interface MapChangeListener extends Serializable {
    /**
     * Invoked when the target map changed a value.<br>
     * <b>NOTE:</b> This must be invoked <b>after</b> the change has already
     * happened in order to allow the implementing class to react to the change.
     * 
     * @param x
     *            x-coordinate of updated location
     * @param y
     *            y-coordinate of updated location
     */
    void changed(int x, int y);
}