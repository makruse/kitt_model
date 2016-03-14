package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;

/**
 * Map storing a potential value for every location. Negative values repel,
 * positive ones attract. Returned values and those in portrayable grid should
 * be in range of {@value #MAX_ATTRACTIVE_VALUE} and
 * {@value #MAX_REPULSIVE_VALUE}, e.g. for visualization.
 * 
 * @author mey
 *
 */
// TODO when in java 8, make default methods getEdgeHint, getName
// and delete interfaces
public interface PotentialMap extends PathfindingMap, ProvidesPortrayable<FieldPortrayable<DoubleGrid2D>> {
    /** Value for maximum repulsion. */
    public static final double MAX_REPULSIVE_VALUE = -1;
    /** Value for maximum attraction. */
    public static final double MAX_ATTRACTIVE_VALUE = 1;

    /**
     * Retrieve potential for given location.
     * 
     * @param x
     * @param y
     * @return potential at given location
     */
    double obtainPotential(int x, int y);
}
