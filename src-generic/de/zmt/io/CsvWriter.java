package de.zmt.io;

import java.io.*;

/**
 * Provides functions to serialize data to a comma separated file (CSV),
 * although tabs are used instead of commas.
 * 
 * @author oth
 */
public class CsvWriter {

    /** Buffered writer instance to write to. */
    private final BufferedWriter output;
    public static final String FILENAME_SUFFIX = ".csv";
    /** Character separating fields in file. */
    private static final String sep = "\t";

    /**
     * @param path
     *            file path
     * @throws FileNotFoundException
     */
    public CsvWriter(String path) throws FileNotFoundException {
	FileOutputStream file = new FileOutputStream(path);
	output = new BufferedWriter(new OutputStreamWriter(file));
    }

    /**
     * Appends data to end of file followed by a separator character.
     * 
     * @param str
     *            data content
     * @throws IOException
     */
    public void append(String str) throws IOException {
	output.write(str + sep);
	output.flush();
    }

    /**
     * Write line separator.
     * 
     * @see BufferedWriter#newLine()
     * @throws IOException
     */
    public void newLine() throws IOException {
	output.newLine();
	output.flush();
    }

    /**
     * Close file, preventing any further write access.
     * 
     * @see BufferedWriter#close()
     * @throws IOException
     */
    public void close() throws IOException {
	output.close();
    }
}
