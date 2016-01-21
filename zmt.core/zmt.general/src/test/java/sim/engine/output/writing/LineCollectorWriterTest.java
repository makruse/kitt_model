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
import sim.engine.output.writing.LineCollectorWriter;

public class LineCollectorWriterTest {
    private static final String HEADER = "header";
    private static final String VALUE = "value";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private LineCollectorWriter collectorWriter;
    private Path outputFile;

    @Before
    public void setUp() throws Exception {
	outputFile = folder.newFile("output.csv").toPath();
	collectorWriter = new LineCollectorWriter(new TestCollector(HEADER, VALUE), outputFile);
    }

    @After
    public void tearDown() throws Exception {
	collectorWriter.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void writeData() throws IOException {
	long steps = 0;

	collectorWriter.writeValues(steps);
	assertThat(Files.readAllLines(outputFile, StandardCharsets.UTF_8),
		contains(containsString(HEADER), equalToIgnoringWhiteSpace(String.valueOf(steps) + " " + VALUE)));
    }
}
