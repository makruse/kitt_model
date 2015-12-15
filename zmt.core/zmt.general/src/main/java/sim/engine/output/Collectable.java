package sim.engine.output;

import java.io.Serializable;
import java.util.Collection;

/**
 * An aggregation of collected data, which is organized in columns. Each column
 * has an associated header. The data is collected by a {@link Collector} and
 * can be written into a CSV file.
 * 
 * @author mey
 * 
 */
public interface Collectable extends Serializable {
    /**
     * Obtain headers. Will be called when creating the writer.
     * 
     * @return names of headers in CSV file for this class
     */
    Collection<String> obtainHeaders();

    /**
     * @return writable data, must be equal in length and iteration order to
     *         collection from {@link #obtainHeaders()}
     */
    Collection<?> obtainData();

    /** Clear collected data. */
    void clear();
}