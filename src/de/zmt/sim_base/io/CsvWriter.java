package de.zmt.sim_base.io;

import java.io.*;

/**
 * provides the functions to serialize data to a comma separated file
 * 
 * @author oth
 */
public class CsvWriter {

    /** Buffered writer instance to write to */
    private final BufferedWriter output;
    public static final String FILENAME_SUFFIX = ".csv";
    /** defines the seperator for each field in the line */
    private static final String sep = "\t";

    /**
     * @param path
     *            defines the path for the file to be generated
     * @throws FileNotFoundException
     */
    public CsvWriter(String path) throws FileNotFoundException {
	FileOutputStream file = new FileOutputStream(path);
	output = new BufferedWriter(new OutputStreamWriter(file));
    }

    /**
     * @param str
     *            the string to write in the next field of csv file appends one
     *            seperated element to the file
     * @throws IOException
     */
    public void append(String str) throws IOException {
	output.write(str + sep);
	output.flush();
    }

    /**
     * create a new line in the file
     * 
     * @throws IOException
     */
    public void newLine() throws IOException {
	output.newLine();
	output.flush();
    }

    /**
     * close the file, no more write access is possible
     * 
     * @throws IOException
     */
    public void close() throws IOException {
	if (output == null)
	    return;

	output.flush();
	output.close();
    }
}
