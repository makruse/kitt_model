package sim.engine.output;

/**
 * A {@link Collectable} that can be cleared, i.e. the contained data removed or
 * reset to default values.
 * 
 * @author mey
 * @param <V>
 *            the type of contained values
 *
 */
public interface ClearableCollectable<V> extends Collectable<V> {
    /** Clears the data contained in this collectable. */
    void clear();
}
