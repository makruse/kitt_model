package de.zmt.kitt.sim;

import java.awt.Color;

public enum HabitatHerbivore {

    // habitat seleciton rules due to activity pattern and risk and food??
    // habitat hat ID, name, farbe, minFood, maxFood, predRisk, determines
    // survival and food availability
    // algal turf standing crop in g/m2, als richtlinine: nach Cliffton 1995:
    // 5-14 g/m2 in reef
    // neue Angaben in gramm/m2
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
    // UNSPECIFIED(0, "unspecified",new Color(220,220,220), 0.0, 0.0 ); // light
    // grey

    // Unterscheidung der food initialwerte f�r habitate zw carnivoren und
    // herbivor
    private final String name;
    /** color in the input map that is related to this habitat */
    private final Color color;
    /** typical algal standing crop in g/m2 */
    private final double initialFoodMin;
    private final double initialFoodMax;
    private final double mortalityPredation;

    private HabitatHerbivore(String name, Color color, double initialFoodMin,
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