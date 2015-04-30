package de.zmt.io;

import java.io.*;
import java.util.Collection;

/**
 * {@link CsvWriter} writing step number as first column.
 * 
 * @author cmeyer
 * 
 */
public class SteppedCsvWriter extends CsvWriter {
    private static final long serialVersionUID = 1L;

    private static final String STEPS_COLUMN_HEADER = "steps";

    public SteppedCsvWriter(File file) throws IOException {
	super(file);
    }

    @Override
    public void writeHeaders(Collection<String> headers) throws IOException {
	append(STEPS_COLUMN_HEADER);
	super.writeHeaders(headers);
    }

    @Override
    public void writeData(Collection<?> data) throws IOException {
	throw new UnsupportedOperationException("Provide number of steps.");
    }

    public void writeData(Collection<?> data, long steps) throws IOException {
	append(String.valueOf(steps));
	super.writeData(data);
    }
}
