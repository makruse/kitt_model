package de.zmt.io;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.*;

/**
 * Provides functions to serialize data to a comma separated value (CSV) file,
 * although tabs are used instead of commas.
 * 
 * @author oth
 * @author cmeyer
 */
public class CsvWriter implements Serializable, Closeable {
    private static final long serialVersionUID = 1L;

    /** Locale used for formatting numbers */
    private static final Locale LOCALE = Locale.US;
    private static final Charset CHARSET = StandardCharsets.US_ASCII;
    private static final boolean PERCENT_CHARACTER_OUTPUT = false;

    /** Character separating fields in file. */
    private static final String sep = "\t";

    /** Header for the steps column */
    private static final String STEPS_COLUMN_HEADER = "steps";

    /** File is saved to restore writer when deserializing */
    private final File file;
    private transient BufferedWriter writer;

    /** A step column is written if true */
    private boolean stepsWriting = true;

    /**
     * Creates writer outputting to {@code file}.
     * 
     * @param writable
     *            {@link CsvWritable} to get the data from
     * @param file
     *            for writing data to
     * @throws IOException
     */
    public CsvWriter(File file) throws IOException {
	this.file = file;
	writer = Files.newBufferedWriter(file.toPath(), CHARSET);
    }

    /**
     * Append headers from associated {@link CsvWritable} to top of file.
     * 
     * @throws IOException
     */
    public void writeHeaders(Collection<String> headers) throws IOException {
	if (stepsWriting) {
	    append(STEPS_COLUMN_HEADER);
	}

	for (String header : headers) {
	    append(header);
	}
	newLine();
    }

    /**
     * Dump data from associated {@link CsvWritable}.
     * 
     * @param data
     * @param steps
     *            current number for steps column, unused if
     *            {@link #stepsWriting} turned off
     * @throws IOException
     */
    public void writeData(Collection<?> data, long steps) throws IOException {
	if (stepsWriting) {
	    append(String.valueOf(steps));
	}
	for (Object obj : data) {
	    append(obj);
	}
	newLine();
    }

    protected void append(Object obj) throws IOException {
	if (obj instanceof Long || obj instanceof Integer) {
	    appendInteger(((Number) obj).longValue());
	} else if (obj instanceof Double || obj instanceof Float) {
	    appendNumber(((Number) obj).doubleValue());
	} else if (obj instanceof PercentWrapper) {
	    appendPercent(((PercentWrapper) obj).getNumber());
	} else {
	    append(obj.toString());
	}
    }

    /**
     * Appends an integer formatted by the current locale.
     * 
     * @see #append(String)
     * @param integer
     * @throws IOException
     */
    private void appendInteger(long integer) throws IOException {
	append(NumberFormat.getIntegerInstance(LOCALE).format(integer));
    }

    /**
     * Appends a real number formatted by the current locale.
     * 
     * @see #append(String)
     * @param number
     * @throws IOException
     */
    private void appendNumber(double number) throws IOException {
	append(NumberFormat.getNumberInstance(LOCALE).format(number));
    }

    /**
     * Appends a fraction as percentage formatted by the current locale.
     * 
     * @see #append(String)
     * @param percent
     * @throws IOException
     */
    private void appendPercent(double percent) throws IOException {
	String percentString = NumberFormat.getPercentInstance(LOCALE).format(
		percent);

	// remove the % if necessary
	if (!PERCENT_CHARACTER_OUTPUT) {
	    percentString = percentString.substring(0,
		    percentString.length() - 1);
	}

	append(percentString);
    }

    /**
     * Appends data to end of file followed by a separator character.
     * 
     * @param str
     *            data content
     * @throws IOException
     */
    private void append(String str) throws IOException {
	writer.write(str + sep);
    }

    /**
     * Write line separator and flush the stream to ensure data has been written
     * to file.
     * 
     * @see BufferedWriter#newLine()
     * @throws IOException
     */
    private void newLine() throws IOException {
	writer.newLine();
	writer.flush();
    }

    public void setStepsWriting(boolean enabled) {
	this.stepsWriting = enabled;
    }

    /**
     * Close output stream, preventing any further write access.
     * 
     * @see BufferedWriter#close()
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
	writer.close();
    }

    /**
     * Restores the writer when deserializing.
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
	    ClassNotFoundException {
	in.defaultReadObject();
	writer = Files.newBufferedWriter(file.toPath(), CHARSET);
    }

    /**
     * Use this interface to mark numbers being written as percentages (1 =
     * 100%).
     * 
     * @author cmeyer
     * 
     */
    public static interface PercentWrapper {
	Double getNumber();
    }
}
