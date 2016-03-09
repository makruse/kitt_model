package de.zmt.pathfinding;

/**
 * A dynamic map with changing content. Locations can be updated selectively or
 * marked dirty to postpone update until it is needed.
 * 
 * @author mey
 *
 */
public interface DynamicMap extends PathfindingMap {

    /**
     * Marks the given position and extends as dirty.
     * 
     * @param x
     * @param y
     */
    void markDirty(int x, int y);

    /**
     * Updates the given position independent of being marked dirty.
     * 
     * @param x
     * @param y
     */
    void forceUpdate(int x, int y);

    /** Updates all locations independent from being marked dirty. */
    void forceUpdateAll();

    /**
     * Updates the given location if marked dirty.
     * 
     * @param x
     * @param y
     */
    void updateIfDirty(int x, int y);

    /** Updates any location marked dirty. */
    void updateIfDirtyAll();

}