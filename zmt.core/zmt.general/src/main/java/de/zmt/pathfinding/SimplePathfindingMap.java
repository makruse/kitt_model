package de.zmt.pathfinding;

import sim.field.grid.Grid2D;

/**
 * A pathfinding map backed by a {@link Grid2D}.
 * 
 * @author mey
 *
 * @param <T>
 */
class SimplePathfindingMap<T extends Grid2D> implements PathfindingMap {

    private final T mapGrid;

    /**
     * Constructs a new {@code SimplePathfindingMap} backed by given grid.
     * 
     * @param mapGrid
     */
    public SimplePathfindingMap(T mapGrid) {
	this.mapGrid = mapGrid;
    }

    @Override
    public int getWidth() {
	return mapGrid.getWidth();
    }

    @Override
    public int getHeight() {
	return mapGrid.getHeight();
    }

    /**
     * 
     * @return {@link Grid2D} that backs this map.
     */
    public T getMapGrid() {
	return mapGrid;
    }
}