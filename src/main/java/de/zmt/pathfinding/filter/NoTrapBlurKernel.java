package de.zmt.pathfinding.filter;

import de.zmt.pathfinding.filter.Kernel;

/**
 * 
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
 *  8 * w * 1 + 1 * w_o * 0 = 8w
 * </pre>
 * 
 * for the diagonal adjacents:
 * 
 * <pre>
 * 2 * w * 1 + 6 * w * 0 + 1 * w_0 * 1 = 2w + w_0
 * </pre>
 * 
 * and for the straight adjacents:
 * 
 * <pre>
 * 4 * w * 1 + 4 * w * 0 + w_0 * 1 = 4w + w_0
 * </pre>
 * 
 * where {@code w} is set to '1'. The origin weight {@code w_0} needs to be set
 * in a way that at least one adjacent will return a higher value than the empty
 * center. The difference in sum of towards the more valuable straight adjacent
 * is:
 * 
 * <pre>
 * 8w = 4w + w_0
 * w_0 = 8w - 4w
 * w_0 = 4w = 4
 * </pre>
 * 
 * Now we have the origin weight which leads to equality between empty location
 * and adjacents. So we increment it by '1' which leads to the end result of '5'
 * and the agent will not get stuck on the empty location.
 * <p>
 * This result could also be used for bigger kernel sizes because the difference
 * between empty location and adjacents can only be smaller then.
 *
 * 
 * @author mey
 *
 */
public class NoTrapBlurKernel extends Kernel {
    private static final double ORIGIN_FACTOR = 5;
    private static final int SIZE = 3;
    private static final double DIVISOR = SIZE * SIZE - 1 + ORIGIN_FACTOR;
    private static final double DEFAULT_WEIGHT = 1 / DIVISOR;
    private static final double ORIGIN_WEIGHT = ORIGIN_FACTOR / DIVISOR;
    private static final double[] WEIGHTS = new double[] { DEFAULT_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT,
	    DEFAULT_WEIGHT, ORIGIN_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT };

    public NoTrapBlurKernel() {
	super(SIZE, SIZE, WEIGHTS);
    }
}