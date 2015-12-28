package sim.engine.output;

import sim.engine.output.message.*;
import sim.util.Proxiable;

/**
 * Simple abstract {@link Collector} implementation storing one
 * {@link Collectable}, which is also used as properties proxy for inspection if
 * viewed in GUI.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of {@code Collectable} stored
 */
public abstract class AbstractCollector<T extends Collectable> implements Collector, Proxiable {
    private static final long serialVersionUID = 1L;

    private final T collectable;

    /**
     * Implementing classes invoke this constructor to construct a
     * {@code Collector} with the given {@code Collectable}.
     * 
     * @param collectable
     *            the {@link Collectable} used
     */
    public AbstractCollector(T collectable) {
	super();
	this.collectable = collectable;
    }

    @Override
    public void beforeCollect(BeforeMessage message) {
    }

    @Override
    public void afterCollect(AfterMessage message) {
    }

    @Override
    public T getCollectable() {
	return collectable;
    }

    @Override
    public Object propertiesProxy() {
	return collectable;
    }

    @Override
    public String toString() {
	return "[" + collectable + "]";
    }
}
