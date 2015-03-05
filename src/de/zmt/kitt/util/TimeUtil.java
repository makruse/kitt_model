package de.zmt.kitt.util;

import static org.joda.time.DateTimeConstants.*;

import org.joda.time.Duration;

/**
 * Utility functions for converting time from and to milliseconds. Results for
 * months and years are averages.
 * <p>
 * In contrast to {@link Duration}'s own toStandard... methods, double values
 * are returned here that include fractions.
 * 
 * @author cmeyer
 * 
 */
@Deprecated
// use jscience Amount<Duration>
public class TimeUtil {
    private static final double MILLIS_PER_MONTH = MILLIS_PER_DAY * 29.53;
    private static final double MILLIS_PER_YEAR = MILLIS_PER_DAY * 365.25;

    private static final double TO_SECONDS = 1d / MILLIS_PER_SECOND;
    private static final double TO_MINUTES = 1d / MILLIS_PER_MINUTE;
    private static final double TO_HOURS = 1d / MILLIS_PER_HOUR;
    private static final double TO_DAYS = 1d / MILLIS_PER_DAY;
    private static final double TO_WEEKS = 1d / MILLIS_PER_WEEK;
    private static final double TO_MONTHS = 1d / MILLIS_PER_MONTH;
    private static final double TO_YEARS = 1d / MILLIS_PER_YEAR;

    public static Duration fromSeconds(double seconds) {
	return new Duration(Math.round(seconds * MILLIS_PER_SECOND));
    }

    public static Duration fromSeconds(long seconds) {
	return new Duration(seconds * MILLIS_PER_SECOND);
    }

    public static Duration fromMinutes(double minutes) {
	return new Duration(Math.round(minutes * MILLIS_PER_MINUTE));
    }

    public static Duration fromMinutes(long minutes) {
	return new Duration(minutes * MILLIS_PER_MINUTE);
    }

    public static Duration fromHours(double hours) {
	return new Duration(Math.round(hours * MILLIS_PER_HOUR));
    }

    public static Duration fromHours(long hours) {
	return new Duration(hours * MILLIS_PER_HOUR);
    }

    public static Duration fromDays(double days) {
	return new Duration(Math.round(days * MILLIS_PER_DAY));
    }

    public static Duration fromDays(long days) {
	return new Duration(days * MILLIS_PER_DAY);
    }

    public static Duration fromWeeks(double weeks) {
	return new Duration(Math.round(weeks * MILLIS_PER_WEEK));
    }

    public static Duration fromWeeks(long weeks) {
	return new Duration(weeks * MILLIS_PER_WEEK);
    }

    public static Duration fromMonths(double months) {
	return new Duration(Math.round(months * MILLIS_PER_MONTH));
    }

    public static Duration fromMonths(long months) {
	return new Duration(Math.round(months * MILLIS_PER_MONTH));
    }

    public static Duration fromYears(double years) {
	return new Duration(Math.round(years * MILLIS_PER_YEAR));
    }

    public static Duration fromYears(long years) {
	return new Duration(Math.round(years * MILLIS_PER_YEAR));
    }

    public static double toSeconds(Duration duration) {
	return duration.getMillis() * TO_SECONDS;
    }

    public static double toMinutes(Duration duration) {
	return duration.getMillis() * TO_MINUTES;
    }

    public static double toHours(Duration duration) {
	return duration.getMillis() * TO_HOURS;
    }

    public static double toDays(Duration duration) {
	return duration.getMillis() * TO_DAYS;
    }

    public static double toWeeks(Duration duration) {
	return duration.getMillis() * TO_WEEKS;
    }

    public static double toMonths(Duration duration) {
	return duration.getMillis() * TO_MONTHS;
    }

    public static double toYears(Duration duration) {
	return duration.getMillis() * TO_YEARS;
    }
}
