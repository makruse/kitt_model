package de.zmt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.AngularVelocity;
import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.Unit;

import de.zmt.params.EnvironmentDefinition;
import de.zmt.util.quantity.AreaDensity;
import de.zmt.util.quantity.LinearMassDensity;
import de.zmt.util.quantity.SpecificEnergy;

/**
 * Definitions of measurement units for their specific applications.
 * 
 * @author mey
 * 
 */
public final class UnitConstants {
    private UnitConstants() {

    }

    // MASS
    /** Unit for measuring food: g */
    public static final Unit<Mass> FOOD = GRAM;
    /** Unit for measuring biomass: g */
    public static final Unit<Mass> BIOMASS = GRAM;

    // ENERGY
    /** Unit for measuring energy stored in cells: kJ */
    public static final Unit<Energy> CELLULAR_ENERGY = KILO(JOULE);

    // LENGTH
    /** Unit for measuring distances in the world: m */
    public static final Unit<Length> WORLD_DISTANCE = METER;
    /** Unit for measuring body length of species: cm */
    public static final Unit<Length> BODY_LENGTH = CENTIMETER;

    // DURATION
    /** Unit for measuring simulation time: s */
    public static final Unit<Duration> SIMULATION_TIME = SECOND;
    /** Unit for measuring agent age: s */
    public static final Unit<Duration> AGE = SIMULATION_TIME;
    /** Unit for displaying age parameters in the GUI: year */
    public static final Unit<Duration> AGE_GUI = YEAR;

    // VELOCITY
    /** Unit for time in velocity units: s */
    public static final Unit<Duration> VELOCITY_TIME = SECOND;
    /** Unit for measuring velocity: m/s */
    public static final Unit<Velocity> VELOCITY = METER.divide(VELOCITY_TIME).asType(Velocity.class);
    /** Unit for measuring angular velocity: rad/s */
    public static final Unit<AngularVelocity> ANGULAR_VELOCITY = RADIAN.divide(VELOCITY_TIME)
	    .asType(AngularVelocity.class);
    /** Unit for displaying angular velocity in the GUI: &deg;/s */
    public static final Unit<AngularVelocity> ANGULAR_VELOCITY_GUI = DEGREE_ANGLE.divide(SECOND)
	    .asType(AngularVelocity.class);
    /** Unit for displaying velocity factors on body length in the GUI: BL/s */
    public static final Unit<Frequency> BODY_LENGTH_VELOCITY = Unit.ONE.alternate("BL").divide(VELOCITY_TIME)
	    .asType(Frequency.class);

    // POWER
    /** Unit for measuring energy per time: kJ/h */
    public static final Unit<Power> ENERGY_PER_TIME = CELLULAR_ENERGY.divide(HOUR).asType(Power.class);

    // CONVERSION
    /** Unit for measuring the energy content of body tissue: kJ/g */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_TISSUE = CELLULAR_ENERGY.divide(BIOMASS)
	    .asType(SpecificEnergy.class);
    /** Unit for measuring the energy content of food: kJ/g */
    public static final Unit<SpecificEnergy> ENERGY_CONTENT_FOOD = CELLULAR_ENERGY.divide(FOOD)
	    .asType(SpecificEnergy.class);
    /** Unit for measuring relationships between mass and length: g/cm */
    public static final Unit<LinearMassDensity> MASS_PER_LENGTH = BIOMASS.divide(BODY_LENGTH)
	    .asType(LinearMassDensity.class);

    // AREA
    /** Unit for measuring area in world space: m<sup>2</sup> */
    public static final Unit<Area> WORLD_AREA = SQUARE_METRE;

    // AREA DENSITY
    /** Unit for measuring the amount of food within an area: g/m<sup>2</sup> */
    public static final Unit<AreaDensity> FOOD_DENSITY = FOOD.divide(WORLD_AREA).asType(AreaDensity.class);

    public static final Unit<Frequency> PER_STEP = AmountUtil.convertToUnit(EnvironmentDefinition.STEP_DURATION)
	    .inverse().asType(Frequency.class);
    public static final Unit<Frequency> PER_SECOND = HERTZ;
    public static final Unit<Frequency> PER_HOUR = Unit.ONE.divide(HOUR).asType(Frequency.class);
    public static final Unit<Frequency> PER_DAY = Unit.ONE.divide(DAY).asType(Frequency.class);
    public static final Unit<Frequency> PER_YEAR = Unit.ONE.divide(YEAR).asType(Frequency.class);
}
