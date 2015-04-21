package de.zmt.io;

import java.io.*;

/**
 * {@link CsvWriter} writing step number as first column.
 * 
 * @author cmeyer
 * 
 */
public class SteppedCsvWriter extends CsvWriter {
    private static final long serialVersionUID = 1L;

    private static final String STEPS_COLUMN_HEADER = "steps";

    public SteppedCsvWriter(CsvWritable writable, File file) throws IOException {
	super(writable, file);
    }

    @Override
    public void writeData() throws IOException {
	throw new UnsupportedOperationException("Provide number of steps.");
    }

    public void writeData(long steps) throws IOException {
	append(String.valueOf(steps));
	super.writeData();
    }

    @Override
    protected void writeHeaders() throws IOException {
	append(STEPS_COLUMN_HEADER);
	super.writeHeaders();
    }
}
