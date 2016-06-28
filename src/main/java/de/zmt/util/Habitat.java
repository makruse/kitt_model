package de.zmt.util;

import static de.zmt.util.HabitatConstants.*;

import java.awt.Color;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamAlias;

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
@XStreamAlias("Habitat")
public enum Habitat {
    /** Coral reef: cyan color */
    CORALREEF(CORALREEF_NAME, CORALREEF_COLOR, CORALREEF_FOOD_MIN_VALUE, CORALREEF_FOOD_MAX_VALUE,
            CORALREEF_SPEED_FACTOR),
    /** Seagrass bed: green color */
    SEAGRASS(SEAGRASS_NAME, SEAGRASS_COLOR, SEAGRASS_FOOD_MIN_VALUE, SEAGRASS_FOOD_MAX_VALUE, SEAGRASS_SPEED_FACTOR),
    /** Mangrove: dark green color, highest risk of predation */
    MANGROVE(MANGROVE_NAME, MANGROVE_COLOR, MANGROVE_FOOD_MIN_VALUE, MANGROVE_FOOD_MAX_VALUE, MANGROVE_SPEED_FACTOR),
    /** Rock: Gray color */
    ROCK(ROCK_NAME, ROCK_COLOR, ROCK_FOOD_MIN_VALUE, ROCK_FOOD_MAX_VALUE, ROCK_SPEED_FACTOR),
    /** Sandy bottom: yellow color */
    SANDYBOTTOM(SANDYBOTTOM_NAME, SANDYBOTTOM_COLOR, SANDYBOTTOM_FOOD_MIN_VALUE, SANDYBOTTOM_FOOD_MAX_VALUE,
            SANDYBOTTOM_SPEED_FACTOR),
    /** Main land: white color */
    MAINLAND(MAINLAND_NAME, MAINLAND_COLOR, MAINLAND_FOOD_MIN_VALUE, MAINLAND_FOOD_MAX_VALUE, MAINLAND_SPEED_FACTOR);

    public static final Habitat DEFAULT = SANDYBOTTOM;
    /** Maximum range that food density can vary within a patch. */
    public static final double MAX_FOOD_RANGE = CORALREEF_FOOD_MAX_VALUE - CORALREEF_FOOD_MIN_VALUE;

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
    private final double speedFactor;

    private Habitat(String name, Color color, double foodDensityMinValue, double foodDensityMaxValue,
            double speedFactor) {
        this.name = name;
        this.color = color;
        this.foodDensityMin = Amount.valueOf(foodDensityMinValue, UnitConstants.FOOD_DENSITY);
        this.foodDensityMax = Amount.valueOf(foodDensityMaxValue, UnitConstants.FOOD_DENSITY);
        this.foodDensityRange = Amount.valueOf(foodDensityMaxValue - foodDensityMinValue, UnitConstants.FOOD_DENSITY);
        this.speedFactor = speedFactor;
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
     * @return the speed factor applied on agent's speed when moving within this
     *         habitat
     */
    public double getSpeedFactor() {
        return speedFactor;
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