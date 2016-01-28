package de.zmt.pathfinding.filter;

/**
 * Immutable Kernel for operation on double grids. A similar class already
 * exists but is limited to image operations.
 * 
 * @see java.awt.image.Kernel
 * @see ConvolveOp
 * @author mey
 *
 */
public class Kernel {
    private final int width;
    private final int height;
    private final int yOrigin;
    private final int xOrigin;
    private final double[] weights;

    /**
     * Constructs a <code>Kernel</code> object from an array of doubles. If the
     * length of the <code>weights</code> array is less than width*height, an
     * <code>IllegalArgumentException</code> is thrown. The X origin is
     * (width-1)/2 and the Y origin is (height-1)/2.
     * 
     * @param width
     *            width of the kernel
     * @param height
     *            height of the kernel
     * @param weights
     *            weights data in row major order
     * @throws IllegalArgumentException
     *             if the length of <code>weights</code> is less than the
     *             product of <code>width</code> and <code>height</code>
     */
    public Kernel(int width, int height, double[] weights) {
	super();
	this.width = width;
	this.height = height;
	this.xOrigin = (width - 1) >> 1;
	this.yOrigin = (height - 1) >> 1;
	this.weights = weights;

	int len = width * height;
	if (weights.length < len) {
	    throw new IllegalArgumentException(
		    "Data array too small " + "(is " + weights.length + " and should be " + len);
	}
    }

    /**
     * Multiplies all weights by {@code scalar} and return result with a new
     * object.
     * 
     * @param scalar
     * @return new kernel object with applied {@code scalar}
     */
    public Kernel multiply(double scalar) {
	double[] scaledWeights = new double[weights.length];
	for (int i = 0; i < weights.length; i++) {
	    scaledWeights[i] = weights[i] * scalar;
	}

	return new Kernel(width, height, scaledWeights);
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public int getyOrigin() {
	return yOrigin;
    }

    public int getxOrigin() {
	return xOrigin;
    }

    public double getWeight(int x, int y) {
	return weights[y * width + x];
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("weights:\n");
	for (int y = 0; y < getHeight(); y++) {
	    for (int x = 0; x < getWidth(); x++) {
		builder.append(String.format("%3.1f ", getWeight(x, y)));
	    }
	    builder.append("\n");
	}
	return builder.toString();
    }

}