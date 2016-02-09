package de.zmt.util;

/**
 * The day is split in different periods of time, ranging from its start time to
 * the next period.
 * 
 * @author mey
 *
 */
public enum TimeOfDay {
    SUNRISE(6), DAY(7), SUNSET(18), NIGHT(19);

    /** Start time in hours */
    private final int startTime;

    TimeOfDay(int startTime) {
	this.startTime = startTime;
    }

    public static TimeOfDay timeFor(int hourOfDay) {
	if (hourOfDay < 0 || hourOfDay > 24) {
	    throw new IllegalArgumentException(hourOfDay + " must not be negative.");
	}

	TimeOfDay previous = NIGHT;

	for (TimeOfDay value : TimeOfDay.values()) {
	    if (hourOfDay < value.startTime) {
		return previous;
	    }
	    previous = value;
	}
	return previous;
    }
}