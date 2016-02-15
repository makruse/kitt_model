package de.zmt.pathfinding.filter;

/**
 * A kernel for a box blur filter that emphasizes the origin value more than
 * those surrounding, to prevent an agent from being trapped. The most extreme
 * example is an empty location surrounded by full ones:
 * 
 * <pre>
 * 0 0 0 0 0
 * 0 1 1 1 0
 * 0 1 0 1 0
 * 0 1 1 1 0
 * 0 0 0 0 0
 * </pre>
 * 
 * where '1' is the maximum and '0' the minimum value. A 3x3 box blur with equal
 * weights would associate a higher value to the center than to its adjacents,
 * although its value is zero. A no trap kernel needs the origin to be
 * emphasized in a way to make the empty location in the center less valuable
 * than those surrounding.
 * <p>
 * The sum for the empty center is:
 * 
 * <pre>
 * (width * height - 1) * w * 1 + w_o * 0
 * 	= width * height - 1
 * </pre>
 * 
 * and for the left / right adjacents:
 * 
 * <pre>
 * w_0 + (width + 1) * w * 0 + ((width * height) - (width + 1) - 1) * w * 1
 * 	= w_0 + (width * height) - width - 2
 * </pre>
 * 
 * where {@code w} is set to '1'. The upper / lower adjacents are similar:
 * 
 * <pre>
 * w_0 + (width * height) - height - 2
 * </pre>
 * 
 * The origin weight {@code w_0} needs to be set in a way that at least one
 * adjacent will return a higher value than the empty center. Straight adjacents
 * will always return higher values than other. The difference in sum of towards
 * them is:
 * 
 * <pre>
 * w_0 + width * height - width - 2 = width * height - 1
 * w_0 = width * height - 1 - (width * height - width - 2)
 * w_0 = width * height - 1 - width * height + width + 2
 * w_0 = width + 1
 * </pre>
 * 
 * or for upper / lower adjacents:
 * 
 * <pre>
 * w_0 = height + 1
 * </pre>
 * 
 * Now we have the origin weight which leads to equality between empty location
 * and adjacents. So we increment it by '1' which leads to the end result of:
 * 
 * <pre>
 * w_0 = width + 2
 * w_0 = height + 2
 * </pre>
 * 
 * and the agent will not get stuck on the empty location. If the kernel is not
 * square the lower result for {@code w_0} is sufficient.
 *
 * 
 * @author mey
 *
 */
public class NoTrapBlurKernel extends Kernel {
    private static final long serialVersionUID = 1L;

    private static final int ORIGIN_ADDEND_EXTEND = 2;

    /**
     * Constructs a {@link NoTrapBlurKernel} from given distances.
     * 
     * @param width
     *            the distance from origin to the left and right side
     * @param height
     *            the distance from origin to the top and bottom side
     */
    public NoTrapBlurKernel(int width, int height) {
	super(width, height, createWeights(width, height));
    }

    /**
     * 
     * @param width
     * @param height
     * @return weights array for the specified extents
     */
    private static double[] createWeights(int width, int height) {
	int xDist = toDist(width);
	int yDist = toDist(height);

	if (xDist < 1 || yDist < 1) {
	    throw new IllegalArgumentException("Distance cannot be lower than 1.");
	}

	int size = width * height;
	
	double originWeight = (width < height ? width : height) + ORIGIN_ADDEND_EXTEND;
	double weightSum = (size - 1) + originWeight;
	double defaultWeight = 1 / weightSum;
	originWeight /= weightSum;
	int originIndex = (size - 1) / 2;

	double[] weights = new double[size];
	for (int i = 0; i < size; i++) {
	    // accentuate origin
	    if (i == originIndex) {
		weights[i] = originWeight;
	    }
	    // all else to default value
	    else {
		weights[i] = defaultWeight;
	    }
	}
	return weights;
    }

    /**
     * 
     * @param extent
     * @return distance from extent
     */
    private static int toDist(int extent) {
	if (extent % 2 == 0) {
	    throw new IllegalArgumentException("Given extents must be uneven: " + extent);
	}
	return (extent - 1) / 2;
    }

}
