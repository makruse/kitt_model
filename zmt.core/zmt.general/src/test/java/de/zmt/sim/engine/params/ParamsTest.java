package de.zmt.sim.engine.params;

import static org.junit.Assert.*;

import java.io.*;
import java.util.logging.Logger;

import javax.xml.bind.*;

import org.junit.Test;
import org.xml.sax.*;

import de.zmt.util.ParamsUtil;

public class ParamsTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsTest.class
	    .getName());

    private static final String PARAMS_PATH = "params_temp.xml";
    private static final String INVALID_PARAMS_PATH = "params-invalid.xml";
    private static final String SCHEMA_PATH = "www.zmt-bremen.de.xsd";

    private static final String STRING_VALUE = "correct value";
    private static final int INT_VALUE = 33;

    @Test
    public void testWriteRead() throws JAXBException, IOException {
	TestParams writeParams = new TestParams();
	writeParams.getDefinition().setStringValue(STRING_VALUE);
	writeParams.getDefinition().setIntValue(INT_VALUE);

	ParamsUtil.writeToXml(writeParams, PARAMS_PATH);

	TestParams readParams = ParamsUtil.readFromXml(PARAMS_PATH,
		TestParams.class);
	assertEquals(writeParams.getDefinition().getStringValue(), readParams
		.getDefinition().getStringValue());
	assertEquals(writeParams.getDefinition().getIntValue(), readParams
		.getDefinition().getIntValue());

	new File(PARAMS_PATH).deleteOnExit();
    }

    @Test
    public void testValidate() throws SAXException, FileNotFoundException,
	    JAXBException {
	String invalidParamsPath = ParamsTest.class.getResource(
		INVALID_PARAMS_PATH).getPath();
	String schemaPath = ParamsTest.class.getResource(SCHEMA_PATH).getPath();

	try {
	    ParamsUtil.readFromXml(invalidParamsPath, TestParams.class,
		    schemaPath);
	} catch (UnmarshalException e) {
	    if (e.getLinkedException() instanceof SAXParseException) {
		logger.info("Validation failed with invalid xml file. This is correct.");
		return;
	    }
	}
	fail("Validation was successful with invalid xml file. This is not correct.");
    }

}
