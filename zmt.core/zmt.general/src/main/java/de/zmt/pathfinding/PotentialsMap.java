package de.zmt.pathfinding;

/**
 * Map storing potentials per cell.
 * 
 * @author mey
 *
 */
public interface PotentialsMap extends PathfindingMap {

    /**
     * Retrieve potential for given position.
     * 
     * @param x
     * @param y
     * @return potential at given position
     */
    double obtainPotential(int x, int y);

}
