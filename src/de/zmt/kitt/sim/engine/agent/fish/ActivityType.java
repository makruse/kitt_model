package de.zmt.kitt.sim.engine.agent.fish;

public enum ActivityType {
    FORAGING, RESTING;

    private static final double COST_FACTOR_FORAGING = 4.3;
    private static final double COST_FACTOR_RESTING = 1.6;

    /** Fish needs {@code costFactor * SMR} to maintain this activity type. */
    public double getCostFactor() {
	switch (this) {
	case FORAGING:
	    return COST_FACTOR_FORAGING;
	case RESTING:
	    return COST_FACTOR_RESTING;
	default:
	    return 1;
	}
    }
}
