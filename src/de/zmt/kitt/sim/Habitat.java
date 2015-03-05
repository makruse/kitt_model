package de.zmt.kitt.sim;

import java.awt.Color;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.AmountUtil;

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
	public double getFoodMin() {
	    return CORALREEF_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return CORALREEF_FOOD_MAX;
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
	public double getFoodMin() {
	    return SEAGRASS_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return SEAGRASS_FOOD_MAX;
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
	public double getFoodMin() {
	    return MANGROVE_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return MANGROVE_FOOD_MAX;
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
	public double getFoodMin() {
	    return ROCK_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return ROCK_FOOD_MAX;
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
	public double getFoodMin() {
	    return SANDYBOTTOM_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return SANDYBOTTOM_FOOD_MAX;
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
	public double getFoodMin() {
	    return MAINLAND_FOOD_MIN;
	}

	@Override
	public double getFoodMax() {
	    return MAINLAND_FOOD_MAX;
	}

	@Override
	public Amount<Frequency> getMortalityRisk() {
	    return MAINLAND_MORTALITY_RISK;
	}
    };

    private static final String CORALREEF_NAME = "coral reef";
    private static final Color CORALREEF_COLOR = Color.CYAN;
    private static final double CORALREEF_FOOD_MIN = 10;
    private static final double CORALREEF_FOOD_MAX = 14;
    private static final Amount<Frequency> CORALREEF_MORTALITY_RISK = Amount
	    .valueOf(0.002, AmountUtil.PER_DAY);

    private static final String SEAGRASS_NAME = "seagrass bed";
    private static final Color SEAGRASS_COLOR = Color.GREEN;
    private static final double SEAGRASS_FOOD_MIN = 5;
    private static final double SEAGRASS_FOOD_MAX = 10;
    private static final Amount<Frequency> SEAGRASS_MORTALITY_RISK = Amount
	    .valueOf(0.001, AmountUtil.PER_DAY);

    private static final String MANGROVE_NAME = "mangrove";
    private static final Color MANGROVE_COLOR = new Color(0, 178, 0);
    private static final double MANGROVE_FOOD_MIN = 3;
    private static final double MANGROVE_FOOD_MAX = 5;
    private static final Amount<Frequency> MANGROVE_MORTALITY_RISK = Amount
	    .valueOf(0.02, AmountUtil.PER_DAY);

    private static final String ROCK_NAME = "rock";
    private static final Color ROCK_COLOR = Color.LIGHT_GRAY;
    private static final double ROCK_FOOD_MIN = 5;
    private static final double ROCK_FOOD_MAX = 6;
    private static final Amount<Frequency> ROCK_MORTALITY_RISK = Amount
	    .valueOf(0.001, AmountUtil.PER_DAY);

    private static final String SANDYBOTTOM_NAME = "sandy bottom";
    private static final Color SANDYBOTTOM_COLOR = Color.YELLOW;
    private static final double SANDYBOTTOM_FOOD_MIN = 0;
    private static final double SANDYBOTTOM_FOOD_MAX = 3;
    private static final Amount<Frequency> SANDYBOTTOM_MORTALITY_RISK = Amount
	    .valueOf(0.008, AmountUtil.PER_DAY);

    private static final String MAINLAND_NAME = "mainland";
    private static final Color MAINLAND_COLOR = Color.WHITE;
    private static final double MAINLAND_FOOD_MIN = 0;
    private static final double MAINLAND_FOOD_MAX = 0;
    private static final Amount<Frequency> MAINLAND_MORTALITY_RISK = AmountUtil
	    .zero(AmountUtil.PER_DAY);

    public static Habitat DEFAULT = SANDYBOTTOM;
    public static final double FOOD_MAX_GENERAL = CORALREEF.getFoodMax();

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

    public abstract String getName();

    /**
     * Color associated to habitat in image
     * 
     * @return Color
     */
    public abstract Color getColor();

    public abstract double getFoodMin();

    public abstract double getFoodMax();

    public abstract Amount<Frequency> getMortalityRisk();
}