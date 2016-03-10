package de.zmt.pathfinding;

/**
 * {@link PathfindingMap} that has a name.
 * 
 * @author mey
 *
 */
interface NamedMap extends PathfindingMap {
    /**
     * Returns the name of this pathfinding map.
     * 
     * @return the name of this pathfinding map
     */
    String getName();
}
