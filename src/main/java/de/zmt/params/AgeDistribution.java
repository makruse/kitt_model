package de.zmt.params;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.util.distribution.PiecewiseLinear;

/**
 * Age distribution for creating a population based on phase probabilities and
 * lengths. The resulting intervals are simply interpolated resulting in a
 * piecewise linear distribution.
 * 
 * @see Phase#getProbability()
 * @see PiecewiseLinear
 * @author mey
 *
 */
public class AgeDistribution {
    private static final Unit<Duration> UNIT = UnitConstants.AGE;

    private final PiecewiseLinear distribution;

    AgeDistribution(Amount<Duration> min, Amount<Duration> max, Amount<Duration> initialPhaseAge,
            Amount<Duration> terminalPhaseAge, MersenneTwisterFast random) {
        super();

        // create shift / scale from min and max values
        double shift = min.doubleValue(UNIT);
        double scale = max.doubleValue(UNIT) - shift;

        // scale intervals accordingly
        double juvenileInterval = (initialPhaseAge.doubleValue(UNIT) - shift) / scale;
        double initialInterval = (terminalPhaseAge.doubleValue(UNIT) - shift) / scale - juvenileInterval;
        double terminalInterval = 1 - initialInterval - juvenileInterval;

        distribution = new PiecewiseLinear(scale, shift, random);
        distribution.addInterval(Phase.JUVENILE.getProbability(), juvenileInterval);
        distribution.addInterval(Phase.INITIAL.getProbability(), initialInterval);
        distribution.addInterval(Phase.TERMINAL.getProbability(), terminalInterval);
    }

    /**
     * @return the next value of the distribution rounded to be in exact seconds
     */
    public Amount<Duration> next() {
        // nextInt() might be insufficient for old agents
        return Amount.valueOf(Math.round(distribution.nextDouble()), UNIT);
    }

    /**
     * Evaluate for given value. For testing.
     * 
     * @param x
     * @return age for given value
     */
    Amount<Duration> evaluateCdf(double x) {
        return Amount.valueOf(Math.round(distribution.evaluateCdf(x)), UNIT);
    }
}
