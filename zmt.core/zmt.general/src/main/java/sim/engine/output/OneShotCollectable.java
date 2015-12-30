package sim.engine.output;

/**
 * A {@link Collectable} that contains data for a whole file, which is to be
 * written to a new file after each collection.
 * 
 * @author mey
 *
 */
public interface OneShotCollectable extends Collectable {
    /**
     * Values contained in this {@code Collectable}. Each inner
     * {@code Collection} represents a column and is equal in size to
     * {@link #getColumnSize()}. The outer collection is equal in size to
     * {@link #getSize()}.
     * 
     * @return writable values
     */
    @Override
    Iterable<? extends Iterable<?>> obtainValues();

    /**
     * @see #obtainValues()
     * @return size of a column
     */
    int getColumnSize();
}
