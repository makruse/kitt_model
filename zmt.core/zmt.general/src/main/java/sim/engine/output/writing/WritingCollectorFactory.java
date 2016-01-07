package sim.engine.output.writing;

import java.io.*;
import java.util.Arrays;

import sim.engine.output.*;

/**
 * Factory class for creating {@link WritingCollector} instances and generating
 * file names for output.
 * 
 * @author mey
 *
 */
public final class WritingCollectorFactory {
    /** Number of digits used in file names for numbers. */
    private static final int DIGITS_COUNT = 5;

    private WritingCollectorFactory() {

    }

    /**
     * Wraps given collector in a fitting {@link WritingCollector}.
     * 
     * @param collector
     *            the collector which data needs to be written
     * @param outputFile
     *            the output file, if several are needed the given one acts a
     *            base
     * @return a {@link WritingCollector} around given collector
     */
    public static <T extends Collectable<?>> WritingCollector<T> wrap(Collector<T> collector, File outputFile) {
	if (collector.getCollectable() instanceof OneShotCollectable) {
	    return new OneShotWritingCollector<>(collector, outputFile);
	}
	return new LineWritingCollector<>(collector, outputFile);
    }

    /**
     * Wraps given collector in a fitting {@link WritingCollector}.
     * 
     * @param collector
     *            the collector which data needs to be written
     * @param directory
     *            the directory where the file resides
     * @param prefixBeforeIndex
     *            part of the file name prefix before the index
     * @param index
     *            the index number to be used in the file name
     * @param prefixAfterIndex
     *            part of the file name prefix after the index
     * @return a {@link WritingCollector} around given collector
     */
    public static <T extends Collectable<?>> WritingCollector<T> wrap(Collector<T> collector, File directory,
	    String prefixBeforeIndex, int index, String prefixAfterIndex) {
	return wrap(collector, generateOutputFile(directory, prefixBeforeIndex, index, prefixAfterIndex));
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
     * Generates an output file to be used with a {@link WritingCollector}.
     * 
     * @param directory
     *            the directory where the file resides
     * @param prefixBeforeIndex
     *            part of the file name prefix before the index
     * @param index
     *            the index number to be used in the file name
     * @param prefixAfterIndex
     *            part of the file name prefix after the index
     * @return generated output file
     */
    static File generateOutputFile(File directory, String prefixBeforeIndex, int index, String prefixAfterIndex) {
	if (!directory.isDirectory()) {
	    throw new IllegalArgumentException(directory + " must be a directory.");
	}
	return new File(directory,
		prefixBeforeIndex
			// next integer with leading zeroes
			+ String.format("%0" + DIGITS_COUNT + "d", index) + prefixAfterIndex);
    }
}
