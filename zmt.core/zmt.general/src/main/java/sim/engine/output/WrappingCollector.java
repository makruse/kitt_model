package sim.engine.output;

/**
 * A {@link Collector} wrapping around another collector.
 * 
 * @author mey
 *
 */
public interface WrappingCollector extends Collector {

    /** @return the wrapped collector */
    Collector getCollector();

}