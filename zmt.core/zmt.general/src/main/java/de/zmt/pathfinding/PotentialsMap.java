package de.zmt.pathfinding;

/**
 * Map for retrieving potentials.
 * 
 * @author mey
 *
 */
public interface PotentialsMap extends PathfindingMap {

    /**
     * Retrieve potential for given location.
     * 
     * @param x
     * @param y
     * @return potential at given location
     */
    double obtainPotential(int x, int y);

}
