package de.zmt.kitt.sim;

import java.awt.Color;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.AreaDensity;

public enum Habitat {
    // algal turf standing crop in g/m2 as guideline:
    // after Cliffton 1995 5-14 g/m2 in reef
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
	public Amount<Frequency> getMortalityRisk() {
	    return CORALREEF_MORTALITY_RISK;
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
	public Amount<Frequency> getMortalityRisk() {
	    return SEAGRASS_MORTALITY_RISK;
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
	public Amount<Frequency> getMortalityRisk() {
	    return MANGROVE_MORTALITY_RISK;
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
	public Amount<Frequency> getMortalityRisk() {
	    return ROCK_MORTALITY_RISK;
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
	public Amount<Frequency> getMortalityRisk() {
	    return SANDYBOTTOM_MORTALITY_RISK;
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
	public Amount<Frequency> getMortalityRisk() {
	    return MAINLAND_MORTALITY_RISK;
	}
    };

    private static final String CORALREEF_NAME = "coral reef";
    private static final Color CORALREEF_COLOR = Color.CYAN;
    private static final int CORALREEF_FOOD_MIN_VALUE = 5;
    private static final int CORALREEF_FOOD_MAX_VALUE = 14;
    private static final double CORALREEF_MORTALITY_RISK_VALUE = 0.002;

    private static final String SEAGRASS_NAME = "seagrass bed";
    private static final Color SEAGRASS_COLOR = Color.GREEN;
    private static final int SEAGRASS_FOOD_MIN_VALUE = 5;
    private static final int SEAGRASS_FOOD_MAX_VALUE = 10;
    private static final double SEAGRASS_MORTALITY_RISK_VALUE = 0.001;

    private static final String MANGROVE_NAME = "mangrove";
    private static final Color MANGROVE_COLOR = new Color(0, 178, 0);
    private static final int MANGROVE_FOOD_MIN_VALUE = 3;
    private static final int MANGROVE_FOOD_MAX_VALUE = 5;
    private static final double MANGROVE_MORTALITY_RISK_VALUE = 0.002;

    private static final String ROCK_NAME = "rock";
    private static final Color ROCK_COLOR = Color.LIGHT_GRAY;
    private static final int ROCK_FOOD_MIN_VALUE = 2;
    private static final int ROCK_FOOD_MAX_VALUE = 5;
    private static final double ROCK_MORTALITY_RISK_VALUE = 0.004;

    private static final String SANDYBOTTOM_NAME = "sandy bottom";
    private static final Color SANDYBOTTOM_COLOR = Color.YELLOW;
    private static final double SANDYBOTTOM_FOOD_MIN_VALUE = 0.1;
    private static final int SANDYBOTTOM_FOOD_MAX_VALUE = 3;
    private static final double SANDYBOTTOM_MORTALITY_RISK_VALUE = 0.008;

    private static final String MAINLAND_NAME = "mainland";
    private static final Color MAINLAND_COLOR = Color.WHITE;
    private static final int MAINLAND_FOOD_MIN_VALUE = 0;
    private static final int MAINLAND_FOOD_MAX_VALUE = 0;
    private static final int MAINLAND_MORTALITY_RISK_VALUE = 0;

    private static final Amount<AreaDensity> CORALREEF_FOOD_MIN = Amount
	    .valueOf(CORALREEF_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> CORALREEF_FOOD_MAX = Amount
	    .valueOf(CORALREEF_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> CORALREEF_FOOD_RANGE = Amount
	    .valueOf(CORALREEF_FOOD_MAX_VALUE - CORALREEF_FOOD_MIN_VALUE,
		    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> CORALREEF_MORTALITY_RISK = Amount
	    .valueOf(CORALREEF_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    private static final Amount<AreaDensity> SEAGRASS_FOOD_MIN = Amount
	    .valueOf(SEAGRASS_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SEAGRASS_FOOD_MAX = Amount
	    .valueOf(SEAGRASS_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SEAGRASS_FOOD_RANGE = Amount
	    .valueOf(SEAGRASS_FOOD_MAX_VALUE - SEAGRASS_FOOD_MIN_VALUE,
		    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> SEAGRASS_MORTALITY_RISK = Amount
	    .valueOf(SEAGRASS_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    private static final Amount<AreaDensity> MANGROVE_FOOD_MIN = Amount
	    .valueOf(MANGROVE_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MANGROVE_FOOD_MAX = Amount
	    .valueOf(MANGROVE_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MANGROVE_FOOD_RANGE = Amount
	    .valueOf(MANGROVE_FOOD_MAX_VALUE - MANGROVE_FOOD_MIN_VALUE,
		    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> MANGROVE_MORTALITY_RISK = Amount
	    .valueOf(MANGROVE_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    private static final Amount<AreaDensity> ROCK_FOOD_MIN = Amount.valueOf(
	    ROCK_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> ROCK_FOOD_MAX = Amount.valueOf(
	    ROCK_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> ROCK_FOOD_RANGE = Amount.valueOf(
	    ROCK_FOOD_MAX_VALUE - ROCK_FOOD_MIN_VALUE,
	    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> ROCK_MORTALITY_RISK = Amount
	    .valueOf(ROCK_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_MIN = Amount
	    .valueOf(SANDYBOTTOM_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_MAX = Amount
	    .valueOf(SANDYBOTTOM_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> SANDYBOTTOM_FOOD_RANGE = Amount
	    .valueOf(SANDYBOTTOM_FOOD_MAX_VALUE - SANDYBOTTOM_FOOD_MIN_VALUE,
		    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> SANDYBOTTOM_MORTALITY_RISK = Amount
	    .valueOf(SANDYBOTTOM_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    private static final Amount<AreaDensity> MAINLAND_FOOD_MIN = Amount
	    .valueOf(MAINLAND_FOOD_MIN_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MAINLAND_FOOD_MAX = Amount
	    .valueOf(MAINLAND_FOOD_MAX_VALUE, UnitConstants.FOOD_DENSITY);
    private static final Amount<AreaDensity> MAINLAND_FOOD_RANGE = Amount
	    .valueOf(MAINLAND_FOOD_MAX_VALUE - MAINLAND_FOOD_MIN_VALUE,
		    UnitConstants.FOOD_DENSITY);
    private static final Amount<Frequency> MAINLAND_MORTALITY_RISK = Amount
	    .valueOf(MAINLAND_MORTALITY_RISK_VALUE, UnitConstants.PER_DAY);

    public static Habitat DEFAULT = SANDYBOTTOM;
    public static final double FOOD_RANGE_MAX = SEAGRASS_FOOD_MAX_VALUE
	    - SEAGRASS_FOOD_MIN_VALUE;

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
	throw new IllegalArgumentException(color + " is not associated with a "
		+ Habitat.class.getSimpleName());
    }

    public abstract String getName();

    /**
     * Color associated to habitat in image
     * 
     * @return Color
     */
    public abstract Color getColor();

    /** @return Minimum food density in gram dry mass per square meter. */
    public abstract Amount<AreaDensity> getFoodDensityMin();

    /** @return Maximum food density in gram dry mass per square meter. */
    public abstract Amount<AreaDensity> getFoodDensityMax();

    /**
     * @return Maximum minus minimum food density in gram dry mass per square
     *         meter
     */
    public abstract Amount<AreaDensity> getFoodDensityRange();

    /** @return Mortality risk per day for this habitat */
    public abstract Amount<Frequency> getMortalityRisk();
}