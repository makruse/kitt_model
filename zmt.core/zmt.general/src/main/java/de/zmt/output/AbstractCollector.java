package de.zmt.output;

import de.zmt.output.message.AfterMessage;
import de.zmt.output.message.BeforeMessage;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Simple abstract {@link Collector} implementation storing one
 * {@link Collectable}, which is also used as properties proxy for inspection if
 * viewed in GUI.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of the contained {@link Collectable}
 */
public abstract class AbstractCollector<T extends Collectable<?>> implements Collector<T>, ProvidesInspector {
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
    public Inspector provideInspector(GUIState state, String name) {
        return Inspector.getInspector(collectable, state, name);
    }

    @Override
    public String toString() {
        return "[" + collectable + "]";
    }
}
