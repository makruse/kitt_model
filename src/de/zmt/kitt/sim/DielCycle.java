package de.zmt.kitt.sim;

public enum DielCycle {
    LATE_NIGHT(0, 5), SUNRISE(5, 8), DAY(8, 17), SUNSET(17, 20), NIGHT(20, 24);

    /** Start time in hours */
    private final int startTime;
    /** End time in hours */
    private final int endTime;

    DielCycle(int startTime, int endTime) {
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

    public static DielCycle getDielCycle(long dayHour) {
	for (DielCycle dc : DielCycle.values()) {
	    if ((dayHour >= dc.startTime && dayHour <= dc.endTime))
		return dc;
	}
	throw new IllegalArgumentException(
		"Parameter 'dayHour' needed in range of 0 to 24.");
    }
}