package de.zmt.pathfinding;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.portrayable.*;
import sim.util.Double2D;

/**
 * Map storing flow directions for every location.
 * 
 * @author mey
 *
 */
public interface FlowMap extends PathfindingMap, ProvidesPortrayable<FieldPortrayable<ObjectGrid2D>> {
    /**
     * Obtains flow direction vector for given location.
     * 
     * @param x
     * @param y
     * @return direction vector at given location
     */
    Double2D obtainDirection(int x, int y);
}
