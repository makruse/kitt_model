package sim.engine.output;

import java.io.Serializable;
import java.util.Collection;

/**
 * An aggregation of data, which resembles a row in a CSV file. Data changes
 * with the simulation running. While the data changes, the headers are not. The
 * size of the {@code Collectable} specifies the number of entries within each
 * row, i.e. the size of the returned collections from {@link #obtainData()} and
 * {@link #obtainHeaders()} match.
 * 
 * @author mey
 * 
 */
public interface Collectable extends Serializable {
    /**
     * Obtains headers. Equal in size to {@link #getSize()}.
     * 
     * @return names of column headers
     */
    Collection<String> obtainHeaders();

    /**
     * @return writable data, equal in size and iteration order to collection
     *         from {@link #obtainHeaders()}
     */
    Collection<?> obtainData();

    /** Clears collected data. */
    void clear();

    /**
     * Number of entries for this {@code Collectable}. Equal to the size of
     * {@code Collection}s returned from {@link #obtainHeaders()} and
     * {@link #obtainHeaders()}.
     * 
     * @return number of entries
     */
    int getSize();
}