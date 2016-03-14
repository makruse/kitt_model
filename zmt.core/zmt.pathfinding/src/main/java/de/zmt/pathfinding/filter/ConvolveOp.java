package de.zmt.pathfinding.filter;

import java.io.Serializable;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.BooleanGrid;
import sim.field.grid.DoubleGrid2D;

/**
 * A convolution from source to destination working with double grids. Values at
 * the edges from the grid are extended if the kernel matrix is beyond borders.
 * <p>
 * Unfortunately java's own ConvolveOp is only suitable for images and does not
 * support floating point data, even when packed into a custom image. Apart from
 * that, the purpose of this class is similar.
 * 
 * @see java.awt.image.ConvolveOp
 * @see <a href="http://docs.gimp.org/en/plug-in-convmatrix.html">Convolution
 *      Matrix (Explanation in GIMP manual)</a>
 * @author mey
 *
 */
public class ConvolveOp implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Kernel kernel;
    private final EdgeHandler edgeHandler;
    
    /**
     * Constructs a new {@link ConvolveOp} with {@link EdgeHandler#getDefault()}
     * .
     * 
     * @param kernel
     *            the kernel to be used
     */
    public ConvolveOp(Kernel kernel) {
	super();
	this.kernel = kernel;
	this.edgeHandler = EdgeHandler.getDefault();
    }

    /**
     * Constructs a new {@link ConvolveOp}. For handling grid edges the given
     * {@link EdgeHandler} is used.
     * 
     * @param kernel
     *            the kernel used for the convolve operation
     * @param edgeHandler
     *            the edge hanlder to be used
     */
    public ConvolveOp(Kernel kernel, EdgeHandler edgeHandler) {
	super();
	this.kernel = kernel;
	this.edgeHandler = edgeHandler;
    }

    /**
     * Performs a convolution on a DoubleGrid2D. Each cell of the source grid
     * will be convolved. The results are written into a copy of {@code src}.
     * 
     * @param src
     *            the source grid
     * @return the resulting grid
     */
    public DoubleGrid2D filter(DoubleGrid2D src) {
	return filter(src, null, null);
    }

    /**
     * Performs a convolution on a DoubleGrid2D. Each cell of the source grid
     * will be convolved. If the destination grid is <code>null</code> a copy of
     * {@code src} will be created and used as destination.
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
     * Performs a convolution only on the cells that are marked with a flag. If
     * the destination grid is <code>null</code> a copy of {@code src} will be
     * created and used as destination.
     * 
     * @param src
     *            the source grid
     * @param dest
     *            the destination for filtered {@code src} or <code>null</code>
     * @param include
     *            array containing a flag for every cell if it is to be included
     *            in the convolution
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dest, BooleanGrid include) {
	if (src == dest) {
	    throw new IllegalArgumentException("Source and destination grids must not be different objects.");
	}

	int width = src.getWidth();
	int height = src.getHeight();

	if (dest == null) {
	    dest = new DoubleGrid2D(src);
	}

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		if (include == null || include.get(x, y)) {
		    dest.set(x, y, filter(x, y, src));
		}
		// not included, copy from source
		else {
		    dest.set(x, y, src.get(x, y));
		}
	    }
	}

	return dest;
    }

    /**
     * Convolves a single cell on the grid.
     * 
     * @param x
     * @param y
     * @param src
     * @return the convoluted value for that cell
     */
    public double filter(int x, int y, DoubleGrid2D src) {
	double result = 0;
	for (int i = 0; i < kernel.getWidth(); i++) {
	    for (int j = 0; j < kernel.getHeight(); j++) {
		double weight = kernel.getWeight(i, j);
		int fieldX = x + i - kernel.getxOrigin();
		int fieldY = y + j - kernel.getyOrigin();

		// extend the field on borders
		double value = edgeHandler.getValue(src, fieldX, fieldY);
		result += value * weight;
	    }
	}
	return result;
    }

    public Kernel getKernel() {
	return kernel;
    }

    public EdgeHandler getEdgeHandler() {
	return edgeHandler;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[kernel=" + kernel + "]";
    }
}