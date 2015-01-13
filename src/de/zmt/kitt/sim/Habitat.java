package de.zmt.kitt.sim;

import java.awt.Color;

public enum Habitat {
    // algal turf standing crop in g/m2 as guideline:
    // after Cliffton 1995 5-14 g/m2 in reef
    /** Coral reef: cyan color */
    CORALREEF("coral reef", Color.cyan, 10.0, 14.0, 0.002),
    /** Seagrass bed: green color */
    SEAGRASS("seagrass bed", Color.green, 5.0, 10.0, 0.001),
    /** Mangrove: dark green color, highest risk of predation */
    MANGROVE("mangrove", new Color(0, 178, 0), 3.0, 5.0, 0.02),
    /** Rock: Gray color */
    ROCK("rock", Color.lightGray, 5.0, 6.0, 0.001),
    /** Sandy bottom: yellow color */
    SANDYBOTTOM("sandy bottom", Color.yellow, 0.0, 3.0, 0.008),
    /** Main land: white color */
    MAINLAND("mainland", Color.white, 0.0, 0.0, 0.0);

    public static Habitat DEFAULT = SANDYBOTTOM;

    /**
     * 
     * @param color
     * @return habitat associated with color or null.
     */
    public static Habitat valueOf(Color color) {
	for (Habitat habitat : Habitat.values()) {
	    if (habitat.getColor().equals(color)) {
		return habitat;
	    }
	}
	return null;
    }

    private final String name;
    /** color in the input map that is associated with this habitat */
    private final Color color;
    private final double initialFoodMin;
    private final double initialFoodMax;
    private final double mortalityPredation;

    private Habitat(String name, Color color, double initialFoodMin,
	    double initialFoodMax, double mortalityPredation) {
	this.name = name;
	this.color = color;
	this.initialFoodMin = initialFoodMin;
	this.initialFoodMax = initialFoodMax;
	this.mortalityPredation = mortalityPredation;
    }

    public String getName() {
	return name;
    }

    /** Color associated to habitat in image */
    public Color getColor() {
	return color;
    }

    public double getInitialFoodMin() {
	return initialFoodMin;
    }

    public double getInitialFoodMax() {
	return initialFoodMax;
    }

    public double getMortalityPredation() {
	return mortalityPredation;
    }
}