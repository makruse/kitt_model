package de.zmt.pathfinding.filter;

import java.io.Serializable;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.DoubleGrid2D;

/**
 * A convolution from source to destination working with double grids.
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
public class ConvolveOp extends AbstractGridFilteringOp implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Kernel kernel;

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
    }

    /**
     * Constructs a new {@link ConvolveOp}. For handling grid edges the given
     * {@link EdgeHandler} is used.
     * 
     * @param kernel
     *            the kernel used for the convolve operation
     * @param edgeHandler
     *            the edge handler to be used
     */
    public ConvolveOp(Kernel kernel, EdgeHandler edgeHandler) {
	super(edgeHandler);
	this.kernel = kernel;
    }

    public Kernel getKernel() {
	return kernel;
    }

    @Override
    public double filter(int x, int y, DoubleGrid2D src) {
	double result = 0;
	for (int i = 0; i < kernel.getWidth(); i++) {
	    for (int j = 0; j < kernel.getHeight(); j++) {
		double weight = kernel.getWeight(i, j);
		int gridX = x + i - kernel.getxOrigin();
		int gridY = y + j - kernel.getyOrigin();

		// handle edges according to edge handler
		double value = getEdgeHandler().getValue(src, gridX, gridY);
		result += value * weight;
	    }
	}
	return result;
    }

    @Override
    public int getxExtend() {
	return kernel.getxOrigin();
    }

    @Override
    public int getyExtend() {
	return kernel.getyOrigin();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[kernel=" + kernel + "]";
    }
}