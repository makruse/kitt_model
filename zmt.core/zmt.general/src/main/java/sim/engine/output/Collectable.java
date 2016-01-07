package sim.engine.output;

import java.io.Serializable;

/**
 * An aggregation of data, which resembles a row in a CSV file. Values change
 * with the simulation running. While the values change, the headers are not.
 * The size of the {@code Collectable} specifies the number of entries within
 * each row, i.e. the size of the returned iterables from
 * {@link #obtainValues()} and {@link #obtainHeaders()} match.
 * 
 * @author mey
 * @param <V>
 *            the type of contained values
 * 
 */
public interface Collectable<V> extends Serializable {
    /**
     * Obtains headers. Equal in size to {@link #getSize()}.
     * 
     * @return names of column headers
     */
    Iterable<String> obtainHeaders();

    /**
     * @return values that can be written as a row into a CSV file, equal in
     *         size and iteration order {@link #obtainHeaders()}
     */
    Iterable<? extends V> obtainValues();

    /**
     * Number of entries for this {@code Collectable}. Equal to the size of
     * {@link #obtainHeaders()} and {@link #obtainValues()}.
     * 
     * @return number of entries
     */
    int getSize();
}