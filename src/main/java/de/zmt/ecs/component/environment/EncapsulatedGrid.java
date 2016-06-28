package de.zmt.ecs.component.environment;

import sim.field.grid.Grid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;

/**
 * Class encapsulating a {@link Grid2D} providing portrayable and accessors
 * 
 * @author mey
 *
 * @param <T>
 *            the type of grid that is encapsulated
 */
class EncapsulatedGrid<T extends Grid2D> implements ProvidesPortrayable<FieldPortrayable<T>> {

    /** The encapsulated grid. */
    private final T grid;

    /**
     * Constructs an {@link EncapsulatedGrid} from given grid.
     * 
     * @param grid
     *            the grid to be encapsulated
     */
    public EncapsulatedGrid(T grid) {
        super();
        this.grid = grid;
    }

    protected T getGrid() {
        return grid;
    }

    /** @return the width of the grid */
    public int getWidth() {
        return grid.getWidth();
    }

    /** @return the height of the grid */
    public int getHeight() {
        return grid.getHeight();
    }

    @Override
    public FieldPortrayable<T> providePortrayable() {
        return new FieldPortrayable<T>() {

            @Override
            public T getField() {
                return grid;
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[width=" + grid.getWidth() + ", height=" + grid.getHeight() + "]";
    }

}