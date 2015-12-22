package sim.engine.output;

/**
 * A {@link Collectable} that can be cleared, i.e. the contained data removed or
 * reset to default values.
 * 
 * @author mey
 *
 */
public interface ClearableCollectable extends Collectable {
    /** Clears the data contained in this collectable. */
    void clear();
}
