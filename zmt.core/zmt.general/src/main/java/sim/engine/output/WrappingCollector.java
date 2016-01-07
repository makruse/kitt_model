package sim.engine.output;

/**
 * A {@link Collector} wrapping around another collector.
 * 
 * @author mey
 * @param <T>
 *            the type of the contained {@link Collectable}
 *
 */
public interface WrappingCollector<T extends Collectable<?>> extends Collector<Collectable<?>> {

    /** @return the wrapped collector */
    Collector<T> getCollector();

}