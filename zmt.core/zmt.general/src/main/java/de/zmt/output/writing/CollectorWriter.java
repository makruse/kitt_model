package de.zmt.output.writing;

import java.io.IOException;
import java.io.Serializable;

import de.zmt.output.Collector;

public interface CollectorWriter extends Serializable {
    /** @return the collector which output is written */
    Collector<?> getCollector();

    /**
     * Implementing classes need to specify how to write values from the
     * associated collectable. Called after the collection process.
     * 
     * @param steps
     * @throws IOException
     *             If an I/O error occurs
     */
    void writeValues(long steps) throws IOException;
}
