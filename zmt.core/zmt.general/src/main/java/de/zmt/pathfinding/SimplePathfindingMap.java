package de.zmt.pathfinding;

import sim.field.grid.Grid2D;
import sim.portrayal.portrayable.*;

/**
 * A pathfinding map backed by a {@link Grid2D}.
 * 
 * @author mey
 *
 * @param <T>
 */
class SimplePathfindingMap<T extends Grid2D> implements PathfindingMap, ProvidesPortrayable<FieldPortrayable<T>> {

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

    @Override
    public FieldPortrayable<T> providePortrayable() {
	return new FieldPortrayable<T>() {

	    @Override
	    public T getField() {
		return mapGrid;
	    }
	};
    }
}