package de.zmt.util;

import java.awt.Color;

/**
 * Constants for {@link Habitat} in external class because enums cannot be
 * initialized with constants within the same class.
 * 
 * @author mey
 *
 */
final class HabitatConstants {
    private HabitatConstants() {

    }

    static final String CORALREEF_NAME = "coral reef";
    static final Color CORALREEF_COLOR = Color.CYAN;
    static final double CORALREEF_FOOD_MIN_VALUE = 5;
    static final double CORALREEF_FOOD_MAX_VALUE = 14;
    static final double CORALREEF_PREDATION_RISK_PER_DAY_VALUE = 0.002;

    static final String SEAGRASS_NAME = "seagrass bed";
    static final Color SEAGRASS_COLOR = Color.GREEN;
    static final double SEAGRASS_FOOD_MIN_VALUE = 5;
    static final double SEAGRASS_FOOD_MAX_VALUE = 10;
    static final double SEAGRASS_PREDATION_RISK_PER_DAY_VALUE = 0.001;

    static final String MANGROVE_NAME = "mangrove";
    static final Color MANGROVE_COLOR = new Color(0, 178, 0);
    static final double MANGROVE_FOOD_MIN_VALUE = 3;
    static final double MANGROVE_FOOD_MAX_VALUE = 5;
    static final double MANGROVE_PREDATION_RISK_PER_DAY_VALUE = 0.002;

    static final String ROCK_NAME = "rock";
    static final Color ROCK_COLOR = Color.LIGHT_GRAY;
    static final double ROCK_FOOD_MIN_VALUE = 2;
    static final double ROCK_FOOD_MAX_VALUE = 5;
    static final double ROCK_PREDATION_RISK_PER_DAY_VALUE = 0.004;

    static final String SANDYBOTTOM_NAME = "sandy bottom";
    static final Color SANDYBOTTOM_COLOR = Color.YELLOW;
    static final double SANDYBOTTOM_FOOD_MIN_VALUE = 0.1;
    static final double SANDYBOTTOM_FOOD_MAX_VALUE = 3;
    static final double SANDYBOTTOM_PREDATION_RISK_PER_DAY_VALUE = 0.008;

    static final String MAINLAND_NAME = "mainland";
    static final Color MAINLAND_COLOR = Color.WHITE;
    static final double MAINLAND_FOOD_MIN_VALUE = 0;
    static final double MAINLAND_FOOD_MAX_VALUE = 0;
    /** Very deadly for fish. */
    static final int MAINLAND_PREDATION_RISK_PER_DAY_VALUE = 1;
}