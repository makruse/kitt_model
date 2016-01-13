package sim.engine.output.writing;

import sim.engine.output.*;

/**
 * A {@link Collector} wrapping around another collector which data is written.
 * 
 * @author mey
 * @param <T>
 *            the type of the contained {@link Collectable}
 *
 */
public interface WritingCollector<T extends Collectable<?>> extends Collector<Collectable<?>> {
    /** Number of digits used in file names for numbers. */
    public static int DIGITS_COUNT = 5;

    /** @return the wrapped collector */
    Collector<T> getWrappedCollector();

}