package de.zmt.pathfinding.filter;

/**
 * A kernel with all weights set to '1'.
 * 
 * @author mey
 *
 */
public class ConstantKernel extends Kernel {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@link ConstantKernel} with given dimensions.
     * 
     * @param width
     * @param height
     */
    public ConstantKernel(int width, int height) {
	super(width, height, createWeights(width, height));
    }

    /**
     * 
     * @param width
     * @param height
     * @return weights array with the specified dimensions
     */
    private static double[] createWeights(int width, int height) {
	int size = width * height;
	double[] weights = new double[size];

	for (int i = 0; i < size; i++) {
	    weights[i] = 1;
	}

	return weights;
    }
}
