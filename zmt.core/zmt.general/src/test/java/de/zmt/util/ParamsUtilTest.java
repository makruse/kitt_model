package de.zmt.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.params.TestParams;

public class ParamsUtilTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtilTest.class.getName());

    private static final String PARAMS_PATH = "params_temp.xml";

    private static final String STRING_VALUE = "correct value";
    private static final int INT_VALUE = 33;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testWriteRead() throws IOException {
	TestParams params = new TestParams();
	params.getDefinition().setStringValue(STRING_VALUE);
	params.getDefinition().setIntValue(INT_VALUE);

	Path path = folder.newFile(PARAMS_PATH).toPath();

	testWriteRead(params, path);
    }

    /**
     * Writes object to path, reads it from path and compare both.
     * 
     * @param object
     * @param path
     * @throws IOException
     */
    public static void testWriteRead(Object object, Path path) throws IOException {
	ParamsUtil.writeToXml(object, path);
	assertThat(ParamsUtil.readFromXml(path, Object.class), is(object));
    }
}
