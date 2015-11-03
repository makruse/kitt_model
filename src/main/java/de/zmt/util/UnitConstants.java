package de.zmt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;

import de.zmt.util.quantity.*;
import sim.params.def.EnvironmentDefinition;

/**
 * Definitions of measurement units for their specific applications.
 * 
 * @author cmeyer
 * 
 */
public final class UnitConstants {
    private UnitConstants() {

    }

    // MASS
    public static final Unit<Mass> FOOD = GRAM;
    public static final Unit<Mass> BIOMASS = GRAM;

    // ENERGY
    /** Unit for energy stored in cells. */
    public static final Unit<Energy> CELLULAR_ENERGY = KILO(JOULE);

    // LENGTH
    /** Unit for measuring distances in the world. */
    public static final Unit<Length> WORLD_DISTANCE = METER;
    /** Unit for measuring body length of species. */
    public static final Unit<Length> BODY_LENGTH = CENTIMETER;

    // DURATION
    public static final Unit<Duration> SIMULATION_TIME = SECOND;
    public static final Unit<Duration> AGE = DAY;
    public static final Unit<Duration> MAX_AGE = YEAR;

    // VELOCITY
    public static final Unit<Duration> VELOCITY_TIME = SECOND;
    public static final Unit<Velocity> VELOCITY = METER.divide(VELOCITY_TIME).asType(Velocity.class);
    public static final Unit<AngularVelocity> ANGULAR_VELOCITY = RADIAN.divide(VELOCITY_TIME)
	    .asType(AngularVelocity.class);
    public static final Unit<AngularVelocity> ANGULAR_VELOCITY_GUI = DEGREE_ANGLE.divide(SECOND)
	    .asType(AngularVelocity.class);

    // POWER
    public static final Unit<Power> ENERGY_PER_TIME = CELLULAR_ENERGY.divide(HOUR).asType(Power.class);

    /** Unit for measuring the energy content of body tissue. */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_TISSUE = CELLULAR_ENERGY.divide(BIOMASS)
	    .asType(SpecificEnergy.class);
    /** Unit for measuring the energy content of food. */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_FOOD = CELLULAR_ENERGY.divide(FOOD)
	    .asType(SpecificEnergy.class);

    // AREA
    public static final Unit<Area> WORLD_AREA = SQUARE_METRE;

    // AREA DENSITY
    /** Unit for measuring the amount of food within an area. */
    public static final Unit<AreaDensity> FOOD_DENSITY = FOOD.divide(WORLD_AREA).asType(AreaDensity.class);

    public static final Unit<Frequency> PER_STEP = AmountUtil.convertToUnit(EnvironmentDefinition.STEP_DURATION)
	    .inverse().asType(Frequency.class);
    public static final Unit<Frequency> PER_HOUR = Unit.ONE.divide(HOUR).asType(Frequency.class);
    public static final Unit<Frequency> PER_DAY = Unit.ONE.divide(DAY).asType(Frequency.class);
    public static final Unit<Frequency> PER_YEAR = Unit.ONE.divide(YEAR).asType(Frequency.class);
}
