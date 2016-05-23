package de.zmt.launcher.strategies;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.output.Output;
import sim.engine.SimState;
import sim.engine.ZmtSimState;

class DefaultOutputPathGenerator implements OutputPathGenerator {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultOutputPathGenerator.class.getName());

    /** Classifier for outer component in output path. */
    private static final String CLASSIFIER_OUTER = ZmtSimState.DEFAULT_OUTPUT_DIR.getFileName().toString();
    /**
     * Classifier for inner component in output path, changing with every
     * simulation run.
     */
    private static final String CLASSIFIER_INNER = "run";

    /**
     * Creates paths to output directories: <br>
     * {@code <sim>_output_<mode>[_<batch_index>]/run_<inner_index>}<br>
     * <p>
     * The indices are numbers consisting of five digits starting with the first
     * index not already used before. {@code <run_index>} is incremented with
     * each returned path while {@code <batch_index>} is not and only present in
     * {@link Mode#BATCH}.
     */
    @Override
    public Iterable<Path> createPaths(Class<? extends SimState> simClass, final Mode mode, Path directory) {
	final Path outerPath = generateOuterPath(simClass, mode, directory);

	return new Iterable<Path>() {

	    @Override
	    public Iterator<Path> iterator() {
		return new InnerPathIterator(outerPath, findFirstIndex(mode, outerPath));
	    }
	};
    }

    /**
     * Generates outer path for output directories: <br>
     * {@code <sim>_output_<mode>[_<batch_index>]}
     * 
     * @param simClass
     *            the simulation class to generate an output path for
     * @param mode
     *            the launch mode
     * @param directory
     *            the parent directory for the created directories
     * @return the outer output path
     */
    private static Path generateOuterPath(Class<? extends SimState> simClass, Mode mode, Path directory) {
	String directoryName = Output.generateFileName(processForFileName(simClass), CLASSIFIER_OUTER,
		mode.toString().toLowerCase());

	if (mode == Mode.BATCH) {
	    // find next batch index
	    int batchIndex = findNextIndex(directory, directoryName);
	    directoryName = Output.generateFileName(batchIndex, directoryName);
	}
	return directory.resolve(directoryName);
    }

    /**
     * Finds the first index for the inner path, which does not point to an
     * existing file / directory.
     * 
     * @param mode
     *            the launch mode
     * @param outerPath
     *            the outer ouput path
     * @return a unique inner path
     */
    private static int findFirstIndex(Mode mode, final Path outerPath) {
	// already created unique outer path when in batch
	if (mode == Mode.BATCH || Files.notExists(outerPath)) {
	    return 0;
	} else {
	    return findNextIndex(outerPath, Output.generateFileName(CLASSIFIER_INNER, ""));
	}
    }

    /**
     * Processes class literal to be used in a file name.
     * 
     * @param clazz
     *            the class literal
     * @return the class name processed to be used in file names
     */
    private static String processForFileName(Class<?> clazz) {
	return clazz.getSimpleName().toLowerCase();
    }

    /**
     * Finds next index for files starting with {@code prefixBeforeIndex} to be
     * used in output.
     * 
     * @param directory
     *            the directory to find the next index
     * @param beforeIndex
     *            the substring before the index in searched file names
     * @return index after the last already present in {@code directory}.
     */
    private static int findNextIndex(Path directory, final String beforeIndex) {
	return findNextIndex(directory.toFile(), beforeIndex);
    }

    /**
     * Finds next index for files starting with {@code prefixBeforeIndex} to be
     * used in output.
     * 
     * @param directory
     *            the directory to find the next index
     * @param beforeIndex
     *            the substring before the index in searched file names
     * @return index after the last already present in {@code directory}.
     */
    // TODO with java.nio when in Java 8
    private static int findNextIndex(File directory, final String beforeIndex) {
	if (!directory.isDirectory()) {
	    throw new IllegalArgumentException(directory + " must be a directory.");
	}

	// get list of files from former simulation runs
	File[] files = directory.listFiles(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		if (name.startsWith(beforeIndex)) {
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
	int lastIndex = Integer.parseInt(lastFileName.replaceAll("[^0-9]", ""));

	return lastIndex + 1;
    }

    /**
     * Iterator to generate new inner paths with an incrementing index.
     * 
     * @author mey
     *
     */
    private static class InnerPathIterator implements Iterator<Path> {
	private final Path outerPath;
	private int runIndex;

	private InnerPathIterator(Path outerPath, int firstRunIndex) {
	    this.outerPath = outerPath;
	    this.runIndex = firstRunIndex;
	}

	@Override
	public Path next() {
	    String innerPathName = Output.generateFileName(runIndex++, CLASSIFIER_INNER);
	    Path outputPath = outerPath.resolve(innerPathName);

	    return outputPath;
	}

	@Override
	public boolean hasNext() {
	    return runIndex <= Integer.MIN_VALUE;
	}
    }

}
