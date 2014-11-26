package de.zmt.kitt.sim;

public enum MoveMode {
    
	// move mode determines steplength and turning angle and speed and thus activity costs and new positions
	// move mode hat ID, name, turning angle, steplength, speed)
	FORAGING ( 0, "Feeding", 0.1, 0.5),
    SEARCHFOODPATCH ( 1,"SearchFoodPatch", 1.0 , 1.0), 
    RESTING( 2, "resting", 0.1, 0.4),
    DIRECTEDMIGRATION( 3,"directed to attraction", 1.0, 0.1);
    // 1. change habitat
    // 2. avoid predation
    // 3. move to feeding resting place

	String name;
	double stepLengthFactor;
	double turningFactor;

	MoveMode(int id,String name, double stepLengthFactor, double turningFactor) {
        this.name=name;
    }
    //public double standardStepSize() { return standardStepSize; }
 
//    public void print(){        
//        for (MovementModus mm : MovementModus.values())
//           System.out.printf("movement mode %s is %d%d%n",
//                             t, t.beginTime(), t.endTime());
//    } 
};


// net swimming costs = active metabolic rate - routine metabolic rate = bU^c
// Net costs (mg02/h)=1.193*U(cm/s)^1.66 nach Korsmeyer et al. 2002 für S. schlegeli
// nach korsmeyer et al. 2002: effect of mass not significant bei net cost berechnung
// oxicaloric value of 1mg O2=14.2 J (Ohlberger et al. 2006)
//=> net costs (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142
// time resolution => net costs of swimming per time step! U muss bestimmt werden, wo?
