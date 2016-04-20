package de.zmt.pathfinding.filter;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.BooleanGrid2D;
import sim.field.grid.DoubleGrid2D;

/**
 * Skeletal implementation of a {@link GridFilteringOp}.
 * 
 * @author mey
 *
 */
abstract class AbstractGridFilteringOp implements GridFilteringOp {
    private final EdgeHandler edgeHandler;

    /**
     * Constructs a new {@link AbstractGridFilteringOp} with
     * {@link EdgeHandler#getDefault()}.
     */
    public AbstractGridFilteringOp() {
	this.edgeHandler = EdgeHandler.getDefault();
    }

    /**
     * Constructs a new {@link AbstractGridFilteringOp}. For handling grid edges
     * the given {@link EdgeHandler} is used.
     * 
     * @param edgeHandler
     *            the edge handler to be used
     */
    public AbstractGridFilteringOp(EdgeHandler edgeHandler) {
	super();
	this.edgeHandler = edgeHandler;
    }

    /**
     * Performs a filtering operation on a DoubleGrid2D. Each cell of the source
     * grid will be filtered. The results are written into a copy of {@code src}
     * and returned .
     * 
     * @param src
     *            the source grid
     * @return the resulting grid
     */
    public DoubleGrid2D filter(DoubleGrid2D src) {
	return filter(src, null, null);
    }

    /**
     * Performs a filtering operation on a DoubleGrid2D. Each cell of the source
     * grid will be filtered. The results are written into a copy of {@code src}
     * and returned.
     * 
     * @param src
     *            the source grid
     * @param dest
     *            the destination for filtered {@code src} or <code>null</code>
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dest) {
	return filter(src, dest, null);
    }

    /**
     * Performs a filtering operation only on the cells that are marked with a
     * flag. If the destination grid is <code>null</code> a copy of {@code src}
     * will be created and used as destination.
     * 
     * @param src
     *            the source grid
     * @param selection
     *            the selection grid containing a flag for every cell if it is
     *            to be included in the filtering operation
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, BooleanGrid2D selection) {
	return filter(src, null, selection);
    }

    /**
     * Performs a filtering operation only on the cells that are marked with a
     * flag. If the destination grid is <code>null</code> a copy of {@code src}
     * will be created and used as destination.
     * 
     * @param src
     *            the source grid
     * @param dest
     *            the destination for filtered {@code src} or <code>null</code>
     * @param selection
     *            the selection grid containing a flag for every cell if it is
     *            to be included in the filtering operation
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dest, BooleanGrid2D selection) {
	if (src == dest) {
	    throw new IllegalArgumentException("Source and destination grids must be different objects.");
	}

	int width = src.getWidth();
	int height = src.getHeight();

	if (dest == null) {
	    dest = new DoubleGrid2D(src);
	}

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		if (selection == null || selection.get(x, y)) {
		    dest.set(x, y, filter(x, y, src));
		}
		// not selected, copy from source
		else {
		    dest.set(x, y, src.get(x, y));
		}
	    }
	}

	return dest;
    }

    @Override
    public abstract double filter(int x, int y, DoubleGrid2D src);

    @Override
    public abstract int getxExtend();

    @Override
    public abstract int getyExtend();

    @Override
    public EdgeHandler getEdgeHandler() {
	return edgeHandler;
    }
}