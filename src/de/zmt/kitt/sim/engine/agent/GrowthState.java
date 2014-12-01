package de.zmt.kitt.sim.engine.agent;

public enum GrowthState {

    // 50% maturity values (change of lifeStage only based size (SL in cm) not
    // age):
    // juv=14.8 TL, females=20.7 (McIlwain 2009) => juv=12.34, fem=17.38,
    // Lasym aus vBGF=39.1 => 40 als max wert f�r male genommen
    // TL to SL (based on formula f�r Serranidae: SL=TL/1.1709-0.2975, aus:
    // Gaygusuz et al. 2006)
    
    /** fish is juvenile (0-12cm) */
    JUVENILE("Juvenile", 0, 12),
    /** fish is female (12-17cm) */
    ADULT_FEMALE("Female", 12, 17),
    /** fish is male(more than 17cm) */
    ADULT_MALE("Male", 17, 40);

    private final String name;
    private final int minSize;
    private final int maxSize;

    GrowthState(String name, int minSize, int maxSize) {
	this.name = name;
	this.minSize = minSize;
	this.maxSize = maxSize;
    }

    public String getName() {
	return name;
    }

    public void print() {
	for (GrowthState m : GrowthState.values()) {
	    // System.out.printf("Life stage %s is %d%d%n", m, m.name(),
	    // m.color());
	}
    }

    // int minSize= LifeStage.JUVENILE.getMinAge()
};