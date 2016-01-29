package de.zmt.pathfinding;

import java.io.Serializable;

import sim.field.grid.Grid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;

/**
 * A pathfinding map backed by a {@link Grid2D}.
 * 
 * @author mey
 *
 * @param <T>
 *            type of grid
 */
class SimplePathfindingMap<T extends Grid2D>
	implements PathfindingMap, ProvidesPortrayable<FieldPortrayable<T>>, Serializable {
    private static final long serialVersionUID = 1L;

    /** The grid which backs this map. */
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
     * Gets the grid which backs this map.
     * 
     * @return {@link Grid2D} which backs this map.
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