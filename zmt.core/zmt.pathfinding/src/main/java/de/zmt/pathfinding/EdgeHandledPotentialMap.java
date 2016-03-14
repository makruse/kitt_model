package de.zmt.pathfinding;

interface EdgeHandledPotentialMap extends PotentialMap {
    /**
     * Returns a handler for access to locations beyond map boundaries.
     * 
     * @return the edge handler
     */
    EdgeHandler getEdgeHandler();
}
