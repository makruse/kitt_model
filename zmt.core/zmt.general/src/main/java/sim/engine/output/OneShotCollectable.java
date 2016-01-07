package sim.engine.output;

/**
 * A {@link Collectable} that contains data for a whole file, which is to be
 * written to a new file after each collection.
 * 
 * @author mey
 * @param <V>
 *            the type of contained values
 * @param <T>
 *            the iterable type used for each column
 *
 */
public interface OneShotCollectable<V, T extends Iterable<V>> extends Collectable<T> {
    /**
     * Values contained in this {@code Collectable}. Each inner iterable
     * represents a column and is equal in size to {@link #getColumnSize()}. The
     * outer iterable is equal in size to {@link #getSize()}.
     * 
     * @return writable values
     */
    @Override
    Iterable<T> obtainValues();

    /**
     * @see #obtainValues()
     * @return size of a column
     */
    int getColumnSize();
}
