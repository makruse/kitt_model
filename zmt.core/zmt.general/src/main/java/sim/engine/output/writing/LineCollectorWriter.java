package sim.engine.output.writing;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Logger;

import sim.engine.output.Collector;

/**
 * Decorator class writing data line by line in CSV format from a
 * {@link Collector}.
 * 
 * @author mey
 *
 */
class LineCollectorWriter extends AbstractCollectorWriter implements Closeable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LineCollectorWriter.class.getName());

    private final CsvWriter writer;

    /**
     * Constructs a new {@code WritingCollector}. Headers are written
     * immediately after opening the file.
     * 
     * @param collector
     *            the collector to write data from
     * @param outputPath
     *            the file data is written to
     */
    public LineCollectorWriter(Collector<?> collector, Path outputPath) {
	super(collector);
	this.writer = createWriter(outputPath);
	writeHeaders(writer);
    }

    private static CsvWriter createWriter(Path outputPath) {
	try {
	    return new CsvWriter(outputPath);
	} catch (IOException e) {
	    throw new RuntimeException("Unable to write to file. Exception thrown during creation.", e);
	}
    }

    @Override
    public void writeValues(long steps) throws IOException {
	writer.writeValues(getCollector().getCollectable().obtainValues(), steps);
    }

    @Override
    public void close() throws IOException {
	writer.close();
    }
}
