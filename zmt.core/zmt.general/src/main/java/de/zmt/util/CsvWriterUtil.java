package de.zmt.util;

import java.io.*;
import java.util.Arrays;

import de.zmt.io.CsvWriter;

public final class CsvWriterUtil {
    private static final int DIGITS_COUNT = 5;
    private static final String FILENAME_SUFFIX = ".csv";

    private CsvWriterUtil() {

    }

    /**
     * Finds next index for files starting with {@code prefixBeforeIndex} to be
     * used in output.
     * 
     * @param directory
     * @param prefixBeforeIndex
     * @return index after the last already present in {@code directory}.
     */
    public static int findNextIndex(File directory, final String prefixBeforeIndex) {
	// get list of files from former simulation runs
	File[] files = directory.listFiles(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		if (name.startsWith(prefixBeforeIndex)) {
		    return true;
		}
		return false;
	    }
	});

	// no other files present, first index is 0
	if (files.length <= 0) {
	    return 0;
	}

	// get last existing index from file list
	Arrays.sort(files);
	String lastFileName = files[files.length - 1].getName();
	// extract index from last file in list
	int lastIndex = Integer.parseInt(
		lastFileName.substring(prefixBeforeIndex.length(), prefixBeforeIndex.length() + DIGITS_COUNT));

	return lastIndex + 1;
    }

    /**
     * @param directory
     * @param prefixBeforeIndex
     * @param index
     * @param prefixAfterIndex
     * @return {@link File} for {@link CsvWriter}
     */
    public static File generateWriterFile(File directory, String prefixBeforeIndex, int index,
	    String prefixAfterIndex) {
	return new File(directory, prefixBeforeIndex
		// next integer with leading zeroes
		+ String.format("%0" + DIGITS_COUNT + "d", index) + prefixAfterIndex + CsvWriterUtil.FILENAME_SUFFIX);
    }

}
