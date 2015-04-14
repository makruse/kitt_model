package de.zmt.kitt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;

import de.zmt.kitt.util.quantity.*;

/**
 * Definitions of measurement units for their specific applications.
 * 
 * @author cmeyer
 * 
 */
public class UnitConstants {
    // MASS
    public static final Unit<Mass> FOOD = GRAM;
    public static final Unit<Mass> BIOMASS = GRAM;

    // ENERGY
    /** Unit for energy stored in cells. */
    public static final Unit<Energy> CELLULAR_ENERGY = KILO(JOULE);

    // LENGTH
    /** Unit for measuring distances on the map. */
    public static final Unit<Length> MAP_DISTANCE = METER;
    /** Unit for measuring body length of species. */
    public static final Unit<Length> BODY_LENGTH = CENTIMETER;

    // DURATION
    public static final Unit<Duration> SIMULATION_TIME = MINUTE;
    public static final Unit<Duration> AGE = DAY;

    // VELOCITY
    public static final Unit<Velocity> VELOCITY = METERS_PER_SECOND;

    // SPECIFIC ENERGY
    private static final Unit<SpecificEnergy> KJ_PER_GRAM = KILO(JOULE)
	    .divide(GRAM).asType(SpecificEnergy.class);
    /** Unit for measuring the energy content of body tissue. */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_TISSUE = KJ_PER_GRAM;
    /** Unit for measuring the energy content of food. */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_FOOD = KJ_PER_GRAM;

    // AREA DENSITY
    /** Unit for measuring the amount of food within an area. */
    public static final Unit<AreaDensity> FOOD_DENSITY = GRAM.divide(
	    SQUARE_METRE).asType(AreaDensity.class);

    public static final Unit<Frequency> PER_HOUR = Unit.ONE.divide(HOUR)
	    .asType(Frequency.class);
    public static final Unit<Frequency> PER_DAY = Unit.ONE.divide(DAY).asType(
	    Frequency.class);
    public static final Unit<Frequency> PER_YEAR = Unit.ONE.divide(YEAR)
	    .asType(Frequency.class);
}