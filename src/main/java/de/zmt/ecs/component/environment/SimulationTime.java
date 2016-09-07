package de.zmt.ecs.component.environment;

import static javax.measure.unit.SI.SECOND;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.TimeOfDay;
import sim.util.Proxiable;
import sim.util.TemporalValuable;
import sim.util.Valuable;

/**
 * Component for storing simulation time.
 * 
 * @author mey
 *
 */
public class SimulationTime implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** {@link LocalDateTime} for storing current simulation time. */
    private LocalDateTime dateTime;
    /** The {@link LocalDateTime} the simulation has started. */
    private final TemporalAccessor startDateTime;
    /** {@link Duration} of one simulation step. */
    private final Duration stepDuration;

    /**
     * Constructs a new {@link SimulationTime} object.
     * 
     * @param startTemporal
     *            the temporal object the simulation starts at
     * @param stepDuration
     *            the duration of one simulation step.
     * @throws IllegalArgumentException
     *             if duration is smaller than 1 second
     */
    public SimulationTime(TemporalAccessor startTemporal, TemporalAmount stepDuration) {
        if (stepDuration.get(ChronoUnit.SECONDS) < 1) {
            throw new IllegalArgumentException();
        }
        this.startDateTime = startTemporal;
        this.dateTime = LocalDateTime.from(startTemporal);
        this.stepDuration = Duration.from(stepDuration);
    }

    /** Adds the duration of one step to current simulation time. */
    public void addStep() {
        dateTime = dateTime.plus(stepDuration);
    }

    /**
     * Returns <code>true</code> if at first step of day according to given step
     * duration.
     * 
     * @param stepDuration
     *            the step duration
     * @return <code>true</code> if at first step of day
     */
    public boolean isFirstStepInDay(Amount<javax.measure.quantity.Duration> stepDuration) {
        return dateTime.toLocalTime().toSecondOfDay() < stepDuration.longValue(SECOND);
    }

    /**
     * Returns the current {@link TimeOfDay}.
     * 
     * @return the current {@link TimeOfDay}
     */
    public TimeOfDay getTimeOfDay() {
        return TimeOfDay.timeFor(dateTime.getHour());
    }

    @Override
    public MyPropertiesProxy propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
        public String getDateTime() {
            return dateTime.toString();
        }

        public Valuable getElapsedTime() {
            return new TemporalValuable(startDateTime, dateTime);
        }

        public String getTimeOfDay() {
            return SimulationTime.this.getTimeOfDay().toString();
        }

        @Override
        public String toString() {
            return SimulationTime.this.getClass().getSimpleName();
        }
    }
}
