package sim.util.distribution;

import java.util.ArrayList;
import java.util.Collection;

import ec.util.MersenneTwisterFast;

/**
 * Piecewise linear distribution mapping source intervals to target intervals.
 * The intervals entries are evaluated in order as they were added, each one
 * added to the sum of the previous ones. Shift and scale are applied last with
 * interval values independent from them.
 * <p>
 * The cumulative distribution function (cdf) maps the accumulated range of
 * source intervals to the accumulated range of target intervals times scale
 * plus shift.
 * 
 * @author mey
 *
 */
public class PiecewiseLinear extends AbstractContinousDistribution {
    private static final long serialVersionUID = 1L;

    private final Collection<IntervalEntry> intervalEntries = new ArrayList<>();
    private double scale;
    private double shift;

    private double sourceRange = 0;

    /**
     * Constructs a new {@link PiecewiseLinear} distribution.
     * 
     * @param scale
     *            the scale of returned values
     * @param shift
     *            the shift of returned values
     * @param random
     *            the random number generator to be used
     */
    public PiecewiseLinear(double scale, double shift, MersenneTwisterFast random) {
	super();
	this.scale = scale;
	this.shift = shift;
	setRandomGenerator(random);
    }

    /**
     * Constructs a new {@link PiecewiseLinear} distribution with neutral shift
     * (1) and scale (0).
     * 
     * @param random
     *            the random number generator to be used
     */
    public PiecewiseLinear(MersenneTwisterFast random) {
	this(1, 0, random);
    }

    /**
     * Adds an interval spanning from the maximum value of the previous interval
     * to the given, mapping from source to target. Interval values are
     * independent from shift and scale.
     * 
     * @param sourceIntervalMax
     * @param targetIntervalMax
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addInterval(double sourceIntervalMax, double targetIntervalMax) {
	if (intervalEntries.add(new IntervalEntry(sourceIntervalMax, targetIntervalMax / sourceIntervalMax))) {
	    sourceRange += sourceIntervalMax;
	    return true;
	}
	return false;
    }

    /**
     * Evaluates cumulative distribution function for this distribution. If the
     * value exceeds [0,1] the result is either zero for the lower end or the
     * maximum for the upper end.
     * 
     * @param x
     * @return result from function
     */
    public double evaluateCdf(double x) {
	double remaining = x;
	double result = 0;
	for (IntervalEntry intervalEntry : intervalEntries) {
	    // amount of x within current interval
	    double within = Math.min(remaining, intervalEntry.sourceIntervalMax);
	    // scale according to current amount
	    result += within * intervalEntry.targetScale;
	    remaining -= within;

	    if (remaining <= 0) {
		break;
	    }
	}

	return result * scale + shift;
    }

    public double getScale() {
	return scale;
    }

    public void setScale(double scale) {
	this.scale = scale;
    }

    public double getShift() {
	return shift;
    }

    public void setShift(double shift) {
	this.shift = shift;
    }

    @Override
    public double nextDouble() {
	return evaluateCdf(getRandomGenerator().nextDouble() * sourceRange);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[intervalEntries=" + intervalEntries + ", scale=" + scale + ", shift="
		+ shift + "]";
    }

    private static class IntervalEntry {
	private final double sourceIntervalMax;
	private final double targetScale;

	public IntervalEntry(double sourceIntervalMax, double targetScale) {
	    super();
	    this.sourceIntervalMax = sourceIntervalMax;
	    this.targetScale = targetScale;
	}

	@Override
	public String toString() {
	    return sourceIntervalMax + "*=" + targetScale;
	}

    }
}