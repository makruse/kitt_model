package de.zmt.kitt.sim.engine.agent;

/**
 * move mode determines steplength and turning angle and speed and thus activity
 * costs and new positions move mode hat ID, name, turning angle, steplength,
 * speed)
 */
enum MoveMode {

    FORAGING("Feeding", 0.1, 0.5),
    /** change habitat */
    SEARCHFOODPATCH("SearchFoodPatch", 1.0, 1.0),
    /** avoid predation */
    RESTING("resting", 0.1, 0.4),
    /** move to feed or rest place */
    DIRECTEDMIGRATION("directed to attraction", 1.0, 0.1);

    String name;
    double stepLengthFactor;
    double turningFactor;

    MoveMode(String name, double stepLengthFactor, double turningFactor) {
	this.name = name;
    }

    @Override
    public String toString() {
	return name;
    }
};

/*
 * net swimming costs = active metabolic rate - routine metabolic rate = bU^c
 * Net costs (mg02/h)=1.193*U(cm/s)^1.66 nach Korsmeyer et al. 2002 f�r S.
 * schlegeli nach korsmeyer et al. 2002: effect of mass not significant bei net
 * cost berechnung oxicaloric value of 1mg O2=14.2 J (Ohlberger et al. 2006) =>
 * net costs (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142 time resolution => net costs
 * of swimming per time step! U muss bestimmt werden, wo?
 */
