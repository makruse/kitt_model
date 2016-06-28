package sim.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.TemporalAccessor;

/**
 * {@link Valuable} for displaying a duration between two temporal objects.
 * 
 * @author mey
 *
 */
public class TemporalValuable implements Valuable {
    /**
     * {@link LocalDateTime} the duration represented by this valuable starts
     * at.
     */
    private final LocalDateTime from;
    /**
     * {@link LocalDateTime} the duration represented by this valuable ends at.
     */
    private final LocalDateTime to;

    /**
     * Constructs a new {@link TemporalValuable} representing the duration
     * between from and to.
     * 
     * @param from
     *            the temporal object the duration starts at
     * @param to
     *            the temporal object the duration ends at
     */
    public TemporalValuable(TemporalAccessor from, TemporalAccessor to) {
        super();
        this.from = LocalDateTime.from(from);
        this.to = LocalDateTime.from(to);
    }

    /** Seconds of the duration between from and to temporals. */
    @Override
    public double doubleValue() {
        return Duration.between(from, to).getSeconds();
    }

    /** Complete duration representation of ISO-8601: PnYnMnDTnHnMnS. */
    @Override
    public String toString() {
        // subtract the time component of from as duration
        LocalDateTime offsetTime = to.minus(Duration.between(LocalTime.MIDNIGHT, from.toLocalTime()));

        // use date component of remaining part as period...
        Period period = Period.between(from.toLocalDate(), offsetTime.toLocalDate());
        // ... and time component as duration
        Duration duration = Duration.between(LocalTime.MIDNIGHT, offsetTime.toLocalTime());

        // if less than one day just return the duration
        if (period.equals(Period.ZERO)) {
            return duration.toString();
        }
        return period + duration.toString().replaceFirst("P", "");
    }

}
