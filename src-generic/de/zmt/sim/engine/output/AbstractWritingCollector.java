package de.zmt.sim.engine.output;

import java.io.*;
import java.util.Collection;
import java.util.logging.*;

import de.zmt.io.CsvWriter;
import de.zmt.sim.engine.params.def.ParamDefinition;

public abstract class AbstractWritingCollector<K extends ParamDefinition, V extends Collectable>
	extends AbstractCollector<K, V> implements Closeable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(AbstractWritingCollector.class.getName());
    private static final long serialVersionUID = 1L;

    protected final CsvWriter writer;

    public AbstractWritingCollector(Collection<? extends K> agentClassDefs,
	    File outputFile) {
	super(agentClassDefs);

	CsvWriter tempWriter;
	try {
	    tempWriter = new CsvWriter(outputFile);
	} catch (IOException e) {
	    logger.log(Level.WARNING,
		    "Unable to write to file. Exception thrown during creation.",
		    e);
	    tempWriter = null;
	}
	this.writer = tempWriter;

	Collection<String> headers = obtainHeaders();
	try {
	    writer.writeHeaders(headers);
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception while writing headers: "
		    + headers + ".", e);
	}
    }

    @Override
    public void afterCollect(AfterMessage message) {
	if (writer == null) {
	    return;
	}

	Collection<?> data = obtainData();
	try {
	    writer.writeData(data, message.getSteps());
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception while writing data: " + data
		    + ".", e);
	}
    }

    @Override
    public void close() throws IOException {
	if (writer == null) {
	    return;
	}

	try {
	    writer.close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Exception when closing writer!", e);
	}
    }
}
