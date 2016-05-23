package de.zmt.ecs.component.environment;

import static javax.measure.unit.SI.*;

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.joda.time.ReadableDuration;

import de.zmt.ecs.Component;
import de.zmt.params.def.EnvironmentDefinition;
import de.zmt.util.TimeOfDay;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * Component for storing simulation time.
 * 
 * @author mey
 *
 */
public class SimulationTime implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /**
     * {@link EnvironmentDefinition#STEP_DURATION} in yoda's {@link Duration}
     * format.
     */
    private static final Duration STEP_DURATION_YODA = new Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND)).getExactValue());

    /** {@link MutableDateTime} for storing simulation time */
    private final MutableDateTime dateTime;

    public SimulationTime(Instant startInstant) {
	this.dateTime = new MutableDateTime(startInstant);
    }

    public void addTime(ReadableDuration duration) {
	dateTime.add(duration);
    }

    /**
     * 
     * @return true if current step is the first of the day.
     */
    public boolean isFirstStepInDay() {
	return dateTime.getMillisOfDay() < STEP_DURATION_YODA.getMillis();
    }

    /**
     * 
     * @return true if current step is the first of the week, i.e. the first of
     *         Monday.
     */
    public boolean isFirstStepInWeek() {
	return dateTime.getDayOfWeek() == DateTimeConstants.MONDAY && isFirstStepInDay();
    }

    public TimeOfDay getTimeOfDay() {
	return TimeOfDay.timeFor(dateTime.getHourOfDay());
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {

	public Valuable getTime() {
	    final Period period = new Period(EnvironmentDefinition.START_INSTANT, dateTime);
	    return new Valuable() {

		@Override
		public double doubleValue() {
		    return period.toStandardSeconds().getSeconds();
		}

		@Override
		public String toString() {
		    return period.toString();
		}
	    };
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
