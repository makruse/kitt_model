package sim.engine.output;

import java.io.Serializable;
import java.util.Collection;

/**
 * An aggregation of data, which is organized in columns. Each column has an
 * associated header. The data is collected by a {@link Collector} and organized
 * in columns, each of them with a title header.
 * 
 * @author mey
 * 
 */
public interface Collectable extends Serializable {
    /**
     * Obtains headers. Equal in size to {@link #getColumnCount()}.
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
     * Number of columns for this {@code Collectable}. Equal to the size of
     * {@code Collection}s returned from {@link #obtainHeaders()} and
     * {@link #obtainHeaders()}.
     * 
     * @return number of columns
     */
    int getColumnCount();
}