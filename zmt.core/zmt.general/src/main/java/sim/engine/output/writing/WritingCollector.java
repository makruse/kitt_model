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
    /** @return the wrapped collector */
    Collector<T> getWrappedCollector();

}