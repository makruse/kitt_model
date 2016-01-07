package sim.engine.output.writing;

import java.io.IOException;
import java.util.logging.*;

import sim.engine.output.*;
import sim.engine.output.message.*;

abstract class AbstractWritingCollector<T extends Collectable<?>> implements WritingCollector<T> {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AbstractWritingCollector.class.getName());

    private final Collector<T> collector;

    public AbstractWritingCollector(Collector<T> collector) {
	super();
	this.collector = collector;
    }

    /**
     * Write headers from collectable using the writer.
     * 
     * @see #getWriter()
     */
    protected final void writeHeaders() {
	Iterable<String> headers = getCollectable().obtainHeaders();
	try {
	    getWriter().writeHeaders(headers);
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception while writing headers: " + headers + ".", e);
	}
    }

    protected final void closeWriter() {
	try {
	    getWriter().close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception when closing writer!", e);
	}
    }

    @Override
    public final Collector<T> getWrappedCollector() {
	return collector;
    }

    /** @return {@link CsvWriter} to be used with this writing collector */
    protected abstract CsvWriter getWriter();

    /**
     * Subclasses need to specify how to write values from the collectable.
     * 
     * @param message
     * @throws IOException
     */
    protected abstract void writeValues(AfterMessage message) throws IOException;

    @Override
    public void beforeCollect(BeforeMessage message) {
	collector.beforeCollect(message);
    }

    @Override
    public void collect(CollectMessage message) {
	collector.collect(message);
    }

    /** Writes data after collecting it. */
    @Override
    public void afterCollect(AfterMessage message) {
	collector.afterCollect(message);

	try {
	    writeValues(message);
	} catch (IOException e) {
	    logger.log(Level.WARNING, "I/O error while writing data from " + getWrappedCollector(), e);
	}
    }

    @Override
    public T getCollectable() {
	return collector.getCollectable();
    }

    @Override
    public String toString() {
	return collector.toString();
    }
}