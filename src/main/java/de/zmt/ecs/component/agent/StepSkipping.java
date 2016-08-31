package de.zmt.ecs.component.agent;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import sim.util.AmountValuable;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * {@link Component} that allows setting the next step that the possessing
 * entity will be updated.
 * 
 * @author mey
 *
 */
public class StepSkipping implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** The next step that is not to be skipped. */
    private long nextStep = 0;
    /** The duration that was skipped until {@link #nextStep}. */
    private Amount<Duration> deltaTime;

    /**
     * Constructs a new {@link StepSkipping} component.
     * 
     * @param stepDuration
     *            the step duration
     */
    public StepSkipping(Amount<Duration> stepDuration) {
        this.deltaTime = stepDuration;
    }

    /**
     * Sets next step and delta time.
     * 
     * @param currentSteps
     *            the current step number
     * @param stepsToSkip
     *            the number of steps to skip
     * @param stepDuration
     *            the duration of one step
     */
    public void setSkip(long currentSteps, long stepsToSkip, Amount<Duration> stepDuration) {
        this.nextStep = currentSteps + stepsToSkip;
        if (stepsToSkip > 1) {
            this.deltaTime = stepDuration.times(stepsToSkip);
        }
        // step duration is minimum delta
        else {
            this.deltaTime = stepDuration;
        }
    }

    /**
     * Returns the next step that is not to be skipped.
     * 
     * @return the next step that is not to be skipped
     */
    public long getNextStep() {
        return nextStep;
    }

    /**
     * Returns the duration that was skipped until {@link #getNextStep()}.
     * 
     * @return the duration that was skipped until {@link #getNextStep()}.
     */
    public Amount<Duration> getDeltaTime() {
        return deltaTime;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[nextStep=" + nextStep + ", deltaTime=" + deltaTime + "]";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
        public long getNextStep() {
            return nextStep;
        }

        public Valuable getDeltaTime() {
            return AmountValuable.wrap(deltaTime);
        }

        @Override
        public String toString() {
            return StepSkipping.this.getClass().getSimpleName();
        }
    }
}
