package sim.engine.output.writing;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.*;
import java.nio.file.Files;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import sim.engine.output.TestCollector;
import sim.engine.output.message.DefaultAfterMessage;
import sim.engine.output.writing.LineWritingCollector;

public class WritingCollectorTest {
    private static final String HEADER = "header";
    private static final String VALUE = "value";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private LineWritingCollector writingCollector;
    private File outputFile;

    @Before
    public void setUp() throws Exception {
	outputFile = folder.newFile("output.csv");
	writingCollector = new LineWritingCollector(new TestCollector(HEADER, VALUE), outputFile);
    }

    @After
    public void tearDown() throws Exception {
	writingCollector.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void writeData() throws IOException {
	long steps = 0;

	writingCollector.writeValues(new DefaultAfterMessage(steps));
	assertThat(Files.readAllLines(outputFile.toPath()),
		contains(containsString(HEADER), equalToIgnoringWhiteSpace(String.valueOf(steps) + " " + VALUE)));
    }
}
