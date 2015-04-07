package de.zmt.kitt.util;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.*;
import javax.measure.unit.Unit;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jscience.physics.amount.*;

/**
 * General utility methods for dealing with jScience {@link Amount}s.
 * 
 * @author cmeyer
 * 
 */
public class AmountUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AmountUtil.class
	    .getName());

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

    /** @return {@code amount} clamped between {@code min} and {@code max}. */
    public static <Q extends Quantity> Amount<Q> clamp(Amount<Q> amount,
	    Amount<Q> min, Amount<Q> max) {
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
	    CharSequence amountCsq, Unit<Q> unit) {
	try {
	    return FORMAT_IN.parse(amountCsq).to(unit);
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
     * {@link XmlAdapter} for (un)marshalling jScience {@link Amount}s.
     * 
     * @author cmeyer
     * 
     */
    public static class XmlAmountAdapter extends XmlAdapter<String, Amount<?>> {

	@Override
	public Amount<?> unmarshal(String v) throws Exception {
	    return FORMAT_IN.parse(v);
	}

	@Override
	public String marshal(Amount<?> v) throws Exception {
	    return FORMAT.format(v).toString();
	}
    }
}
