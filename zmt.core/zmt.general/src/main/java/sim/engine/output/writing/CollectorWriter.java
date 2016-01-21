package sim.engine.output.writing;

import java.io.*;

import sim.engine.output.Collector;

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
