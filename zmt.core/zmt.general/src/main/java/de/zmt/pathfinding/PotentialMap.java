package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;
import sim.portrayal.portrayable.*;

/**
 * Map for retrieving potentials.
 * 
 * @author mey
 *
 */
public interface PotentialMap extends PathfindingMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {

    /**
     * Retrieve potential for given location.
     * 
     * @param x
     * @param y
     * @return potential at given location
     */
    double obtainPotential(int x, int y);

}
