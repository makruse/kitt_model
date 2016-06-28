package de.zmt.output.writing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zmt.output.Collector;

abstract class AbstractCollectorWriter implements CollectorWriter {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AbstractCollectorWriter.class.getName());

    private final Collector<?> collector;

    public AbstractCollectorWriter(Collector<?> collector) {
        super();
        this.collector = collector;
    }

    /**
     * Write headers from collectable using the writer.
     * 
     * @param writer
     *            the writer to write the headers
     */
    protected final void writeHeaders(CsvWriter writer) {
        Iterable<String> headers = collector.getCollectable().obtainHeaders();
        try {
            writer.writeHeaders(headers);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception while writing headers: " + headers + ".", e);
        }
    }

    @Override
    public Collector<?> getCollector() {
        return collector;
    }

    @Override
    public String toString() {
        return collector.toString();
    }
}