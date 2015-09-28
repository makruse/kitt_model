package de.zmt.pathfinding.filter;

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

    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dst) {
	int width = src.getWidth();
	int height = src.getHeight();

	if (dst == null) {
	    dst = new DoubleGrid2D(src);
	}

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		dst.set(x, y, filter(x, y, src));
	    }
	}

	return dst;
    }

    public DoubleGrid2D filter(DoubleGrid2D src, DoubleGrid2D dst, boolean[][] include) {
	// TODO only include those patches that have include = true
	return null;
    }

    private double filter(int x, int y, DoubleGrid2D field) {
	double result = 0;
	for (int i = 0; i < kernel.getWidth(); i++) {
	    for (int j = 0; j < kernel.getHeight(); j++) {
		double weight = kernel.getWeight(i, j);
		int fieldX = x + i - kernel.getxOrigin();
		int fieldY = y + j - kernel.getyOrigin();

		// extend the field on borders
		double value = field.get(clamp(fieldX, 0, field.getWidth() - 1),
			clamp(fieldY, 0, field.getHeight() - 1));
		result += value * weight;
	    }
	}
	return result;
    }

    private static int clamp(int val, int min, int max) {
	return Math.max(min, Math.min(max, val));
    }

    public Kernel getKernel() {
	return kernel;
    }
}