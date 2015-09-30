package de.zmt.pathfinding;

import sim.util.Double2D;

public interface FlowMap {
    /**
     * Obtain flow direction vector for given position.
     * 
     * @param x
     * @param y
     * @return flow direction vector
     */
    Double2D obtainDirection(int x, int y);
}
