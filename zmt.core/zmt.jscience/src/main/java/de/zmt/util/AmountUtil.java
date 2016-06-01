package de.zmt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.FixedDefaultUnitFormat;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;

import javolution.text.TypeFormat;

/**
 * General utility methods for dealing with jScience {@link Amount}s.
 * 
 * @author mey
 * 
 */
public abstract class AmountUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AmountUtil.class.getName());

    public static final AmountFormat FORMAT = new SimpleAmountFormat();
    private static final UnitFormat UNIT_FORMAT = new FixedDefaultUnitFormat();
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
     * @param amount
     * @return true if given {@code amount} is exactly zero
     */
    public static boolean isZero(Amount<?> amount) {
	return amount.isExact() && amount.getExactValue() == 0;
    }

    /**
     * 
     * @param unit
     *            of returned amount
     * @return amount of given unit with exact value 1
     */
    public static <Q extends Quantity> Amount<Q> one(Unit<Q> unit) {
	return Amount.valueOf(1, unit);
    }

    /**
     * 
     * @param amount
     * @return amount having the same unit of given amount with exact value 1
     */
    public static <Q extends Quantity> Amount<Q> one(Amount<Q> amount) {
	return one(amount.getUnit());
    }

    /**
     * @param a
     * @param b
     * @return minimum
     * @see Math#min(double, double)
     */
    public static <Q extends Quantity> Amount<Q> min(Amount<Q> a, Amount<Q> b) {
	return (a.isLessThan(b) || a.equals(b)) ? a : b;
    }

    /**
     * @param a
     * @param b
     * @return maximum
     * @see Math#max(double, double)
     */
    public static <Q extends Quantity> Amount<Q> max(Amount<Q> a, Amount<Q> b) {
	return (a.isGreaterThan(b) || a.equals(b)) ? a : b;
    }

    /**
     * @param amount
     * @param min
     * @param max
     * @return {@code amount} clamped between {@code min} and {@code max}.
     */
    public static <Q extends Quantity> Amount<Q> clamp(Amount<Q> amount, Amount<Q> min, Amount<Q> max) {
	return max(min(amount, max), min);
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
	throw new IllegalArgumentException(timeUnit + " cannot be converted to " + Unit.class.getSimpleName());
    }

    /**
     * Parse an {@link Amount} and convert it to given unit.
     * 
     * @param amountCsq
     *            {@link CharSequence} containing amount
     * @param unit
     * @return Parsed {@code amountString} in given unit.
     */
    public static <Q extends Quantity> Amount<Q> parseAmount(CharSequence amountCsq, Unit<Q> unit) {
	try {
	    return FORMAT.parse(amountCsq).to(unit);
	} catch (StringIndexOutOfBoundsException e) {
	    logger.warning("No unit given. Using default.");
	    String amountString = amountCsq.toString();
	    if (amountString.contains(".")) {
		return Amount.valueOf(Double.parseDouble(amountString), unit);
	    } else {
		return Amount.valueOf(Long.parseLong(amountString), unit);
	    }
	}
    }

    /**
     * Converts given {@code amount} to a unit by multiplying the amount's unit
     * by its value.
     * <p>
     * This is useful for creating specialized units, for example a frequency
     * per simulation step, which might be 20 seconds.
     * 
     * @param amount
     * @return {@code amount} converted to a unit
     */
    public static <Q extends Quantity> Unit<Q> convertToUnit(Amount<Q> amount) {
	if (amount.isExact()) {
	    // if value is 1 we can just return the unit
	    if (amount.getExactValue() == 1) {
		return amount.getUnit();
	    }
	    return amount.getUnit().times(amount.getExactValue());
	}
	return amount.getUnit().times(amount.getEstimatedValue());

    }

    /**
     * {@link Converter} for {@link Amount} to / from XML.
     * 
     * @author mey
     *
     */
    public static class XmlAmountConverter implements SingleValueConverter {

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
	    return type.equals(Amount.class);
	}

	@Override
	public String toString(Object obj) {
	    return FORMAT.format((Amount<?>) obj).toString();
	}

	@Override
	public Object fromString(String str) {
	    return FORMAT.parse(str);
	}

    }

    /**
     * {@link Converter} for {@link Unit} to / from XML.
     * 
     * @author mey
     *
     */
    public static class XmlUnitConverter implements SingleValueConverter {
	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
	    return Unit.class.isAssignableFrom(type);
	}

	@Override
	public String toString(Object obj) {
	    return UNIT_FORMAT.format(obj).toString();
	}

	@Override
	public Object fromString(String str) {
	    try {
		return UNIT_FORMAT.parseObject(str);
	    } catch (ParseException e) {
		throw new XStreamException(e);
	    }
	}
    }

    /**
     * Simple {@link AmountFormat} not showing error with fixed unit parsing via
     * {@link FixedDefaultUnitFormat}.
     * 
     * @author mey
     *
     */
    private static class SimpleAmountFormat extends AmountFormat {
	@Override
	public Appendable format(Amount<?> obj, Appendable dest) throws IOException {
	    if (obj.isExact()) {
		dest.append(String.valueOf(obj.getExactValue()));
	    } else {
		dest.append(String.valueOf(obj.getEstimatedValue()));
	    }
	    dest.append(" ");
	    return UNIT_FORMAT.format(obj.getUnit(), dest);
	}

	// from AmountFormat.PlusMinusError
	@Override
	public Amount<?> parse(CharSequence csq, Cursor cursor) {
	    int start = cursor.getIndex();
	    try {
		cursor.skip('(', csq);
		long value = TypeFormat.parseLong(csq, 10, cursor);
		if (csq.charAt(cursor.getIndex()) == ' ') { // Exact!
		    cursor.skip(' ', csq);
		    Unit<?> unit = UNIT_FORMAT.parseProductUnit(csq, cursor);
		    return Amount.valueOf(value, unit);
		}
		cursor.setIndex(start);
		double amount = TypeFormat.parseDouble(csq, cursor);
		cursor.skip(' ', csq);
		double error = 0;
		if (csq.charAt(cursor.getIndex()) == '±') { // Error specified.
		    cursor.skip('±', csq);
		    cursor.skip(' ', csq);
		    error = TypeFormat.parseDouble(csq, cursor);
		}
		cursor.skip(')', csq);
		cursor.skip(' ', csq);
		Unit<?> unit = UNIT_FORMAT.parseProductUnit(csq, cursor);
		return Amount.valueOf(amount, error, unit);
	    } catch (ParseException e) {
		cursor.setIndex(start);
		cursor.setErrorIndex(e.getErrorOffset());
		return null;
	    }
	}

    }
}
