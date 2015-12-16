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
 * @author mey
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
     * @param file
     *            for writing data to
     * @throws IOException
     *             if an I/O error occurs opening or creating the file
     */
    public CsvWriter(File file) throws IOException {
	this.file = file;
	writer = Files.newBufferedWriter(file.toPath(), CHARSET);
    }

    /**
     * Append headers to top of file.
     * 
     * @see #writeData(Collection, long)
     * @param headers
     *            size of collection should match that of {@code data} written
     *            later
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
     * Write data to output file.
     * 
     * @see #writeHeaders(Collection)
     * @param data
     *            size of Collection should match that of {@code headers}
     * @param steps
     *            current number for steps column, unused if
     *            {@link #stepsWriting} turned off
     * @throws IOException
     *             If an I/O error occurs
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

    /**
     * Checks type of object to append and formats it according to its type.
     * 
     * @param obj
     *            the object to append
     * @throws IOException
     *             If an I/O error occurs
     */
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
     *             If an I/O error occurs
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
     *             If an I/O error occurs
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
     *             If an I/O error occurs
     */
    private void appendPercent(double percent) throws IOException {
	String percentString = NumberFormat.getPercentInstance(LOCALE).format(percent);

	// remove the % if necessary
	if (!PERCENT_CHARACTER_OUTPUT) {
	    percentString = percentString.substring(0, percentString.length() - 1);
	}

	append(percentString);
    }

    /**
     * Appends data to end of file followed by a separator character.
     * 
     * @param str
     *            data content
     * @throws IOException
     *             If an I/O error occurs
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
     *             If an I/O error occurs
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
     *             If an I/O error occurs
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
     *             If an I/O error occurs
     * @throws ClassNotFoundException
     *             if the class of a serialized object could not be found.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();
	writer = Files.newBufferedWriter(file.toPath(), CHARSET);
    }

    /**
     * Use this interface to mark numbers being written as percentages (1 =
     * 100%).
     * 
     * @author mey
     * 
     */
    public static interface PercentWrapper {
	Double getNumber();
    }
}
