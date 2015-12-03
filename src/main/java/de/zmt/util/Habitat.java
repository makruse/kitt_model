package de.zmt.util;

import java.awt.Color;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.util.quantity.AreaDensity;

/**
 * Habitats available in the environment.
 * <p>
 * Minimum / maximum constants are estimated values based on information of
 * algal standing crop. 5-14 g/m2 for coral reef as guideline.
 * 
 * @see "Cliffton 1995"
 * 
 * @author mey
 *
 */
public enum Habitat {
    /** Coral reef: cyan color */
    CORALREEF(Habitat.CORALREEF_NAME, Habitat.CORALREEF_COLOR, Habitat.CORALREEF_FOOD_MIN_VALUE,
	    Habitat.CORALREEF_FOOD_MAX_VALUE, Habitat.CORALREEF_PREDATION_RISK_PER_DAY_VALUE),
    /** Seagrass bed: green color */
    SEAGRASS(Habitat.SEAGRASS_NAME, Habitat.SEAGRASS_COLOR, Habitat.SEAGRASS_FOOD_MIN_VALUE,
	    Habitat.SEAGRASS_FOOD_MAX_VALUE, Habitat.SEAGRASS_PREDATION_RISK_PER_DAY_VALUE),
    /** Mangrove: dark green color, highest risk of predation */
    MANGROVE(Habitat.MANGROVE_NAME, Habitat.MANGROVE_COLOR, Habitat.MANGROVE_FOOD_MIN_VALUE,
	    Habitat.MANGROVE_FOOD_MAX_VALUE, Habitat.MANGROVE_PREDATION_RISK_PER_DAY_VALUE),
    /** Rock: Gray color */
    ROCK(Habitat.ROCK_NAME, Habitat.ROCK_COLOR, Habitat.ROCK_FOOD_MIN_VALUE, Habitat.ROCK_FOOD_MAX_VALUE,
	    Habitat.ROCK_PREDATION_RISK_PER_DAY_VALUE),
    /** Sandy bottom: yellow color */
    SANDYBOTTOM(Habitat.SANDYBOTTOM_NAME, Habitat.SANDYBOTTOM_COLOR, Habitat.SANDYBOTTOM_FOOD_MIN_VALUE,
	    Habitat.SANDYBOTTOM_FOOD_MAX_VALUE, Habitat.SANDYBOTTOM_PREDATION_RISK_PER_DAY_VALUE),
    /** Main land: white color */
    MAINLAND(Habitat.MAINLAND_NAME, Habitat.MAINLAND_COLOR, Habitat.MAINLAND_FOOD_MIN_VALUE,
	    Habitat.MAINLAND_FOOD_MAX_VALUE, Habitat.MAINLAND_PREDATION_RISK_PER_DAY_VALUE);

    private static final String CORALREEF_NAME = "coral reef";
    private static final Color CORALREEF_COLOR = Color.CYAN;
    private static final double CORALREEF_FOOD_MIN_VALUE = 5;
    private static final double CORALREEF_FOOD_MAX_VALUE = 14;
    private static final double CORALREEF_PREDATION_RISK_PER_DAY_VALUE = 0.002;

    private static final String SEAGRASS_NAME = "seagrass bed";
    private static final Color SEAGRASS_COLOR = Color.GREEN;
    private static final double SEAGRASS_FOOD_MIN_VALUE = 5;
    private static final double SEAGRASS_FOOD_MAX_VALUE = 10;
    private static final double SEAGRASS_PREDATION_RISK_PER_DAY_VALUE = 0.001;

    private static final String MANGROVE_NAME = "mangrove";
    private static final Color MANGROVE_COLOR = new Color(0, 178, 0);
    private static final double MANGROVE_FOOD_MIN_VALUE = 3;
    private static final double MANGROVE_FOOD_MAX_VALUE = 5;
    private static final double MANGROVE_PREDATION_RISK_PER_DAY_VALUE = 0.002;

    private static final String ROCK_NAME = "rock";
    private static final Color ROCK_COLOR = Color.LIGHT_GRAY;
    private static final double ROCK_FOOD_MIN_VALUE = 2;
    private static final double ROCK_FOOD_MAX_VALUE = 5;
    private static final double ROCK_PREDATION_RISK_PER_DAY_VALUE = 0.004;

    private static final String SANDYBOTTOM_NAME = "sandy bottom";
    private static final Color SANDYBOTTOM_COLOR = Color.YELLOW;
    private static final double SANDYBOTTOM_FOOD_MIN_VALUE = 0.1;
    private static final double SANDYBOTTOM_FOOD_MAX_VALUE = 3;
    private static final double SANDYBOTTOM_PREDATION_RISK_PER_DAY_VALUE = 0.008;

    private static final String MAINLAND_NAME = "mainland";
    private static final Color MAINLAND_COLOR = Color.WHITE;
    private static final double MAINLAND_FOOD_MIN_VALUE = 0;
    private static final double MAINLAND_FOOD_MAX_VALUE = 0;
    /** Very deadly for fish. */
    private static final int MAINLAND_PREDATION_RISK_PER_DAY_VALUE = 1;

    public static Habitat DEFAULT = SANDYBOTTOM;
    /** Maximum range that food density can vary within a patch. */
    public static final double MAX_FOOD_RANGE = CORALREEF_FOOD_MAX_VALUE - CORALREEF_FOOD_MIN_VALUE;
    /** Maximum predation risk <b>excluding</b> {@code MAINLAND}. */
    public static final Amount<Frequency> MAX_PREDATION_RISK = SANDYBOTTOM.getPredationRisk();

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
	throw new IllegalArgumentException(color + " is not associated with a " + Habitat.class.getSimpleName());
    }

    private final String name;
    private final Color color;
    private final Amount<AreaDensity> foodDensityMin;
    private final Amount<AreaDensity> foodDensityMax;
    private final Amount<AreaDensity> foodDensityRange;
    private final Amount<Frequency> predationRisk;

    private Habitat(String name, Color color, double foodDensityMinValue, double foodDensityMaxValue,
	    double predationRiskPerDay) {
	this.name = name;
	this.color = color;
	this.foodDensityMin = Amount.valueOf(foodDensityMinValue, UnitConstants.FOOD_DENSITY);
	this.foodDensityMax = Amount.valueOf(foodDensityMaxValue, UnitConstants.FOOD_DENSITY);
	this.foodDensityRange = Amount.valueOf(foodDensityMaxValue - foodDensityMinValue, UnitConstants.FOOD_DENSITY);
	this.predationRisk = Amount.valueOf(predationRiskPerDay, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);
    }

    public String getName() {
	return name;
    }

    /**
     * Color associated to habitat in image
     * 
     * @return Color
     */
    public Color getColor() {
	return color;
    }

    /**
     * @return Minimum food density in gram dry mass per square meter.
     */
    public Amount<AreaDensity> getFoodDensityMin() {
	return foodDensityMin;
    }

    /**
     * @return Maximum food density in gram dry mass per square meter.
     */
    public Amount<AreaDensity> getFoodDensityMax() {
	return foodDensityMax;
    }

    /**
     * @return Maximum minus minimum food density in gram dry mass per square
     *         meter
     */
    public Amount<AreaDensity> getFoodDensityRange() {
	return foodDensityRange;
    }

    /**
     * Estimated predation risk as a summarizing factor of habitat complexity,
     * available refuge and predator abundances.
     * 
     * @return predation risk for this habitat
     */
    public Amount<Frequency> getPredationRisk() {
	return predationRisk;
    }

    /**
     * Habitats are accessible when agents can move on them. Inaccessible
     * habitats ({@link #MAINLAND}) act as a barrier and will not have any food
     * grown on them.
     * 
     * @return <code>true</code> if this habitat can be accessed by agents
     */
    public boolean isAccessible() {
	if (this == MAINLAND) {
	    return false;
	}
	return true;
    }
}