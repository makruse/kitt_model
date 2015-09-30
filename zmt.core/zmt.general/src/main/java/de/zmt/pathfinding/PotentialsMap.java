package de.zmt.pathfinding;

public interface PotentialsMap {
    /**
     * Obtain potential for given position.
     * 
     * @param x
     * @param y
     * @return potential value
     */
    double obtainPotential(int x, int y);
}
