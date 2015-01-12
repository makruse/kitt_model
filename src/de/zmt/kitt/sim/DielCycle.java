package de.zmt.kitt.sim;

public enum DielCycle {
    LATE_NIGHT("night", 0, 5), SUNRISE("sunrise", 5, 8), DAY("daytime", 8, 17), SUNSET(
	    "sunset", 17, 20), NIGHT("night", 20, 24);

    private final int beginTime; // in hours
    private final int endTime; // in hours

    DielCycle(String name, int beginTime, int endTime) {
	this.beginTime = beginTime;
	this.endTime = endTime;
    }

    public int beginTime() {
	return beginTime;
    }

    public int endTime() {
	return endTime;
    }

    public void print() {
	for (DielCycle t : DielCycle.values())
	    System.out.printf("Time of day %s is %d%d%n", t, t.beginTime(),
		    t.endTime());
    }

    public static DielCycle getDielCycle(long dayHour) {
	for (DielCycle dc : DielCycle.values()) {
	    if ((dayHour >= dc.beginTime && dayHour <= dc.endTime))
		return dc;
	}
	throw new IllegalArgumentException(
		"Parameter 'dayHour' needs to be in the range of 0 to 24.");
    }
}