package de.zmt.pathfinding;

import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;

/**
 * {@link FlowMap} that is backed by an {@link ObjectGrid2D}.
 * 
 * @author mey
 *
 */
interface GridBackedFlowMap extends FlowMap {
    /**
     * Gets the grid which backs this map, containing a direction as
     * {@link Double2D} object for every location.
     * 
     * @return {@link ObjectGrid2D} which backs this map.
     */
    ObjectGrid2D getMapGrid();
}
