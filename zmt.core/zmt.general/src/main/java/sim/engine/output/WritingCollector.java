package sim.engine.output;

import java.io.*;
import java.util.Collection;
import java.util.logging.*;

import de.zmt.io.CsvWriter;

/**
 * Decorator class writing data in CSV format from a {@link Collector}.
 * 
 * @author mey
 *
 */
public class WritingCollector implements Collector, Closeable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(WritingCollector.class.getName());

    private final CsvWriter writer;
    private final Collector collector;

    /**
     * Constructs a new {@code WritingCollector}. Headers are written
     * immediately after opening the file.
     * 
     * @param collector
     *            the collector to write data from
     * @param outputFile
     *            the file data is written to
     */
    public WritingCollector(Collector collector, File outputFile) {
	super();
	this.collector = collector;

	try {
	    writer = new CsvWriter(outputFile);
	} catch (IOException e) {
	    throw new RuntimeException("Unable to write to file. Exception thrown during creation.", e);
	}

	try {
	    writer.writeHeaders(obtainHeaders());
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception while writing headers: " + obtainHeaders() + ".", e);
	}
    }

    @Override
    public void beforeCollect(BeforeMessage message) {
	collector.beforeCollect(message);
    }

    @Override
    public Collection<String> obtainHeaders() {
	return collector.obtainHeaders();
    }

    @Override
    public void collect(CollectMessage message) {
	collector.collect(message);
    }

    /** Writes data after collecting it. */
    @Override
    public void afterCollect(AfterMessage message) {
	collector.afterCollect(message);

	Collection<?> data = obtainData();
	try {
	    writer.writeData(data, message.getSteps());
	} catch (IOException e) {
	    logger.log(Level.WARNING, "I/O error while writing data from " + collector, e);
	}
    }

    @Override
    public Collection<?> obtainData() {
	return collector.obtainData();
    }

    @Override
    public void clear() {
	collector.clear();
    }

    @Override
    public int getColumnCount() {
	return collector.getColumnCount();
    }

    @Override
    public void close() throws IOException {
	try {
	    writer.close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception when closing writer!", e);
	}
    }
}
