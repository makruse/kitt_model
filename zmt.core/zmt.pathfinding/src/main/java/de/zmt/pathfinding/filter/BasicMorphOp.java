package de.zmt.pathfinding.filter;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.DoubleGrid2D;

/**
 * Abstract super class for the basic morph operations {@link Dilate} and
 * {@link Erode}.
 * 
 * @author mey
 *
 */
public abstract class BasicMorphOp extends AbstractGridFilteringOp {
    private static final Dilate DEFAULT_DILATE_INSTANCE = new Dilate();
    private static final Erode DEFAULT_ERODE_INSTANCE = new Erode();

    /**
     * Constructs a new {@link BasicMorphOp} with
     * {@link EdgeHandler#getDefault()}.
     */
    private BasicMorphOp() {
	super();
    }

    /**
     * Constructs a new {@link BasicMorphOp}. For handling grid edges the given
     * {@link EdgeHandler} is used.
     * 
     * @param edgeHandler
     *            the edge handler to be used
     */
    private BasicMorphOp(EdgeHandler edgeHandler) {
	super(edgeHandler);
    }

    abstract double updateResult(double result, double value);

    @Override
    public double filter(int x, int y, DoubleGrid2D src) {
	double result = src.get(x, y);
	for (int i = x - 1; i <= x + 1; i++) {
	    for (int j = y - 1; j <= y + 1; j++) {
		double value = getEdgeHandler().getValue(src, i, j);
		result = updateResult(result, value);
	    }
	}
	return result;
    }

    @Override
    public int getxExtend() {
	return 1;
    }

    @Override
    public int getyExtend() {
	return 1;
    }

    /**
     * Returns the default {@link Dilate} instance with
     * {@link EdgeHandler#getDefault()}.
     * 
     * @return the default {@link Dilate} instance
     */
    public static Dilate getDefaultDilate() {
	return DEFAULT_DILATE_INSTANCE;
    }

    /**
     * Returns the default {@link Erode} instance with
     * {@link EdgeHandler#getDefault()}.
     * 
     * @return the default {@link Erode} instance
     */
    public static Erode getDefaultErode() {
	return DEFAULT_ERODE_INSTANCE;
    }

    /**
     * A filtering operation that grows areas with higher values. It will set
     * every grid cell to the value of its highest neighbor or keep its value if
     * already highest.
     * 
     * @author mey
     *
     */
    public static class Dilate extends BasicMorphOp {
	/**
	 * Constructs a new {@link Dilate} object with
	 * {@link EdgeHandler#getDefault()}.
	 */
	private Dilate() {
	    super();
	}

	/**
	 * Constructs a new {@link Dilate} object. For handling grid edges the
	 * given {@link EdgeHandler} is used.
	 * 
	 * @param edgeHandler
	 *            the edge handler to be used
	 */
	public Dilate(EdgeHandler edgeHandler) {
	    super(edgeHandler);
	}

	@Override
	double updateResult(double result, double value) {
	    return Math.max(value, result);
	}
    }

    /**
     * A filtering operation that shrinks areas with higher values. It will set
     * every grid cell to the value of its lowest neighbor or keep its value if
     * already lowest.
     * 
     * @author mey
     *
     */
    public static class Erode extends BasicMorphOp {
	/**
	 * Constructs a new {@link Erode} object with
	 * {@link EdgeHandler#getDefault()}.
	 */
	private Erode() {
	    super();
	}

	/**
	 * Constructs a new {@link Erode} object. For handling grid edges the
	 * given {@link EdgeHandler} is used.
	 * 
	 * @param edgeHandler
	 *            the edge handler to be used
	 */
	public Erode(EdgeHandler edgeHandler) {
	    super(edgeHandler);
	}

	@Override
	double updateResult(double result, double value) {
	    return Math.min(value, result);
	}

    }
}
