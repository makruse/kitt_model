package de.zmt.pathfinding;

import sim.util.Double2D;

/**
 * Map storing flow directions per cell.
 * 
 * @author mey
 *
 */
public interface FlowMap extends PathfindingMap {
    /**
     * Obtain flow direction vector for given position.\
     * 
     * @param x
     * @param y
     * @return direction vector at given position
     */
    Double2D obtainDirection(int x, int y);
}
