package sim.engine.params;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.zmt.util.ParamsUtil;

public class ParamsTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsTest.class.getName());

    private static final String PARAMS_PATH = "params_temp.xml";
    private static final String INVALID_PARAMS_PATH = "params-invalid.xml";
    private static final String SCHEMA_PATH = "www.zmt-bremen.de.xsd";

    private static final String STRING_VALUE = "correct value";
    private static final int INT_VALUE = 33;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testWriteRead() throws JAXBException, IOException {
	TestParams writeParams = new TestParams();
	writeParams.getDefinition().setStringValue(STRING_VALUE);
	writeParams.getDefinition().setIntValue(INT_VALUE);

	File paramsFile = folder.newFile(PARAMS_PATH);

	ParamsUtil.writeToXml(writeParams, paramsFile);

	TestParams readParams = ParamsUtil.readFromXml(paramsFile, TestParams.class);
	assertEquals(writeParams.getDefinition().getStringValue(), readParams.getDefinition().getStringValue());
	assertEquals(writeParams.getDefinition().getIntValue(), readParams.getDefinition().getIntValue());
    }

    @Test
    public void testValidate() throws SAXException, IOException, JAXBException {
	String invalidParamsPath = ParamsTest.class.getResource(INVALID_PARAMS_PATH).getPath();
	String schemaPath = ParamsTest.class.getResource(SCHEMA_PATH).getPath();

	try {
	    ParamsUtil.readFromXml(invalidParamsPath, TestParams.class, schemaPath);
	} catch (UnmarshalException e) {
	    if (e.getLinkedException() instanceof SAXParseException) {
		logger.info("Validation failed with invalid xml file. This is correct.");
		return;
	    }
	}
	fail("Validation was successful with invalid xml file. This is not correct.");
    }

}
