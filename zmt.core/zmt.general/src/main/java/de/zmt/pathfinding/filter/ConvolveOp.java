package de.zmt.pathfinding.filter;

import de.zmt.sim.field.grid.BooleanGrid;
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
public class ConvolveOp {
    protected final Kernel kernel;

    public ConvolveOp(Kernel kernel) {
	super();
	this.kernel = kernel;
    }

    /**
     * Performs a convolution on a DoubleGrid2D. Each cell of the source grid
     * will be convolved.
     * 
     * @param src
     *            the source grid
     * @param dest
     *            the destination for filtered {@code src}. If null a new
     *            DoubleGrid2D will be created with the same dimensions as
     *            {@code src}.
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dest) {
	return filter(src, dest, null);
    }

    /**
     * Performs a convolution only on the cells that are marked with a flag.
     * 
     * @param src
     *            the source grid
     * @param dest
     *            the destination for filtered {@code src}. If null a new
     *            DoubleGrid2D will be created with the same dimensions as
     *            {@code src}.
     * @param include
     *            array containing a flag for every cell if it is to be included
     *            in the convolution
     * @return the resulting grid {@code dest}
     */
    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dest, BooleanGrid include) {
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
		double value = getAndExtend(src, fieldX, fieldY);
		result += value * weight;
	    }
	}
	return result;
    }

    protected final double getAndExtend(DoubleGrid2D grid, int fieldX, int fieldY) {
	return grid.get(clamp(fieldX, 0, grid.getWidth() - 1), clamp(fieldY, 0, grid.getHeight() - 1));
    }

    private static int clamp(int val, int min, int max) {
	return Math.max(min, Math.min(max, val));
    }

    public Kernel getKernel() {
	return kernel;
    }
}