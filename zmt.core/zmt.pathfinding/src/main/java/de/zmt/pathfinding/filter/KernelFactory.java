package de.zmt.pathfinding.filter;

/**
 * Factory class for creating common {@link Kernel} types.
 * 
 * @author mey
 *
 */
public final class KernelFactory {
    private KernelFactory() {

    }

    private static final Kernel NEUTRAL_INSTANCE = createConstant(1, 1);
    /** Weight added to the origin to prevent trapping. */
    private static final int NOTRAP_ORIGIN_ADDEND_EXTEND = 2;
    private static final double GAUSSIAN_BLUR_STD_DEV_FACTOR = 1d / 4;

    /**
     * Returns the neutral kernel. If used in itself in a convolution it will
     * not change the grid. Useful for scaling operations with
     * {@link Kernel#multiply(double)}.
     * 
     * @return the neutral kernel
     */
    public static Kernel getNeutral() {
        return NEUTRAL_INSTANCE;
    }

    /**
     * Returns a constant kernel in given dimensions, with all weights set to
     * '1'.
     * 
     * @param width
     *            the width of the kernel
     * @param height
     *            the height of the kernel
     * @return the constant kernel in given dimensions
     */
    public static Kernel createConstant(int width, int height) {
        int size = width * height;
        double[] weights = new double[size];

        for (int i = 0; i < size; i++) {
            weights[i] = 1;
        }

        return new Kernel(width, height, weights);
    }

    /**
     * Returns a kernel for a box blur filter that emphasizes the origin value
     * more than those surrounding, to prevent an agent from being trapped. The
     * most extreme example is an empty location surrounded by full ones:
     * 
     * <pre>
     * 0 0 0 0 0
     * 0 1 1 1 0
     * 0 1 0 1 0
     * 0 1 1 1 0
     * 0 0 0 0 0
     * </pre>
     * 
     * where '1' is the maximum and '0' the minimum value. A 3x3 box blur with
     * equal weights would associate a higher value to the center than to its
     * adjacents, although its value is zero. A no trap kernel needs the origin
     * to be emphasized in a way to make the empty location in the center less
     * valuable than those surrounding.
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
     * adjacent will return a higher value than the empty center. Straight
     * adjacents will always return higher values than other. The difference in
     * sum of towards them is:
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
     * Now we have the origin weight which leads to equality between empty
     * location and adjacents. So we increment it by '1' which leads to the end
     * result of:
     * 
     * <pre>
     * w_0 = width + 2
     * w_0 = height + 2
     * </pre>
     * 
     * and the agent will not get stuck on the empty location. If the kernel is
     * not square the lower result for {@code w_0} is sufficient.
     * 
     * @param width
     *            the width of the kernel
     * @param height
     *            the height of the kernel
     * @return a no-trap kernel in given dimensions
     *
     */
    public static Kernel createNoTrapBlur(int width, int height) {
        if (width < 3 || height < 3) {
            throw new IllegalArgumentException("Extents cannot be lower than 3.");
        }

        int size = width * height;

        double originWeight = (width < height ? width : height) + NOTRAP_ORIGIN_ADDEND_EXTEND;
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

        return new Kernel(width, height, weights);
    }

    /**
     * Returns a normalized blur kernel based on a Gaussian function. The radius
     * is measured around a single cell and results in:
     * 
     * <pre>
     * extent = ceil(radius) * 2 + 1
     * </pre>
     * 
     * So the resulting kernels are always square and have an uneven extent. The
     * standard deviation is set approximately to make the kernel's values be
     * near zero at the edges.
     * 
     * @see #createGaussian(int, double)
     * @see Math#ceil(double)
     * @param radius
     *            the radius for the Gaussian blur
     * @return a Gaussian blur kernel with given radius
     */
    public static Kernel createGaussianBlur(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("The radius must be positive.");
        }
        if (radius == 0) {
            return getNeutral();
        }

        int extent = (int) Math.ceil(radius) * 2 + 1;
        double stdDev = radius * GAUSSIAN_BLUR_STD_DEV_FACTOR;
        return createGaussian(extent, stdDev).normalize();
    }

    /**
     * Creates a {@link Kernel} based on a Gaussian function.
     * 
     * <pre>
     * G(x, y) = (1 / (2 * PI * stdDev ^ 2)) * exp(-(x ^ 2 + y ^ 2) / (2 * stdDev ^ 2))
     * </pre>
     * 
     * @see <a href=
     *      "https://en.wikipedia.org/w/index.php?title=Gaussian_blur&oldid=691028704">
     *      Wikipedia: Gaussian blur</a>
     * @param extent
     *            the width and height of the kernel.
     * @param stdDev
     *            the standard deviation for the Gaussian function
     * @return the Gaussian kernel from given parameters
     */
    public static Kernel createGaussian(int extent, double stdDev) {
        double[] weights = new double[extent * extent];
        int origin = (extent - 1) / 2;

        double stdDevSq = stdDev * stdDev;
        double firstPart = 1 / (2 * Math.PI * stdDevSq);
        for (int j = 0; j < extent; j++) {
            for (int i = 0; i < extent; i++) {
                int x = i - origin;
                int y = j - origin;
                weights[j * extent + i] = firstPart * Math.exp(-(x * x + y * y) / (2 * stdDevSq));
            }
        }

        return new Kernel(extent, extent, weights);
    }
}
