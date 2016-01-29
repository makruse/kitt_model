package de.zmt.launcher.strategies;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.launcher.LauncherArgs.Mode;
import de.zmt.launcher.LauncherTest.TestSimState;

public class DefaultOutputPathGeneratorTest {
    private static final OutputPathGenerator OUTPUT_PATH_GENERATOR = new DefaultOutputPathGenerator();
    private static final int ITERATIONS = 6;

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Path tempDir;

    @Before
    public void setUp() throws Exception {
	tempDir = folder.newFolder().toPath();
    }

    @Test
    public void createPathsOnEmpty() throws IOException {
	createAndVerify(0, Mode.BATCH);
    }

    @Test
    public void createPathsOnSingleNonEmpty() throws IOException {
	createPathsOnNonEmpty(Mode.SINGLE);
    }

    @Test
    public void createPathsOnBatchNonEmpty() throws IOException {
	createPathsOnNonEmpty(Mode.BATCH);
    }

    private void createPathsOnNonEmpty(Mode mode) throws IOException {
	// create a folder that should be skipped later
	OUTPUT_PATH_GENERATOR.createPaths(TestSimState.class, mode, tempDir).iterator().next();

	// create another file / folder which should not matter
	folder.newFile("file");
	folder.newFolder("folder");

	createAndVerify(1, mode);
    }

    private void createAndVerify(int firstIndex, Mode mode) {
	Set<Path> uniquePaths = new HashSet<>();
	Iterator<Path> iterator = OUTPUT_PATH_GENERATOR.createPaths(TestSimState.class, mode, tempDir).iterator();

	for (int i = 0; i < ITERATIONS; i++) {
	    Path outputPath = iterator.next();
	    String innerName = outputPath.getFileName().toString();
	    String outerName = outputPath.getParent().toString();

	    assertThat(outerName, containsString(mode.toString().toLowerCase()));

	    if (mode == Mode.BATCH) {
		assertThat(outerName, endsWith(Integer.toString(firstIndex)));
		assertThat(innerName, endsWith(Integer.toString(i)));
	    } else {
		assertThat(innerName, endsWith(Integer.toString(i + firstIndex)));
	    }

	    assertTrue(Files.isDirectory(outputPath));
	    // adding a duplicate would return false
	    assertTrue(uniquePaths.add(outputPath));
	}
    }
}
