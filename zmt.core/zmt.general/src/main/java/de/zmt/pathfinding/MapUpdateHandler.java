package de.zmt.pathfinding;

/**
 * Classes can implement this interface to handle lazy updates to a
 * {@link PathfindingMap}. Locations can be marked dirty and updated
 * selectively.
 * 
 * @author mey
 *
 */
public interface MapUpdateHandler extends MapChangeNotifier {

    /**
     * Mark the given position and extends as dirty.
     * 
     * @param x
     * @param y
     */
    void markDirty(int x, int y);

    /**
     * Updates all locations independent from being marked dirty.
     */
    void forceUpdateAll();

    /**
     * Updates the given location if marked dirty.
     * 
     * @param x
     * @param y
     */
    void updateIfDirty(int x, int y);

    /**
     * Updates any location marked dirty.
     * 
     * @see #updateIfDirty(int, int)
     */
    void updateIfDirtyAll();

}