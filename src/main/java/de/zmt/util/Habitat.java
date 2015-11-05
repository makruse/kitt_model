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
    CORALREEF {
	@Override
	public String getName() {
	    return CORALREEF_NAME;
	}

	@Override
	public Color getColor() {
	    return CORALREEF_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return CORALREEF_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return CORALREEF_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return CORALREEF_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return CORALREEF_PREDATION_RISK;
	}
    },
    /** Seagrass bed: green color */
    SEAGRASS {
	@Override
	public String getName() {
	    return SEAGRASS_NAME;
	}

	@Override
	public Color getColor() {
	    return SEAGRASS_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return SEAGRASS_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return SEAGRASS_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return SEAGRASS_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return SEAGRASS_PREDATION_RISK;
	}
    },
    /** Mangrove: dark green color, highest risk of predation */
    MANGROVE {
	@Override
	public String getName() {
	    return MANGROVE_NAME;
	}

	@Override
	public Color getColor() {
	    return MANGROVE_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return MANGROVE_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return MANGROVE_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return MANGROVE_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return MANGROVE_PREDATION_RISK;
	}
    },
    /** Rock: Gray color */
    ROCK() {
	@Override
	public String getName() {
	    return ROCK_NAME;
	}

	@Override
	public Color getColor() {
	    return ROCK_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return ROCK_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return ROCK_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return ROCK_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return ROCK_PREDATION_RISK;
	}
    },
    /** Sandy bottom: yellow color */
    SANDYBOTTOM {
	@Override
	public String getName() {
	    return SANDYBOTTOM_NAME;
	}

	@Override
	public Color getColor() {
	    return SANDYBOTTOM_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return SANDYBOTTOM_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return SANDYBOTTOM_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return SANDYBOTTOM_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return SANDYBOTTOM_PREDATION_RISK;
	}
    },
    /** Main land: white color */
    MAINLAND {
	@Override
	public String getName() {
	    return MAINLAND_NAME;
	}

	@Override
	public Color getColor() {
	    return MAINLAND_COLOR;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMin() {
	    return MAINLAND_FOOD_MIN;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityMax() {
	    return MAINLAND_FOOD_MAX;
	}

	@Override
	public Amount<AreaDensity> getFoodDensityRange() {
	    return MAINLAND_FOOD_RANGE;
	}

	@Override
	public Amount<Frequency> getPredationRisk() {
	    return MAINLAND_PREDATION_RISK;
	}
    };

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

    private static final Amount<AreaDensity> CORALREEF_FOOD_MIN = Amount.valueOf(CORALREEF_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> CORALREEF_FOOD_MAX = Amount.valueOf(CORALREEF_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> CORALREEF_FOOD_RANGE = Amount
	    .valueOf(CORALREEF_FOOD_MAX_VALUE - CORALREEF_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> CORALREEF_PREDATION_RISK = Amount
	    .valueOf(CORALREEF_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    private static final Amount<AreaDensity> SEAGRASS_FOOD_MIN = Amount.valueOf(SEAGRASS_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SEAGRASS_FOOD_MAX = Amount.valueOf(SEAGRASS_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SEAGRASS_FOOD_RANGE = Amount
	    .valueOf(SEAGRASS_FOOD_MAX_VALUE - SEAGRASS_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> SEAGRASS_PREDATION_RISK = Amount
	    .valueOf(SEAGRASS_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    private static final Amount<AreaDensity> MANGROVE_FOOD_MIN = Amount.valueOf(MANGROVE_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MANGROVE_FOOD_MAX = Amount.valueOf(MANGROVE_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MANGROVE_FOOD_RANGE = Amount
	    .valueOf(MANGROVE_FOOD_MAX_VALUE - MANGROVE_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> MANGROVE_PREDATION_RISK = Amount
	    .valueOf(MANGROVE_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    private static final Amount<AreaDensity> ROCK_FOOD_MIN = Amount.valueOf(ROCK_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> ROCK_FOOD_MAX = Amount.valueOf(ROCK_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> ROCK_FOOD_RANGE = Amount.valueOf(ROCK_FOOD_MAX_VALUE - ROCK_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> ROCK_PREDATION_RISK = Amount
	    .valueOf(ROCK_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_MIN = Amount.valueOf(SANDYBOTTOM_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_MAX = Amount.valueOf(SANDYBOTTOM_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_RANGE = Amount
	    .valueOf(SANDYBOTTOM_FOOD_MAX_VALUE - SANDYBOTTOM_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> SANDYBOTTOM_PREDATION_RISK = Amount
	    .valueOf(SANDYBOTTOM_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    private static final Amount<AreaDensity> MAINLAND_FOOD_MIN = Amount.valueOf(MAINLAND_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MAINLAND_FOOD_MAX = Amount.valueOf(MAINLAND_FOOD_MAX_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MAINLAND_FOOD_RANGE = Amount
	    .valueOf(MAINLAND_FOOD_MAX_VALUE - MAINLAND_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> MAINLAND_PREDATION_RISK = Amount
	    .valueOf(MAINLAND_PREDATION_RISK_PER_DAY_VALUE, UnitConstants.PER_DAY).to(UnitConstants.PER_STEP);

    public static Habitat DEFAULT = SANDYBOTTOM;
    /** Maximum range that food density can vary within a patch. */
    public static final double MAX_FOOD_RANGE = CORALREEF_FOOD_MAX_VALUE - CORALREEF_FOOD_MIN_VALUE;
    public static final Amount<Frequency> MAX_PREDATION_RISK = SANDYBOTTOM_PREDATION_RISK;

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

    public abstract String getName();

    /**
     * Color associated to habitat in image
     * 
     * @return Color
     */
    public abstract Color getColor();

    /**
     * @return Minimum food density in gram dry mass per square meter.
     */
    public abstract Amount<AreaDensity> getFoodDensityMin();

    /**
     * @return Maximum food density in gram dry mass per square meter.
     */
    public abstract Amount<AreaDensity> getFoodDensityMax();

    /**
     * @return Maximum minus minimum food density in gram dry mass per square
     *         meter
     */
    public abstract Amount<AreaDensity> getFoodDensityRange();

    /**
     * Estimated predation risk as a summarizing factor of habitat complexity,
     * available refuge and predator abundances.
     * 
     * @return predation risk for this habitat
     */
    public abstract Amount<Frequency> getPredationRisk();
}