package de.zmt.kitt.ecs.component.environment;

import static javax.measure.unit.SI.*;

import org.joda.time.*;

import sim.util.Proxiable;
import de.zmt.kitt.sim.TimeOfDay;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import ecs.Component;

public class SimulationTime implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Converted {@link EnvironmentDefinition#STEP_DURATION} to yoda format */
    private static final Duration STEP_DURATION_YODA = new Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND))
		    .getExactValue());

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
	return dateTime.getMillisOfDay() < STEP_DURATION_YODA
		.getMillis();
    }

    /**
     * 
     * @return true if current step is the first of the week, i.e. the first of
     *         Monday.
     */
    public boolean isFirstStepInWeek() {
	return dateTime.getDayOfWeek() == DateTimeConstants.MONDAY
		&& isFirstStepInDay();
    }

    public TimeOfDay getTimeOfDay() {
	return TimeOfDay.timeFor(dateTime.getHourOfDay());
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Period getTime() {
	    return new Period(EnvironmentDefinition.START_INSTANT, dateTime);
	}

	public TimeOfDay getTimeOfDay() {
	    return SimulationTime.this.getTimeOfDay();
	}
    }
}
