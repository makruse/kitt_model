package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;

/**
 * {@link PotentialMap} that is backed by a {@link DoubleGrid2D}.
 * 
 * @author mey
 *
 */
interface GridBackedPotentialMap extends PotentialMap {
    /**
     * Gets the grid which backs this map, containing a potential for every
     * location as double value.
     * 
     * @return {@link DoubleGrid2D} which backs this map.
     */
    DoubleGrid2D getMapGrid();
}
