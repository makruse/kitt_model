package de.zmt.kitt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.concurrent.TimeUnit;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.*;

import de.zmt.kitt.util.quantity.EnergyDensity;

/**
 * General utility methods for dealing with jScience {@link Amount}s.
 * 
 * @author cmeyer
 * 
 */
public class AmountUtil {
    // DEFAULT UNITS
    public static final Unit<Mass> MASS_UNIT = GRAM;
    public static final Unit<Energy> ENERGY_UNIT = KILO(JOULE);
    public static final Unit<Length> LENGTH_UNIT = METER;
    public static final Unit<Length> SHORT_LENGTH_UNIT = CENTIMETER;
    public static final Unit<Duration> DURATION_UNIT = MINUTE;
    public static final Unit<Velocity> VELOCITY_UNIT = METERS_PER_SECOND;
    public static final Unit<EnergyDensity> ENERGY_DENSITY_UNIT = EnergyDensity.UNIT;

    public static final Unit<Frequency> PER_HOUR = Unit.ONE.divide(HOUR)
	    .asType(Frequency.class);
    public static final Unit<Frequency> PER_DAY = Unit.ONE.divide(DAY).asType(
	    Frequency.class);
    public static final Unit<Frequency> PER_YEAR = Unit.ONE.divide(YEAR)
	    .asType(Frequency.class);

    public static final AmountFormat FORMAT = AmountFormat
	    .getExactDigitsInstance();
    private static final AmountFormat FORMAT_IN = AmountFormat
	    .getPlusMinusErrorInstance(2);

    /**
     * 
     * @param unit
     *            of returned amount
     * @return zero amount of given unit
     */
    public static <Q extends Quantity> Amount<Q> zero(Unit<Q> unit) {
	return Amount.valueOf(0, unit);
    }

    /**
     * 
     * @param amount
     * @return zero amount with the same unit of given amount
     */
    public static <Q extends Quantity> Amount<Q> zero(Amount<Q> amount) {
	return zero(amount.getUnit());
    }

    /**
     * 
     * @param unit
     *            of returned amount
     * @return zero amount of given unit
     */
    public static <Q extends Quantity> Amount<Q> one(Unit<Q> unit) {
	return Amount.valueOf(1, unit);
    }

    /**
     * 
     * @param amount
     * @return zero amount with the same unit of given amount
     */
    public static <Q extends Quantity> Amount<Q> one(Amount<Q> amount) {
	return one(amount.getUnit());
    }

    /** @see Math#min(double, double) */
    public static <Q extends Quantity> Amount<Q> min(Amount<Q> a, Amount<Q> b) {
	return (a.isLessThan(b) || a.equals(b)) ? a : b;
    }

    /** @see Math#max(double, double) */
    public static <Q extends Quantity> Amount<Q> max(Amount<Q> a, Amount<Q> b) {
	return (a.isGreaterThan(b) || a.equals(b)) ? a : b;
    }

    /**
     * Converts a {@link Duration} {@link Amount} to a long value in given
     * {@link TimeUnit}.
     * 
     * @param duration
     * @param timeUnit
     * @return long value of {@code duration} in {@code timeUnit}
     */
    public static long toTimeUnit(Amount<Duration> duration, TimeUnit timeUnit) {
	Unit<Duration> unit = toDurationUnit(timeUnit);
	Amount<Duration> convertedDuration = duration.to(unit);
	if (convertedDuration.isExact()) {
	    return convertedDuration.getExactValue();
	} else {
	    return (long) convertedDuration.getEstimatedValue();
	}
    }

    private static Unit<Duration> toDurationUnit(TimeUnit timeUnit) {
	switch (timeUnit) {
	case NANOSECONDS:
	    return NANO(SECOND);
	case MILLISECONDS:
	    return MILLI(SECOND);
	case MICROSECONDS:
	    return MICRO(SECOND);
	case SECONDS:
	    return SECOND;
	case MINUTES:
	    return MINUTE;
	case HOURS:
	    return HOUR;
	case DAYS:
	    return DAY;
	}
	throw new IllegalArgumentException(timeUnit
		+ " cannot be converted to " + Unit.class.getSimpleName());
    }

    /**
     * Parse an {@link Amount} and convert it to given unit.
     * 
     * @param amountString
     * @param unit
     * @return Parsed {@code amountString} in given unit.
     */
    public static <Q extends Quantity> Amount<Q> parseAmount(
	    String amountString, Unit<Q> unit) {
	return FORMAT_IN.parse(amountString).to(unit);
    }

    public static Amount<Mass> parseMass(String massString) {
	return parseAmount(massString, MASS_UNIT);
    }

    public static Amount<Energy> parseEnergy(String energyString) {
	return parseAmount(energyString, ENERGY_UNIT);
    }

    public static Amount<Length> parseShortLength(String shortLengthString) {
	return parseAmount(shortLengthString, SHORT_LENGTH_UNIT);
    }

    public static Amount<Duration> parseDuration(String durationString) {
	return parseAmount(durationString, DURATION_UNIT);
    }

    public static Amount<Velocity> parseVelocity(String velocityString) {
	return parseAmount(velocityString, VELOCITY_UNIT);
    }

    public static Amount<EnergyDensity> parseEnergyDensity(
	    String energyDensityString) {
	return parseAmount(energyDensityString, ENERGY_DENSITY_UNIT);
    }
}
