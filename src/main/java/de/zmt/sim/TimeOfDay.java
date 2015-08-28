package de.zmt.sim;

/**
 * The day is split in different periods of time, each with their start and end
 * times.
 * 
 * @author cmeyer
 *
 */
public enum TimeOfDay {
    LATE_NIGHT(0, 5), SUNRISE(5, 8), DAY(8, 17), SUNSET(17, 20), NIGHT(20, 24);

    /** Start time in hours */
    private final int startTime;
    /** End time in hours */
    private final int endTime;

    TimeOfDay(int startTime, int endTime) {
	this.startTime = startTime;
	this.endTime = endTime;
    }

    public boolean isForageTime() {
	return this == SUNRISE || this == DAY;
    }

    public boolean isRestTime() {
	return !isForageTime();
    }

    public boolean isDay() {
	return !isNight();
    }

    public boolean isNight() {
	return this == NIGHT || this == LATE_NIGHT;
    }

    protected int getStartTime() {
	return startTime;
    }

    protected int getEndTime() {
	return endTime;
    }

    public static TimeOfDay timeFor(long dayHour) {
	for (TimeOfDay dc : TimeOfDay.values()) {
	    if ((dayHour >= dc.startTime && dayHour <= dc.endTime)) {
		return dc;
	    }
	}
	throw new IllegalArgumentException("Parameter 'dayHour' needed in range of 0 to 24.");
    }
}