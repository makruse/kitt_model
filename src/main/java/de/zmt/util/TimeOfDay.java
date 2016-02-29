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

    private static final TimeOfDay[] VALUES = TimeOfDay.values();

    /** Start time in hours */
    private final int startTime;

    TimeOfDay(int startTime) {
	this.startTime = startTime;
    }

    /** @return the {@link TimeOfDay} following this one */
    public TimeOfDay getNext() {
	int nextOrdinal = this.ordinal() + 1;
	if (nextOrdinal >= VALUES.length) {
	    return VALUES[0];
	}
	return VALUES[nextOrdinal];
    }

    public static TimeOfDay timeFor(int hourOfDay) {
	if (hourOfDay < 0 || hourOfDay > 24) {
	    throw new IllegalArgumentException(hourOfDay + " must be between 0 and 24.");
	}

	TimeOfDay previous = NIGHT;

	for (TimeOfDay value : VALUES) {
	    if (hourOfDay < value.startTime) {
		return previous;
	    }
	    previous = value;
	}
	return previous;
    }
}