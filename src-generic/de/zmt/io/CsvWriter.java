package de.zmt.io;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

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
    public static final Locale LOCALE = Locale.US;
    public static final Charset CHARSET = StandardCharsets.US_ASCII;
    public static final String FILENAME_SUFFIX = ".csv";

    private static final boolean PERCENT_CHARACTER_OUTPUT = false;

    /** Character separating fields in file. */
    private static final String sep = "\t";

    /** {@link CsvWritable} to get the data from */
    private final CsvWritable writable;
    /** File is saved to restore writer when deserializing */
    private final File file;
    private transient BufferedWriter writer;

    /**
     * Creates writer outputting to {@code file}.
     * 
     * @param writable
     *            {@link CsvWritable} to get the data from
     * @param file
     *            for writing data to
     * @throws IOException
     */
    public CsvWriter(CsvWritable writable, File file) throws IOException {
	this.writable = writable;
	this.file = file;
	writer = Files.newBufferedWriter(file.toPath(), CHARSET);
	writeHeaders();
    }

    /**
     * Append headers from associated {@link CsvWritable} to top of file.
     * 
     * @throws IOException
     */
    protected void writeHeaders() throws IOException {
	// append the header to the empty file
	for (String header : writable.obtainHeaders()) {
	    append(header);
	}
	newLine();
    }

    /**
     * Dump data from associated {@link CsvWritable}.
     * 
     * @throws IOException
     */
    public void writeData() throws IOException {
	for (Object data : writable.obtainData()) {
	    append(data);
	}
	newLine();
    }

    protected void append(Object obj) throws IOException {
	if (obj instanceof Long || obj instanceof Integer) {
	    appendInteger(((Number) obj).longValue());
	} else if (obj instanceof Double || obj instanceof Float) {
	    appendNumber(((Number) obj).doubleValue());
	} else {
	    append(obj.toString());
	}
	// TODO percent
	// TODO method overloading?
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
}
