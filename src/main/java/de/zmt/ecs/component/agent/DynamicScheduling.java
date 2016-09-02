package de.zmt.ecs.component.agent;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import sim.engine.Schedule;
import sim.util.AmountValuable;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * {@link Component} that allows setting the next time the possessing entity
 * will be updated.
 * 
 * @author mey
 *
 */
public class DynamicScheduling implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** The time the entity should be scheduled for. */
    private double nextTime = 0;
    /** The duration that was skipped until {@link #nextTime}. */
    private Amount<Duration> deltaTime;

    /**
     * Constructs a new {@link DynamicScheduling} component.
     * 
     * @param stepDuration
     *            the step duration
     */
    public DynamicScheduling(Amount<Duration> stepDuration) {
        this.deltaTime = stepDuration;
    }

    /**
     * Configures when the next update should happen.
     * 
     * @param currentTime
     *            the current time from {@link Schedule}
     * @param stepsToPass
     *            the number of steps from now on until the next update should
     *            happen
     * @param stepDuration
     *            the duration of one step
     */
    public void setSkip(double currentTime, long stepsToPass, Amount<Duration> stepDuration) {
        if (stepsToPass < 1) {
            throw new IllegalArgumentException("stepsToPass must be greater than zero, but was: " + stepsToPass);
        }

        this.nextTime = currentTime + stepsToPass;
        this.deltaTime = stepDuration.times(stepsToPass);
    }

    /**
     * Returns the time the entity should be scheduled for.
     * 
     * @return the time the entity should be scheduled for
     */
    public double getNextTime() {
        return nextTime;
    }

    /**
     * Returns the duration that was skipped until {@link #getNextTime()}.
     * 
     * @return the duration that was skipped until {@link #getNextTime()}.
     */
    public Amount<Duration> getDeltaTime() {
        return deltaTime;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[nextTime=" + nextTime + ", deltaTime=" + deltaTime + "]";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
        public double getNextTime() {
            return nextTime;
        }

        public Valuable getDeltaTime() {
            return AmountValuable.wrap(deltaTime);
        }

        @Override
        public String toString() {
            return DynamicScheduling.this.getClass().getSimpleName();
        }
    }
}
