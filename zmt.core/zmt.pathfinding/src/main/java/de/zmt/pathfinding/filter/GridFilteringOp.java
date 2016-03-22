package de.zmt.pathfinding.filter;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.DoubleGrid2D;

/**
 * A filtering operation from source to destination {@link DoubleGrid2D}.
 * 
 * @author mey
 *
 */
public interface GridFilteringOp {

    /**
     * Performs a filtering operation on a single grid cell.
     * 
     * @param x
     *            the x-coordinate of the cell
     * @param y
     *            the y-coordinate of the cell
     * @param src
     *            the source grid
     * @return the filtered value for that cell
     */
    double filter(int x, int y, DoubleGrid2D src);

    /**
     * Returns the x-extend of the filtering operation. For example, if the
     * operation takes only the direct neighbors into account both extends are
     * 1.
     * 
     * @return the x-extend of the filtering operation
     */
    int getxExtend();

    /**
     * Returns the y-extend of the filtering operation. For example, if the
     * operation takes only the direct neighbors into account both extends are
     * 1.
     * 
     * @return the y-extend of the filtering operation
     */
    int getyExtend();

    /**
     * Returns the {@link EdgeHandler} for this operation.
     * 
     * @return the {@link EdgeHandler} for this operation
     */
    EdgeHandler getEdgeHandler();

}