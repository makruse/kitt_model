package sim.engine.output.writing;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Logger;

import sim.engine.output.*;
import sim.engine.output.message.AfterMessage;

/**
 * Decorator class writing data line by line in CSV format from a
 * {@link Collector}.
 * 
 * @author mey
 * @param <T>
 *            the type of the contained {@link Collectable}
 *
 */
class LineWritingCollector<T extends Collectable<?>> extends AbstractWritingCollector<T> implements Closeable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LineWritingCollector.class.getName());

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
    public LineWritingCollector(Collector<T> collector, Path outputPath) {
	this(collector, createWriter(outputPath));
	writeHeaders(writer);
    }

    private static CsvWriter createWriter(Path outputPath) {
	try {
	    return new CsvWriter(outputPath);
	} catch (IOException e) {
	    throw new RuntimeException("Unable to write to file. Exception thrown during creation.", e);
	}
    }

    LineWritingCollector(Collector<T> collector, CsvWriter writer) {
	super(collector);
	this.writer = writer;
    }

    @Override
    protected void writeValues(AfterMessage message) throws IOException {
        writer.writeValues(getCollectable().obtainValues(), message.getSteps());
    }

    @Override
    public void close() throws IOException {
	writer.close();
    }
}
