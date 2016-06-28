package de.zmt.output.writing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.output.AbstractCollectable;
import de.zmt.output.Collectable;
import de.zmt.output.OneShotCollectable;
import de.zmt.output.TestCollector;

public class OneShotCollectorWriterTest {
    private static final String HEADER = "header";
    private static final List<String> VALUE_COLUMN = Arrays.asList("value1", "value2");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private OneShotCollectorWriter collectorWriter;
    private File outputFile;

    @Before
    public void setUp() throws Exception {
        outputFile = new File(folder.newFolder(), "output");
        Collectable<Iterable<String>> collectable = new TestOneShotCollectable(Collections.singletonList(VALUE_COLUMN),
                Collections.singletonList(HEADER), VALUE_COLUMN.size());
        collectorWriter = new OneShotCollectorWriter(new TestCollector(collectable), outputFile.toPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void writeData() throws IOException {
        long steps = 0;

        collectorWriter.writeValues(steps);
        File[] outputFiles = outputFile.getParentFile().listFiles();
        assertThat(outputFiles, arrayWithSize(1));
        assertThat(Files.readAllLines(outputFiles[0].toPath(), StandardCharsets.UTF_8),
                contains(equalToIgnoringWhiteSpace(HEADER), equalToIgnoringWhiteSpace(VALUE_COLUMN.get(0)),
                        equalToIgnoringWhiteSpace(VALUE_COLUMN.get(1))));
    }

    private static class TestOneShotCollectable extends AbstractCollectable<Iterable<String>>
            implements OneShotCollectable<String, Iterable<String>> {
        private static final long serialVersionUID = 1L;

        private final int columnSize;
        private final List<String> headers;

        @SuppressWarnings("unchecked")
        public TestOneShotCollectable(List<? extends Iterable<String>> data, List<String> headers, int columnSize) {
            super((List<Iterable<String>>) data);
            this.columnSize = columnSize;
            this.headers = headers;
        }

        @Override
        public int getColumnSize() {
            return columnSize;
        }

        @Override
        public List<String> obtainHeaders() {
            return headers;
        }

        @Override
        public List<Iterable<String>> obtainValues() {
            return super.obtainValues();
        }

    }
}
