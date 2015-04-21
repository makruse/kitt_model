package de.zmt.io;

import java.util.Collection;

/**
 * Implement this interface to make it writable by the {@link CsvWriter}.
 * 
 * @author cmeyer
 * 
 */
public interface CsvWritable {
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
}
