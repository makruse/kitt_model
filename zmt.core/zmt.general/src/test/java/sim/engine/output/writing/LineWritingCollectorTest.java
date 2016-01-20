package sim.engine.output.writing;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import sim.engine.output.TestCollector;
import sim.engine.output.message.DefaultAfterMessage;

public class LineWritingCollectorTest {
    private static final String HEADER = "header";
    private static final String VALUE = "value";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private LineWritingCollector<?> writingCollector;
    private Path outputFile;

    @Before
    public void setUp() throws Exception {
	outputFile = folder.newFile("output.csv").toPath();
	writingCollector = new LineWritingCollector<>(new TestCollector(HEADER, VALUE), outputFile);
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
	assertThat(Files.readAllLines(outputFile, StandardCharsets.UTF_8),
		contains(containsString(HEADER), equalToIgnoringWhiteSpace(String.valueOf(steps) + " " + VALUE)));
    }
}
