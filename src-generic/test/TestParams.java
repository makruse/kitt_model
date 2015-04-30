package test;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;
import org.xml.sax.*;

import de.zmt.sim.engine.params.AbstractParams;
import de.zmt.sim.engine.params.def.*;

public class TestParams {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TestParams.class
	    .getName());

    private static final String PARAMS_PATH = "params.xml";
    private static final String INVALID_PARAMS_PATH = "params-invalid.xml";
    private static final String SCHEMA_PATH = "www.zmt-bremen.de.xsd";

    private static final String ATTRIBUTE_VALUE = "correct value";

    @Test
    public void testWriteRead() throws JAXBException, IOException {
	Params writeParams = new Params();
	writeParams.getDefinition().setAttribute(ATTRIBUTE_VALUE);

	writeParams.writeToXml(PARAMS_PATH);

	Params readParams = AbstractParams.readFromXml(PARAMS_PATH,
		Params.class);
	assertEquals(writeParams.getDefinition().getAttribute(), readParams
		.getDefinition().getAttribute());

	new File(PARAMS_PATH).deleteOnExit();
    }

    @Test
    public void testValidate() throws SAXException, FileNotFoundException,
	    JAXBException {
	String invalidParamsPath = TestParams.class.getResource(
		INVALID_PARAMS_PATH).getPath();
	String schemaPath = TestParams.class.getResource(SCHEMA_PATH).getPath();

	try {
	    AbstractParams.readFromXml(invalidParamsPath, Params.class,
		    schemaPath);
	} catch (UnmarshalException e) {
	    if (e.getLinkedException() instanceof SAXParseException) {
		logger.info("Validation failed with invalid xml file. This is correct.");
		return;
	    }
	}
	fail("Validation was successful with invalid xml file. This is not correct.");
    }

    @XmlRootElement(name = "params", namespace = "http://www.zmt-bremen.de/")
    @SuppressWarnings({ "unused", "serial" })
    private static class Params extends AbstractParams {
	private Definition definition = new Definition();

	public Definition getDefinition() {
	    return definition;
	}

	public void setDefinition(Definition definition) {
	    this.definition = definition;
	}

	@Override
	public Collection<ParamDefinition> getDefinitions() {
	    return Arrays.asList((ParamDefinition) definition);
	}

	@Override
	public boolean removeOptionalDefinition(
		OptionalParamDefinition optionalDef) {
	    return false;
	}
    }

    @SuppressWarnings({ "unused", "serial" })
    private static class Definition implements ParamDefinition {
	private String attribute;

	@Override
	public String getTitle() {
	    return "Title";
	}

	public String getAttribute() {
	    return attribute;
	}

	public void setAttribute(String attribute) {
	    this.attribute = attribute;
	}
    }

}
